// Copyright (c) 2016-2019 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package edu.gemini.spModel.gemini.ghost

import java.time.Instant

import scala.math.{atan2, cos, hypot, sin}
import scalaz._
import Scalaz._
import edu.gemini.spModel.core.{Angle, Coordinates, Epoch, Offset, Redshift}

/**
 * Time-parameterized coordinates, based on an observed position at some point in time (called
 * the `epoch`) and measured velocities in distance (`radialVelocity`; i.e., doppler shift) and
 * position (`properVelocity`) per year. Given this information we can compute the position at any
 * instant in time. The references below are ''extremely'' helpful, so do check them out if you're
  * trying to understand the implementation.
 * @see The pretty good [[https://en.wikipedia.org/wiki/Proper_motion wikipedia]] article
 * @see Astronomical Almanac 1984 [[https://babel.hathitrust.org/cgi/pt?id=uc1.b3754036;view=1up;seq=141 p.B39]]
 * @see Astronomy and Astrophysics 134 (1984) [[http://articles.adsabs.harvard.edu/cgi-bin/nph-iarticle_query?bibcode=1984A%26A...134....1L&db_key=AST&page_ind=0&data_type=GIF&type=SCREEN_VIEW&classic=YES p.1-6]]
 * @param baseCoordinates observed coordinates at `epoch`
 * @param epoch           time of the base observation; typically `Epoch.J2000`
 * @param properVelocity  proper velocity '''per year''' in [[RightAscension]] and [[Declination]], if any
 * @param radialVelocity  radial velocity (km/y, positive if receding), if any
 * @param parallax        parallax, if any
 */
final case class ProperMotionCalculator(
                               baseCoordinates: Coordinates,
                               epoch:           Epoch,
                               properVelocity:  Option[Offset],
                               radialVelocity:  Option[Redshift],
                               parallax:        Option[Angle]
) {

  def at(i: Instant): ProperMotion =
    plusYears(epoch.untilInstant(i))

  /** Coordinates `elapsedYears` fractional epoch-years after `epoch`. */
  def plusYears(elapsedYears: Double): ProperMotion =
    ProperMotion(
      ProperMotion.properMotion(
        baseCoordinates,
        epoch,
        properVelocity.getOrElse(Offset.zero),
        radialVelocity.getOrElse(RadialVelocity.Zero).toDoubleKilometersPerSecond,
        parallax.getOrElse(Angle.zero),
        elapsedYears
      ),
      epoch.plusYears(elapsedYears),
      properVelocity,
      radialVelocity,
      parallax
    )

}


object ProperMotion extends ProperMotionOptics {
  /** One AU in meters. */
  val AstronomicalUnit: Long = 149597870660L

  /** 2π, to higher precision than what you get in stdlib. */
  val TwoPi: Double = 6.283185307179586476925286766559

  def const(cs: Coordinates): ProperMotion =
    ProperMotion(cs, Epoch.J2000, None, None, None)

