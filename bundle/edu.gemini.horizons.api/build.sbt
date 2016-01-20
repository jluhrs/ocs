import OcsKeys._

// note: inter-project dependencies are declared at the top, in projects.sbt

name := "edu.gemini.horizons.api"

// version set in ThisBuild

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.6",
  "org.scalaz" %% "scalaz-effect" % "7.1.6")

osgiSettings

ocsBundleSettings

OsgiKeys.bundleActivator := Some("edu.gemini.horizons.api.osgi.Activator")

OsgiKeys.bundleSymbolicName := name.value

OsgiKeys.dynamicImportPackage := Seq("")

OsgiKeys.exportPackage := Seq(
  "edu.gemini.horizons.api",
  "edu.gemini.horizons.server.backend"
)
