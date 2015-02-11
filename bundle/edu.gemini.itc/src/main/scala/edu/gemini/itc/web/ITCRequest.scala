package edu.gemini.itc.web

import javax.servlet.http.HttpServletRequest

import edu.gemini.itc.altair.AltairParameters
import edu.gemini.itc.parameters.{ObservingConditionParameters, PlottingDetailsParameters, TeleParameters}
import edu.gemini.itc.shared.ITCMultiPartParser
import edu.gemini.spModel.gemini.altair.AltairParams
import edu.gemini.spModel.gemini.obscomp.SPSiteQuality
import edu.gemini.spModel.telescope.IssPort

/**
 * ITC requests define a generic mechanism to look up values by their parameter names.
 * The different values are either enums, ints or doubles. For enums the simple name of the class is used
 * as the parameter name. This convention allows for a mostly mechanical translation of typed values from
 * a request with string based parameters, i.e. parameters in HttpServletRequests for example.
 */
sealed abstract class ITCRequest {
  def parameter(name: String): String

  /** Gets the named value as an enum of type {{{Class[T]}}} using the simple name of the class as the parameter name. */
  def enumParameter[T <: Enum[T]](c: Class[T]): T = enumParameter(c, c.getSimpleName)

  /** Gets the named value as an enum of type {{{Class[T]}}} using the given name as the parameter name. */
  def enumParameter[T <: Enum[T]](c: Class[T], n: String): T = Enum.valueOf(c, parameter(n))

  /** Gets the named value as an integer. */
  def intParameter(name: String): Int = parameter(name).toInt

  /** Gets the named value as a double. */
  def doubleParameter(name: String): Double = parameter(name).toDouble
}

/**
 * Utility object that allows to translate different objects into ITC requests.
 */
object ITCRequest {

  def from(request: HttpServletRequest): ITCRequest = new ITCRequest {
    override def parameter(name: String): String = request.getParameter(name)
  }
  def from(request: ITCMultiPartParser): ITCRequest = new ITCRequest {
    override def parameter(name: String): String = request.getParameter(name)
  }

  def teleParameters(r: ITCMultiPartParser): TeleParameters = {
    val pc      = ITCRequest.from(r)
    val coating = pc.enumParameter(classOf[TeleParameters.Coating])
    val port    = pc.enumParameter(classOf[IssPort])
    val wfs     = pc.enumParameter(classOf[TeleParameters.Wfs])
    new TeleParameters(coating, port, wfs)
  }

  def obsConditionParameters(r: ITCMultiPartParser): ObservingConditionParameters = {
    val pc      = ITCRequest.from(r)
    val iq      = pc.enumParameter(classOf[SPSiteQuality.ImageQuality])
    val cc      = pc.enumParameter(classOf[SPSiteQuality.CloudCover])
    val wv      = pc.enumParameter(classOf[SPSiteQuality.WaterVapor])
    val sb      = pc.enumParameter(classOf[SPSiteQuality.SkyBackground])
    val airmass = pc.doubleParameter("Airmass")
    new ObservingConditionParameters(iq, cc, wv, sb, airmass)
  }

  def plotParamters(r: ITCMultiPartParser): PlottingDetailsParameters = {
    val pc      = ITCRequest.from(r)
    val limits  = pc.enumParameter(classOf[PlottingDetailsParameters.PlotLimits])
    val lower   = pc.doubleParameter("plotWavelengthL")
    val upper   = pc.doubleParameter("plotWavelengthU")
    new PlottingDetailsParameters(limits, lower, upper)
  }

  def altairParameters(r: ITCMultiPartParser): AltairParameters = {
    val pc      = ITCRequest.from(r)
    val guideStarSeperation  = pc.doubleParameter("guideSep")
    val guideStarMagnitude   = pc.doubleParameter("guideMag")
    val fieldLens            = pc.enumParameter(classOf[AltairParams.FieldLens])
    val wfsMode              = pc.enumParameter(classOf[AltairParams.GuideStarType])
    val wfs                  = pc.enumParameter(classOf[TeleParameters.Wfs])
    val altairUsed           = wfs eq TeleParameters.Wfs.AOWFS
    new AltairParameters(guideStarSeperation, guideStarMagnitude, fieldLens, wfsMode, altairUsed)
  }

}
