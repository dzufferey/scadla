name := "scadla"

organization := "com.github.dzufferey"

version := "0.1.0"

scalaVersion := "2.13.4"

scalacOptions in Compile ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature"
)

resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.0",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
  "org.scalatest" %% "scalatest" % "3.2.2" % "test",
  "com.github.dzufferey" %% "misc-scala-utils" % "1.0.0",
  "eu.mihosoft.vrl.jcsg" % "jcsg" % "0.5.7",
  "org.typelevel"  %% "squants"  % "1.7.0"
)

fork := true
