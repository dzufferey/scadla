name := "scadla"

organization := "com.github.dzufferey"

version := "0.1.2-SNAPSHOT"

scalaVersion := "3.3.7"

scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature"
)

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.4.0",
  "org.scala-lang.modules" %% "scala-xml" % "2.4.0",
  "org.scalatest" %% "scalatest" % "3.2.19" % "test",
  "com.github.dzufferey" %% "misc-scala-utils" % "1.1.1-SNAPSHOT",
  "com.github.dzufferey" %% "almond-x3dom-model-viewer" % "0.2.4-SNAPSHOT",
  "eu.mihosoft.vrl.jcsg" % "jcsg" % "0.5.7",
  "org.typelevel"  %% "squants"  % "1.8.3"
)

fork := true