  /**
   * Proper motion correction in model units.
   * @param baseCoordinates base coordinates
   * @param epoch           the epoch
   * @param properVelocity  proper velocity per epoch year
   * @param radialVelocity  radial velocity (km/sec, positive if receding)
   * @param parallax        parallax
   * @param elapsedYears    elapsed time in epoch years
   * @return Coordinates corrected for proper motion
   */
  def properMotion(
    baseCoordinates: Coordinates,
    epoch:           Epoch,
    properVelocity:  Offset,
    radialVelocity:  Double,
    parallax:        Angle,
    elapsedYears:    Double
  ): Coordinates = {
    val (ra, dec) = properMotionʹ(
      baseCoordinates,
      epoch.scheme.lengthOfYear,
      properVelocity,
      radialVelocity,
      parallax.toArcsecs,
      elapsedYears
    )
    // TODO: What do we do if Declination returns an Option?
    Coordinates(RightAscension.fromAngle(Angle.fromRadians(ra), Declination(Angle.fromRadians(dec)))
  }

  // Some constants we need
  private val secsPerDay  = 86400.0
  private val auPerKm     = 1000.0 / AstronomicalUnit.toDouble
  //private val radsPerAsec = Angle.arcseconds.reverseGet(1).toDoubleRadians
  private val radsPerAsec = Angle.fromArcsecs(1).toArcsecs

  // We need to do things with little vectors of doubles
  private type Vec2 = (Double, Double)
  private type Vec3 = (Double, Double, Double)

  // |+| gives us addition for VecN, but we also need scalar multiplication
  private implicit class Vec3Ops(a: Vec3) {
    def *(d: Double): Vec3 =
      (a._1 * d, a._2 * d, a._3 * d)
  }

  /**
   * Proper motion correction in base units.
   * @param baseCoordinates base (ra, dec) in degrees, which we convert to radians, [0 … 2π) and (-π/2 … π/2)
   * @param daysPerYear     length of epoch year in fractonal days
   * @param properVelocity  proper velocity in (ra, dec) , which we convert to in radians per epoch year
   * @param radialVelocity  radial velocity (km/sec, positive means away from earth)
   * @param parallax        parallax in arcseconds (!)
   * @param elapsedYears    elapsed time in epoch years
   * @return (ra, dec) in radians, corrected for proper motion
   */
  // scalastyle:off method.length
  private def properMotionʹ(
    baseCoordinates: Coordinates,
    daysPerYear:     Double,
    properVelocity:  Offset,
    parallax:        Double,
    radialVelocity:  Double,
    elapsedYears:    Double
  ): Vec2 = {

    // Break out our components
    val (ra,   dec) = (baseCoordinates.ra.toAngle.toRadians, baseCoordinates.dec.toAngle.toRadians)
    val (dRa, dDec) = (properVelocity.p.toAngle.toRadians,   properVelocity.q.toAngle.toRadians)

    // Convert to cartesian
    val pos: Vec3 = {
      val cd = cos(dec)
      (cos(ra) * cd, sin(ra) * cd, sin(dec))
    }

    // Change per year due to radial velocity and parallax. The units work out to asec/y.
    val dPos1: Vec3 =
      pos            *
      daysPerYear    *
      secsPerDay     *
      radsPerAsec    *
      auPerKm        *
      radialVelocity *
      parallax

    // Change per year due to proper velocity
    val dPos2 = (
      -dRa * pos._2 - dDec * cos(ra) * sin(dec),
       dRa * pos._1 - dDec * sin(ra) * sin(dec),
                      dDec *           cos(dec)
    )

    // Our new position (still in polar coordinates). `|+|` here is scalar addition provided by
    // cats … unlike scalaz it does give you Semigroup[Double] even though it's not strictly lawful.
    val pʹ = pos |+| ((dPos1 |+| dPos2) * elapsedYears)

    // Back to spherical
    val (x, y, z) = pʹ
    val r    = hypot(x, y)
    val raʹ  = if (r === 0.0) 0.0 else atan2(y, x)
    val decʹ = if (z === 0.0) 0.0 else atan2(z, r)
    val raʹʹ = {
      // Normalize to [0 .. 2π)
      val rem = raʹ % TwoPi
      if (rem < 0.0) rem + TwoPi else rem
    }
    (raʹʹ, decʹ)

  }
  // scalastyle:on method.length

  implicit val OrderProperMotion: Order[ProperMotion] = {

    implicit val MonoidOrder: Monoid[Order[ProperMotion]] =
      Order.whenEqualMonoid[ProperMotion]

    implicit val AngleOrder: Order[Angle] =
      Angle.SignedAngleOrder

    def order[A: Order](f: ProperMotion => A): Order[ProperMotion] =
      Order.by(f)

    // This could be done with:
    //
    //   Order.by(p => (p.baseCoordinates, p.epoch, ...))
    //
    // but that would always perform comparisons for all the fields (and all
    // their contained fields down to the leaves of the tree) all of the time.
    // The Monoid approach on the other hand will stop at the first difference.
    // This is premature optimization perhaps but it seems like it might make a
    // difference when sorting a long list of targets.

    order(_.baseCoordinates)  |+|
      order(_.epoch)          |+|
      order(_.properVelocity) |+|
      order(_.radialVelocity) |+|
      order(_.parallax)

  }
}

trait ProperMotionOptics {

  val baseCoordinates: ProperMotion @> Coordinates =
    Lens.lensu((a, b) => a.copy(baseCoordinates = b), _.baseCoordinates)

  val epoch: ProperMotion @> Epoch =
    Lens.lensu((a, b) => a.copy(epoch = b), _.epoch)

  val pv: ProperMotion @> Option[Offset] =
    Lens.lensu((a, b) => a.copy(properVelocity = b), _.properVelocity)

  val properVelocity: ProperMotion @> Option[RedShift] =
    Lens.lensu((a, b) => a.copy(redShift = b), _.redShift)

  val parallax ProperMotion @> Option[Angle] =
    Lens.lensu((a, b) => a.copy(parallax = b), _.parallax)

}
