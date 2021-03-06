
import OcsKeys._
import edu.gemini.osgi.tools.app.{ Configuration => AppConfig, _ }
import edu.gemini.osgi.tools.app.Configuration.Distribution.{ Test => TestDistro, _ }

ocsAppSettings

ocsAppManifest := {
  val bs = List(
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_ags_servlet).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_ags_servlet).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_ags).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_ags).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_auxfile_workflow).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_auxfile_workflow).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_catalog).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_catalog).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_dataman_app).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_dataman_app).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_horizons_api).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_horizons_api).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_ictd).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_ictd).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_itc).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_itc).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_itc_shared).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_itc_shared).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_itc_web).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_itc_web).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_json).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_json).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_lchquery_servlet).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_lchquery_servlet).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_obslog).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_obslog).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_oodb_auth_servlet).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_oodb_auth_servlet).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_oodb_too_url).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_oodb_too_url).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_oodb_too_window).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_oodb_too_window).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_osgi_main).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_osgi_main).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_p2checker).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_p2checker).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_phase2_core).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_phase2_core).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_phase2_skeleton_servlet).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_phase2_skeleton_servlet).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_pit).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_pit).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_pit_launcher).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_pit_launcher).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_p1monitor).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_p1monitor).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_tools_p1pdfmaker).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_tools_p1pdfmaker).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_pot).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_pot).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_qpt_client).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_qpt_client).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_qpt_shared).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_qpt_shared).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_qv_plugin).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_qv_plugin).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_seqexec_odb).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_seqexec_odb).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_services_client).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_services_client).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_services_server).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_services_server).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_shared_ca).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_shared_ca).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_shared_gui).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_shared_gui).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_shared_mail).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_shared_mail).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_shared_skyobject).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_shared_skyobject).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_shared_util).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_shared_util).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_smartgcal_odbinit).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_smartgcal_odbinit).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_smartgcal_servlet).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_smartgcal_servlet).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_sp_vcs_log).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_sp_vcs_log).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_sp_vcs_reg).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_sp_vcs_reg).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_sp_vcs).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_sp_vcs).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_spModel_core).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_spModel_core).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_spModel_io).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_spModel_io).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_spModel_pio).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_spModel_pio).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_spModel_smartgcal).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_spModel_smartgcal).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_spdb_reports_collection).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_spdb_reports_collection).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_spdb_rollover_servlet).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_spdb_rollover_servlet).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_spdb_shell).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_spdb_shell).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_too_event).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_too_event).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_ui_workspace).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_ui_workspace).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_ui_miglayout).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_ui_miglayout).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_util_file_filter).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_util_file_filter).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_util_fits).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_util_fits).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_util_javax_mail).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_util_javax_mail).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_util_log_extras).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_util_log_extras).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_util_osgi).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_util_osgi).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_util_security).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_util_security).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_util_skycalc).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_util_skycalc).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_util_ssh).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_util_ssh).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_util_ssl_apache).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_util_ssl_apache).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_util_ssl).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_util_ssl).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_util_trpc).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_util_trpc).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_wdba_session_client).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_wdba_session_client).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_wdba_shared).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_wdba_shared).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_wdba_xmlrpc_api).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_wdba_xmlrpc_api).value)),
    BundleSpec((sbt.Keys.name in bundle_edu_gemini_wdba_xmlrpc_server).value, Version.parse((sbt.Keys.version in bundle_edu_gemini_wdba_xmlrpc_server).value)),
    BundleSpec((sbt.Keys.name in bundle_jsky_app_ot_plugin).value, Version.parse((sbt.Keys.version in bundle_jsky_app_ot_plugin).value)),
    BundleSpec((sbt.Keys.name in bundle_jsky_app_ot_shared).value, Version.parse((sbt.Keys.version in bundle_jsky_app_ot_shared).value)),
    BundleSpec((sbt.Keys.name in bundle_jsky_app_ot_visitlog).value, Version.parse((sbt.Keys.version in bundle_jsky_app_ot_visitlog).value)),
    BundleSpec((sbt.Keys.name in bundle_jsky_app_ot).value, Version.parse((sbt.Keys.version in bundle_jsky_app_ot).value)),
    BundleSpec((sbt.Keys.name in bundle_jsky_app_ot_testlauncher).value, Version.parse((sbt.Keys.version in bundle_jsky_app_ot_testlauncher).value)),
    BundleSpec((sbt.Keys.name in bundle_jsky_coords).value, Version.parse((sbt.Keys.version in bundle_jsky_coords).value)),
    BundleSpec((sbt.Keys.name in bundle_jsky_elevation_plot).value, Version.parse((sbt.Keys.version in bundle_jsky_elevation_plot).value)),
    BundleSpec((sbt.Keys.name in bundle_jsky_util_gui).value, Version.parse((sbt.Keys.version in bundle_jsky_util_gui).value)),
    BundleSpec((sbt.Keys.name in bundle_jsky_util).value, Version.parse((sbt.Keys.version in bundle_jsky_util).value))
  )
  Application(
    id = "all-bundles",
    name = "All Bundles",
    label = None,
    version = ocsVersion.value.toString,
    configs = List(
      AppConfig(
        id = "common",
        distribution = Nil,
        args = Nil,
        vmargs = Nil,
        props = Map.empty,
        icon = None,
        log = None,
        script = None,
        bundles = bs
      ) extending Nil
    )
  )
}
