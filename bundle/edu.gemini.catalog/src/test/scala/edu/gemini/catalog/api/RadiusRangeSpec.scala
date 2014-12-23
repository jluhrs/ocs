package edu.gemini.catalog.api

import edu.gemini.spModel.core.Target.SiderealTarget
import edu.gemini.spModel.core._
import org.specs2.ScalaCheck
import org.scalacheck.Prop._
import org.specs2.mutable.SpecificationWithJUnit

import scalaz._
import Scalaz._

class RadiusRangeSpec extends SpecificationWithJUnit with ScalaCheck with Arbitraries {
  val min: Angle = Angle.fromDegrees(10 / 60.0)
  val max: Angle = Angle.fromDegrees(20 / 60.0)

  "Radius Range" should {
    "filter at 0,0" in {
      val rad = RadiusRange.between(max, min)

      val outMaxNeg = Angle.fromDegrees(-20.1 / 60)
      val outMaxPos = Angle.fromDegrees(20.1 / 60)

      val brdMaxNeg = Angle.fromDegrees(-19.99999 / 60)
      val brdMaxPos = Angle.fromDegrees(19.99999 / 60)

      val inMaxNeg = Angle.fromDegrees(-19.9 / 60)
      val inMaxPos = Angle.fromDegrees(19.9 / 60)

      val outMinNeg = Angle.fromDegrees(-9.9 / 60)
      val outMinPos = Angle.fromDegrees(9.9 / 60)

      val brdMinNeg = Angle.fromDegrees(-10.0001 / 60)
      val brdMinPos = Angle.fromDegrees(10.0001 / 60)

      val inMinNeg = Angle.fromDegrees(-10.1 / 60)
      val inMinPos = Angle.fromDegrees(10.1 / 60)

      val out = List(outMaxNeg, outMaxPos, outMinNeg, outMinPos)
      val in = List(brdMaxNeg, brdMaxPos, inMaxNeg, inMaxPos, brdMinNeg, brdMinPos, inMinNeg, inMinPos)
      val f = rad.targetsFilter(Coordinates.zero)

      ras(out, Angle.zero).filter(f) should beEmpty
      decs(Angle.zero, out).filter(f) should beEmpty
      ras(in, Angle.zero).filter(f) should be size 8
      decs(Angle.zero, in).filter(f) should be size 8

      // in the middle of the limits along the diagonals
      val mins = List(outMinNeg, outMinPos, brdMinNeg, brdMinPos, inMinNeg, inMinPos)
      perms(mins).filter(f) should be size 36

      // out of the max limits along the diagonals
      val maxs = List(outMaxNeg, outMaxPos, brdMaxNeg, brdMaxPos, inMaxNeg, inMaxPos)
      perms(maxs).filter(f) must beEmpty
    }
    "filter at 0,90" in {
      val base = Coordinates(RightAscension.fromDegrees(0), Declination.fromAngle(Angle.fromDegrees(90)).getOrElse(Declination.zero))
      val rad = RadiusRange.between(max, min)
      val f = rad.targetsFilter(base)
      val ra = Angle.fromDegrees(180)
      val deg90 = Angle.fromDegrees(90)
      val dec9 = deg90 + Angle.fromDegrees(-9.0 / 60)
      val dec11 = deg90 + Angle.fromDegrees(-11.0 / 60)
      val dec19 = deg90 + Angle.fromDegrees(-19.0 / 60)
      val dec21 = deg90 + Angle.fromDegrees(-21.0 / 60)
      f.apply(mkSkyObject(ra, dec9)) should beFalse
      f.apply(mkSkyObject(ra, dec11)) should beTrue
      f.apply(mkSkyObject(ra, dec19)) should beTrue
      f.apply(mkSkyObject(ra, dec21)) should beFalse

      f(mkSkyObject(ra, dec9)) should beFalse
      f(mkSkyObject(ra, dec11)) should beTrue
      f(mkSkyObject(ra, dec19)) should beTrue
      f(mkSkyObject(ra, dec21)) should beFalse
    }
    "fail for different coordinates if range is 0" in {
      forAll { (c:Coordinates, a: Angle, b: Angle) =>
        val notTheSame = c != Coordinates(RightAscension.fromAngle(a), Declination.fromAngle(b).getOrElse(Declination.zero))
        val rad = RadiusRange.between(Angle.zero, Angle.zero)
        val f = rad.targetsFilter(c)
        notTheSame ==> ( f(mkSkyObject(a, b)) should beFalse )
      }
    }
    "work for all if range is full" in {
      forAll { (c:Coordinates, a: Angle, b: Angle) =>
        // 360 gets turned to 0, we try instead to get as close as possible to 360
        val rad = RadiusRange.between(Angle.fromDegrees(-0.000000001/3600), Angle.zero)
        val f = rad.targetsFilter(c)
        f(mkSkyObject(a, b)) should beTrue
      }
    }
  }

  private def ras(ras: List[Angle], dec: Angle): List[SiderealTarget] =
    for (ra <- ras) yield mkSkyObject(ra, dec)

  private def decs(ra: Angle, decs: List[Angle]): List[SiderealTarget] =
    for (dec <- decs) yield mkSkyObject(ra, dec)

  private def perms(a:List[Angle]): List[SiderealTarget] =
    for {
      ra <- a
      dec <- a
    } yield mkSkyObject(ra, dec)

  private def name(ra: Angle, dec: Angle): String =
    s"ra=${ra.toDegrees}, dec=${dec.toDegrees}"

  private def mkSkyObject(ra: Angle, dec: Angle): SiderealTarget =
    mkSkyObject(name(ra, dec), Coordinates(RightAscension.fromAngle(ra), Declination.fromAngle(dec).getOrElse(Declination.zero)))

  private def mkSkyObject(name: String, c: Coordinates): SiderealTarget =
    SiderealTarget(name, c, Equinox.J2000, None, Nil, None)
}
