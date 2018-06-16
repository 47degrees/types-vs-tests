import sbt.Keys._

val scalaV = "2.12.6"
val simulacrumV = "0.12.0"
val catsEffectV = "1.0.0-RC2"
val textRazorV = "1.0.11"
val scalacheckV = "1.14.0"
val scalaTestV = "3.0.5"
val refinedV = "0.9.0"
val monixV = "3.0.0-RC1"

scalacOptions ++= Seq(
  "-feature",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ypartial-unification",
  "-deprecation:false")

scalaVersion := scalaV

resolvers ++= Seq(
  Resolver.mavenLocal,
  "Artima Maven Repository" at "http://repo.artima.com/releases",
  DefaultMavenRepository,
  "jcenter" at "http://jcenter.bintray.com",
  "47 Degrees Bintray Repo" at "http://dl.bintray.com/47deg/maven",
  Resolver.typesafeRepo("releases"),
  Resolver.typesafeRepo("snapshots"),
  Resolver.typesafeIvyRepo("snapshots"),
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  Resolver.defaultLocal
)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % scalaV,
  "com.github.mpilquist" %% "simulacrum" % simulacrumV,
  "com.textrazor" % "textrazor" % textRazorV,
  "eu.timepit" %% "refined" % refinedV,
  "eu.timepit" %% "refined-cats" % refinedV,
  "eu.timepit" %% "refined-scalacheck" % refinedV,
  "org.typelevel" %% "cats-effect" % catsEffectV,
  "org.scalacheck" %% "scalacheck" % scalacheckV,
  "org.scalatest" %% "scalatest" % scalaTestV,
  "io.monix" %% "monix" % monixV
)

scalacOptions in(Compile, console) ++= Seq(
  "-i", "myrepl.init"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

enablePlugins(TutPlugin)

tutTargetDirectory := file(".")
