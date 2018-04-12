package edu.gemini.spModel.gemini.ghost

import edu.gemini.spModel.core.AlmostEqual._
import edu.gemini.spModel.gemini.ghost.AsterismConverters._
import edu.gemini.spModel.target.env.{Almosts, Arbitraries, TargetEnvironment}

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.scalacheck.{Gen, Prop}
import org.scalacheck.Prop._

import scalaz._
import Scalaz._


object AsterismConvertersSpec extends Specification with ScalaCheck with Arbitraries with Almosts {

  "Asterism conversion" should {
    "Convert SingleTarget to itself losslessly" in {
      oneWayTest(genGhostSingleTargetTargetEnvironment, GhostSingleTargetConverter)
    }

    "Convert DualTarget to itself losslessly" in {
      oneWayTest(genGhostDualTargetTargetEnvironment, GhostDualTargetConverter)
    }

    "Convert TargetPlusSky to itself losslessly" in {
      oneWayTest(genGhostTargetPlusSkyTargetEnvironment, GhostTargetPlusSkyConverter)
    }

    "Convert SkyPlusTarget to itself losslessly" in {
      oneWayTest(genGhostSkyPlusTargetTargetEnvironment, GhostSkyPlusTargetConverter)
    }

    "Convert HighResolution to itself losslessly" in {
      oneWayTest(genHighResAsterismTargetEnvironment, GhostHighResolutionConverter)
    }

    "Convert between TargetPlusSky and SkyPlusTarget losslessly" in {
      twoWayTest(genGhostTargetPlusSkyTargetEnvironment, GhostSkyPlusTargetConverter, GhostTargetPlusSkyConverter)
    }

    "Convert between SkyPlusTarget and TargetPlusSky losslessly" in {
      twoWayTest(genGhostSkyPlusTargetTargetEnvironment, GhostTargetPlusSkyConverter, GhostSkyPlusTargetConverter)
    }

    "Convert between TargetPlusSky and HighResolution losslessly" in {
      twoWayTest(genGhostTargetPlusSkyTargetEnvironment, GhostHighResolutionConverter, GhostTargetPlusSkyConverter)
    }

    "Convert between SkyPlusTarget and HighResolution losslessly" in {
      twoWayTest(genGhostSkyPlusTargetTargetEnvironment, GhostHighResolutionConverter, GhostSkyPlusTargetConverter)
    }

    "Be able to convert any GHOST asterism to any other" in {
      forAll(genGhostAsterismTargetEnvironment) { env =>
        GhostConverters.forall(_.convert(env) should not(beEmpty))
      }
    }

    "Not convert any Single asterism to a GHOST asterism" in {
      forAll(genSingleAsterismTargetEnvironment) { env =>
        GhostConverters.forall(_.convert(env) should beEmpty)
      }
    }
  }

  private val GhostConverters: List[AsterismConverter] = List(
    GhostSingleTargetConverter,
    GhostDualTargetConverter,
    GhostTargetPlusSkyConverter,
    GhostSkyPlusTargetConverter,
    GhostHighResolutionConverter
  )

  private def oneWayTest(gen: Gen[TargetEnvironment], converter: AsterismConverter): Prop =
    forAll(gen) { env =>
      converter.convert(env).exists(_ ~= env) should beTrue
    }

  private def twoWayTest(gen: Gen[TargetEnvironment], c1: GhostAsterismConverter, c2: GhostAsterismConverter): Prop =
    forAll(gen) { env =>
      c1.convert(env).flatMap(c2.convert).exists(_ ~= env) should beTrue
    }
}