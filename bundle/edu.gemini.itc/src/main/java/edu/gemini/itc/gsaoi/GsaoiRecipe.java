package edu.gemini.itc.gsaoi;

import edu.gemini.itc.gems.*;
import edu.gemini.itc.operation.*;
import edu.gemini.itc.parameters.ObservationDetailsParameters;
import edu.gemini.itc.parameters.ObservingConditionParameters;
import edu.gemini.itc.parameters.SourceDefinitionParameters;
import edu.gemini.itc.parameters.TeleParameters;
import edu.gemini.itc.shared.*;
import edu.gemini.itc.web.HtmlPrinter;
import edu.gemini.itc.web.ITCRequest;
import edu.gemini.spModel.core.Site;
import scala.Option;

import java.io.PrintWriter;

/**
 * This class performs the calculations for Gsaoi used for imaging.
 */
public final class GsaoiRecipe extends RecipeBase {

    private final GemsParameters _gemsParameters;
    private final GsaoiParameters _gsaoiParameters;
    private final ObservingConditionParameters _obsConditionParameters;
    private final ObservationDetailsParameters _obsDetailParameters;
    private final SourceDefinitionParameters _sdParameters;
    private final TeleParameters _teleParameters;

    /**
     * Constructs a GsaoiRecipe by parsing a Multipart servlet request.
     *
     * @param r   Servlet request containing form data from ITC web page.
     * @param out Results will be written to this PrintWriter.
     * @throws Exception on failure to parse parameters.
     */
    public GsaoiRecipe(ITCMultiPartParser r, PrintWriter out) {
        super(out);

        _sdParameters = ITCRequest.sourceDefinitionParameters(r);
        _obsDetailParameters = ITCRequest.observationParameters(r);
        _obsConditionParameters = ITCRequest.obsConditionParameters(r);
        _gsaoiParameters = new GsaoiParameters(r);
        _teleParameters = ITCRequest.teleParameters(r);
        _gemsParameters = new GemsParameters(r);
    }

    /**
     * Constructs a GsaoiRecipe given the parameters. Useful for testing.
     */
    public GsaoiRecipe(SourceDefinitionParameters sdParameters,
                       ObservationDetailsParameters obsDetailParameters,
                       ObservingConditionParameters obsConditionParameters,
                       GsaoiParameters gsaoiParameters, TeleParameters teleParameters,
                       GemsParameters gemsParameters,
                       PrintWriter out)

    {
        super(out);
        _sdParameters = sdParameters;
        _obsDetailParameters = obsDetailParameters;
        _obsConditionParameters = obsConditionParameters;
        _gsaoiParameters = gsaoiParameters;
        _teleParameters = teleParameters;
        _gemsParameters = gemsParameters;
    }

