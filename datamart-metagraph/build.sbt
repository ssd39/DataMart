import Dependencies.*
import sbt.*
import sbt.Keys.*
import scala.sys.process.Process




ThisBuild / organization := "com.my"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / evictionErrorLevel := Level.Warn
ThisBuild / scalafixDependencies += Libraries.organizeImports

ThisBuild / assemblyMergeStrategy := {
  case "logback.xml" => MergeStrategy.first
  case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
  case PathList("com", "my", "buildinfo", xs @ _*) => MergeStrategy.first
  case PathList(xs@_*) if xs.last == "module-info.class" => MergeStrategy.first
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

lazy val commonSettings = Seq(
  scalacOptions ++= List("-Ymacro-annotations", "-Yrangepos", "-Wconf:cat=unused:info", "-language:reflectiveCalls"),
  resolvers += Resolver.mavenLocal,
  resolvers += Resolver.githubPackages("abankowski", "http-request-signer"),
) ++ Defaults.itSettings

lazy val commonLibraryDependencies: Seq[ModuleID] = Seq(
  CompilerPlugin.kindProjector,
  CompilerPlugin.betterMonadicFor,
  CompilerPlugin.semanticDB,
  Libraries.tessellationNodeShared,
  Libraries.cats,
  Libraries.catsEffect,
  Libraries.pureconfigCore,
  Libraries.pureconfigCats,
  Libraries.requests,
  "com.networknt" % "json-schema-validator" % "1.0.53",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.70"
)

lazy val commonTestSettings = Seq(
  testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
  libraryDependencies ++= Seq(
    Libraries.weaverCats,
    Libraries.weaverDiscipline,
    Libraries.weaverScalaCheck,
    Libraries.catsEffectTestkit
  ).map(_ % Test)
)

lazy val buildInfoSettings = Seq(
  buildInfoKeys := Seq[BuildInfoKey](
    name,
    version,
    scalaVersion,
    sbtVersion
  ),
  buildInfoPackage := "com.my.buildinfo"
)

lazy val root = (project in file("."))
  .settings(
    name := "datamart"
  ).aggregate(sharedData, currencyL0, currencyL1, dataL1)

lazy val sharedData = (project in file("modules/shared-data"))
  .enablePlugins(AshScriptPlugin, BuildInfoPlugin, JavaAppPackaging)
  .settings(
    buildInfoSettings,
    commonSettings,
    commonTestSettings,
    name := "datamart-shared-data",
    libraryDependencies ++= commonLibraryDependencies
  )

lazy val currencyL0 = (project in file("modules/l0"))
  .enablePlugins(AshScriptPlugin, BuildInfoPlugin, JavaAppPackaging)
  .dependsOn(sharedData)
  .settings(
    buildInfoSettings,
    commonSettings,
    commonTestSettings,
    name := "datamart-currency-l0",
    libraryDependencies ++= (commonLibraryDependencies ++ Seq(Libraries.tessellationCurrencyL0))
  )

lazy val currencyL1 = (project in file("modules/l1"))
  .enablePlugins(AshScriptPlugin, BuildInfoPlugin, JavaAppPackaging)
  .dependsOn(sharedData)
  .settings(
    buildInfoSettings,
    commonSettings,
    commonTestSettings,
    name := "datamart-currency-l1",
    libraryDependencies ++= (commonLibraryDependencies ++ Seq(Libraries.tessellationCurrencyL1))
  )

lazy val dataL1 = (project in file("modules/data_l1"))
  .enablePlugins(AshScriptPlugin, BuildInfoPlugin, JavaAppPackaging)
  .dependsOn(sharedData)
  .settings(
    buildInfoSettings,
    commonSettings,
    commonTestSettings,
    name := "datamart-data-l1",
    libraryDependencies ++= (commonLibraryDependencies ++ Seq(Libraries.tessellationCurrencyL1))
  )
