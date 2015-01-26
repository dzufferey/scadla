name := "scadla"

organization := "io.github.dzufferey"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "io.github.dzufferey" %% "misc-scala-utils" % "0.1-SNAPSHOT"
)

resolvers +=  "dzufferey maven repo" at "https://github.com/dzufferey/my_mvn_repo/raw/master/repository"

publishMavenStyle := true

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

pomExtra :=
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
    </license>
  </licenses>

