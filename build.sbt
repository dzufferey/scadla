name := "scadla"

organization := "com.github.dzufferey"

version := "0.1.1"

scalaVersion := "2.13.6"

scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature"
)

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.3",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.0.0",
  "org.scala-lang.modules" %% "scala-xml" % "2.0.1",
  "org.scalatest" %% "scalatest" % "3.2.5" % "test",
  "com.github.dzufferey" %% "misc-scala-utils" % "1.0.0",
  "com.github.dzufferey" %% "almond-x3dom-model-viewer" % "0.2.3",
  "eu.mihosoft.vrl.jcsg" % "jcsg" % "0.5.7",
  "org.typelevel"  %% "squants"  % "1.8.1"
)

fork := true
