package scadla.backends.x3d

import scadla._
import java.io._
import java.util.Date
import squants.space.{Length, Millimeters, LengthUnit}

//TODO make parametric in terms of unit

object Printer extends Printer(Millimeters) {
}

class Printer(unit: LengthUnit = Millimeters) {

  def write(obj: Polyhedron, writer: BufferedWriter,
            onlyShape: Boolean = false, //or add scene
            withHeader: Boolean = true, //full xml document
            name: String = "scadla object"): Unit = {
    assert(!onlyShape || !withHeader, "only shape cannot print header")
    def writeLine(s: String): Unit = {
      writer.write(s)
      writer.newLine
    }
    val (points, faces) = obj.indexed
    if (withHeader) {
      writeLine("<?xml version='1.0' encoding='UTF-8'?>")
      writeLine("<!DOCTYPE X3D PUBLIC 'ISO//Web3D//DTD X3D 3.3//EN' 'http://www.web3d.org/specifications/x3d-3.3.dtd'>")
      writeLine("<X3D profile='Interchange' version='3.3' xmlns:xsd='http://www.w3.org/2001/XMLSchema-instance' xsd:noNamespaceSchemaLocation='http://www.web3d.org/specifications/x3d-3.3.xsd'>")
      writeLine("  <head>")
      writeLine("    <meta content='"+name+"' name='title'>")
      writeLine("    <meta content='"+ (new Date()).toString +"' name='created'>")
      writeLine("  </head>")
    } else if (!onlyShape) {
      writeLine("<X3D id='scadlaModel'>")
      writeLine("  <Scene>")
      writeLine("    <Shape>")
    } else {
      writeLine("    <Shape id='scadlaModel'>")
    }
    writeLine("      <Appearance>")
    writeLine("        <Material diffuseColor='0.7 0.7 0.7'/>")
    writeLine("      </Appearance>")
    writer.write("      <IndexedFaceSet coordIndex='")
    faces.foreach{ case (a,b,c) =>
      writer.write(a.toString)
      writer.write(" ")
      writer.write(b.toString)
      writer.write(" ")
      writer.write(c.toString)
      writer.write(" -1 ")
    }
    writeLine("'>")
    writer.write("        <Coordinate point='")
    points.foreach{ p =>
      writer.write(p.x.to(unit).toString)
      writer.write(" ")
      writer.write(p.y.to(unit).toString)
      writer.write(" ")
      writer.write(p.z.to(unit).toString)
      writer.write(" ")
    }
    writeLine("'></Coordinate>")
    writeLine("      </IndexedFaceSet>")
    writeLine("    </Shape>")
    if (!onlyShape) {
      writeLine("  </Scene>")
      writeLine("</X3D>")
    }
  }
  
  def store(obj: Polyhedron, fileName: String) = {
    val writer = new BufferedWriter(new FileWriter(fileName))
    try write(obj, writer)
    finally writer.close
  }

}