    /**
     * Performes recipe calculation and writes results to a cached PrintWriter
     * or to System.out.
     *
     * @throws Exception A recipe calculation can fail in many ways, missing data
     *                   files, incorrectly-formatted data files, ...
     */
    public void writeOutput() {
        // Create the Chart visitor. After a sed has been created the chart
        // visitor
        // can be used by calling the following commented out code:

        _println("");

        // This object is used to format numerical strings.
        FormatStringWriter device = new FormatStringWriter();
        device.setPrecision(2); // Two decimal places
        device.clear();

        // Module 1b
        // Define the source energy (as function of wavelength).
        //
        // inputs: instrument, SED
        // calculates: redshifted SED
        // output: redshifteed SED
        Gsaoi instrument = new Gsaoi(_gsaoiParameters, _obsDetailParameters);

        if (_sdParameters.getDistributionType().equals(SourceDefinitionParameters.Distribution.ELINE))
            if (_sdParameters.getELineWidth() < (3E5 / (_sdParameters
                    .getELineWavelength() * 1000 * 25))) { // *25 b/c of
                // increased
                // resolution of
                // transmission
                // files
                throw new RuntimeException(
                        "Please use a model line width > 0.04 nm (or "
                                + (3E5 / (_sdParameters.getELineWavelength() * 1000 * 25))
                                + " km/s) to avoid undersampling of the line profile when convolved with the transmission response");
            }


        // Calculate image quality
        final ImageQualityCalculatable IQcalc = ImageQualityCalculationFactory.getCalculationInstance(_sdParameters, _obsConditionParameters, _teleParameters, instrument);
        IQcalc.calculate();

        // Altair specific section
        final Option<AOSystem> gems;
        if (_gemsParameters.gemsIsUsed()) {
            final Gems ao = new Gems(instrument.getEffectiveWavelength(),
                    _teleParameters.getTelescopeDiameter(), IQcalc.getImageQuality(),
                    _gemsParameters.getAvgStrehl(), _gemsParameters.getStrehlBand(),
                    _obsConditionParameters.getImageQualityPercentile(),
                    _sdParameters);
            _println(ao.printSummary());
            gems = Option.apply((AOSystem) ao);
        } else {
            gems = Option.empty();
        }

        final SEDFactory.SourceResult calcSource = SEDFactory.calculate(instrument, Site.GS, ITCConstants.NEAR_IR, _sdParameters, _obsConditionParameters, _teleParameters, null, gems);


        // End of the Spectral energy distribution portion of the ITC.

        // Start of morphology section of ITC

        // Module 1a
        // The purpose of this section is to calculate the fraction of the
        // source flux which is contained within an aperture which we adopt
        // to derive the signal to noise ratio. There are several cases
        // depending on the source morphology.
        // Define the source morphology
        //
        // inputs: source morphology specification

        final double pixel_size = instrument.getPixelSize();
        double source_fraction = 0;
        double halo_source_fraction = 0;
        double peak_pixel_count = 0;

        final double sed_integral = calcSource.sed.getIntegral();
        final double sky_integral = calcSource.sky.getIntegral();

        double halo_integral = 0;
        if (_gemsParameters.gemsIsUsed()) {
            halo_integral = calcSource.halo.get().getIntegral();
        }

        // if gems is used we need to calculate both a core and halo
        // source_fraction
        // halo first
        final SourceFraction SFcalc;
        if (_gemsParameters.gemsIsUsed()) {
            final SourceFraction SFcalcHalo;
            final double im_qual = gems.get().getAOCorrectedFWHM();
            if (_obsDetailParameters.isAutoAperture()) {
                SFcalcHalo  = SourceFractionFactory.calculate(_sdParameters.isUniform(), false, 1.18 * im_qual, instrument.getPixelSize(), IQcalc.getImageQuality());
                SFcalc      = SourceFractionFactory.calculate(_sdParameters.isUniform(), _obsDetailParameters.isAutoAperture(), 1.18 * im_qual, instrument.getPixelSize(), im_qual);
            } else {
                SFcalcHalo  = SourceFractionFactory.calculate(_sdParameters, _obsDetailParameters, instrument, IQcalc.getImageQuality());
                SFcalc      = SourceFractionFactory.calculate(_sdParameters, _obsDetailParameters, instrument, im_qual);
            }
            halo_source_fraction = SFcalcHalo.getSourceFraction();
        } else {
           SFcalc = SourceFractionFactory.calculate(_sdParameters, _obsDetailParameters, instrument, IQcalc.getImageQuality());

        }

        // this will be the core for a gems source; unchanged for non gems.
        source_fraction = SFcalc.getSourceFraction();
        final double Npix = SFcalc.getNPix();
        if (_obsDetailParameters.getMethod().isImaging()) {
            if (_gemsParameters.gemsIsUsed()) {
                // If gems is used turn off printing of SF calc
                _print(SFcalc.getTextResult(device, false));
                _println("derived image halo size (FWHM) for a point source = "
                        + device.toString(IQcalc.getImageQuality()) + " arcsec.\n");
            } else {
                _print(SFcalc.getTextResult(device));
                _println(IQcalc.getTextResult(device));
            }
        }

        final PeakPixelFluxCalc ppfc;
        final double im_qual = gems.isDefined() ? gems.get().getAOCorrectedFWHM() : IQcalc.getImageQuality();
        if (!_sdParameters.isUniform()) {

            // calculation of image quaility was in here if the current setup
            // does not work copy it back in here from above, and uncomment
            // the section of code below for the uniform surface brightness.
            // the present way should work.

            ppfc = new PeakPixelFluxCalc(im_qual, pixel_size,
                    _obsDetailParameters.getExposureTime(), sed_integral,
                    sky_integral, instrument.getDarkCurrent());

            peak_pixel_count = ppfc.getFluxInPeakPixel();

            if (_gemsParameters.gemsIsUsed()) {
                PeakPixelFluxCalc ppfc_halo = new PeakPixelFluxCalc(
                        IQcalc.getImageQuality(), pixel_size,
                        _obsDetailParameters.getExposureTime(), halo_integral,
                        sky_integral, instrument.getDarkCurrent());
                peak_pixel_count = peak_pixel_count + ppfc_halo.getFluxInPeakPixel();

            }

        } else  {

            ppfc = new PeakPixelFluxCalc(im_qual, pixel_size,
                    _obsDetailParameters.getExposureTime(), sed_integral,
                    sky_integral, instrument.getDarkCurrent());

            peak_pixel_count = ppfc
                    .getFluxInPeakPixelUSB(source_fraction, Npix);
        }

        // In this version we are bypassing morphology modules 3a-5a.
        // i.e. the output morphology is same as the input morphology.
        // Might implement these modules at a later time.
        final int number_exposures = _obsDetailParameters.getNumExposures();
        final double frac_with_source = _obsDetailParameters.getSourceFraction();

        // report error if this does not come out to be an integer
        checkSourceFraction(number_exposures, frac_with_source);

        // ObservationMode Imaging

        final ImagingS2NCalculatable IS2Ncalc = ImagingS2NCalculationFactory.getCalculationInstance(_sdParameters, _obsDetailParameters, instrument);
        IS2Ncalc.setSedIntegral(sed_integral);
        if (_gemsParameters.gemsIsUsed()) {
            IS2Ncalc.setSecondaryIntegral(halo_integral);
            IS2Ncalc.setSecondarySourceFraction(halo_source_fraction);
        }
        IS2Ncalc.setSkyIntegral(sky_integral);
        IS2Ncalc.setSourceFraction(source_fraction);
        IS2Ncalc.setNpix(Npix);
        IS2Ncalc.setDarkCurrent(instrument.getDarkCurrent());
        IS2Ncalc.calculate();
        _println(IS2Ncalc.getTextResult(device));
        _println(IS2Ncalc.getBackgroundLimitResult());
        device.setPrecision(0); // NO decimal places
        device.clear();

        _println("");
        _println("The peak pixel signal + background is " + device.toString(peak_pixel_count));

        // REL-1353
        int peak_pixel_percent = (int) (100 * peak_pixel_count / 126000);
        _println("This is " + peak_pixel_percent + "% of the full well depth of 126000 electrons");
        if (peak_pixel_percent > 65 && peak_pixel_percent <= 85) {
            _error("Warning: the peak pixel + background level exceeds 65% of the well depth and will cause deviations from linearity of more than 5%.");
        } else if (peak_pixel_percent > 85) {
            _error("Warning: the peak pixel + background level exceeds 85% of the well depth and may cause saturation.");
        }

        _println("");
        device.setPrecision(2); // TWO decimal places
        device.clear();

        _print("<HR align=left SIZE=3>");

        _println("<b>Input Parameters:</b>");
        _println("Instrument: " + instrument.getName() + "\n");
        _println(HtmlPrinter.printParameterSummary(_sdParameters));
        _println(instrument.toString());
        if (_gemsParameters.gemsIsUsed()) {
            _println(printTeleParametersSummary("gems"));
            _println(_gemsParameters.printParameterSummary());
        } else {
            _println(printTeleParametersSummary());
        }
        _println(HtmlPrinter.printParameterSummary(_obsConditionParameters));
        _println(HtmlPrinter.printParameterSummary(_obsDetailParameters));

    }

    public String printTeleParametersSummary() {
        return printTeleParametersSummary(_teleParameters.getWFS().displayValue());
    }

    public String printTeleParametersSummary(String wfs) {
        StringBuffer sb = new StringBuffer();
        sb.append("Telescope configuration: \n");
        sb.append("<LI>" + _teleParameters.getMirrorCoating().displayValue() + " mirror coating.\n");
        sb.append("<LI>wavefront sensor: " + wfs + "\n");
        return sb.toString();
    }
}
