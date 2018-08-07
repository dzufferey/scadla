package scadla.backends.amf

import scadla._
import scala.util.parsing.combinator._
import dzufferey.utils._
import dzufferey.utils.LogLevel._
import scala.xml._
import squants.space.{LengthUnit, Millimeters, Microns, Meters, Inches}

object Parser {
  
  def apply(fileName: String): Polyhedron = {
    val amf = XML.loadFile(fileName)
    val unit: LengthUnit = amf \@ "unit" match {
      case "millimeter" | "" | null  => Millimeters
      case "meter" => Meters
      case "micrometer" => Microns
      case "inch" => Inches
      case other => Logger.logAndThrow("amf.Parser", Error, "unkown unit: " + other)
    }
    def parseVertex(v: Node): Point = {
      val coord = v \ "coordinates"
      val x = (coord \ "x").text.toDouble
      val y = (coord \ "y").text.toDouble
      val z = (coord \ "z").text.toDouble
      Point(unit(x),unit(y),unit(z))
    }
    def parseFace(triangle: Node): (Int, Int, Int) = {
      val a = (triangle \ "v1").text.toInt
      val b = (triangle \ "v2").text.toInt
      val c = (triangle \ "v3").text.toInt
      (a, b, c)
    }
    val meshes = amf \ "object" \ "mesh"
    if (meshes.size == 0) {
      Logger.logAndThrow("amf.Parser", Error, "no mesh found")
    }
    if (meshes.size > 1) {
      Logger("amf.Parser", Warning, "more than one mesh. taking only the first.")
    }
    val mesh = meshes.head
    val vertex = (mesh \ "vertices" \ "vertex").map(parseVertex)
    val faces = (mesh \ "volume" \ "triangle").map( t =>
      parseFace(t) match {
        case (a,b,c) => Face(vertex(a), vertex(b), vertex(c))
      })
    Polyhedron(faces)
  }

}
