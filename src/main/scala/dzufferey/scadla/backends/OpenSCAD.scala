package dzufferey.scadla.backends

import dzufferey.scadla._
import dzufferey.utils.SysCmd
import java.io._

object OpenSCAD {

   //TODO create "cache" methods to factor the CGG tree: common subexpression elimination
  
   protected def spaces(n: Int)(implicit writer: BufferedWriter): Unit = n match {
     case 0 => ()
     case 1 => writer write " ";
     case 2 => writer write "  ";
     case 3 => writer write "   ";
     case 4 => writer write "    ";
     case 5 => writer write "     ";
     case 6 => writer write "      ";
     case 7 => writer write "       ";
     case _ =>
     assert(n >= 8)
     writer write "        ";
     spaces(n - 8)
   }


  protected def print(obj: Solid, indent: Int)(implicit writer: BufferedWriter): Unit = obj match {
    case Empty =>
      spaces(indent)
      writer.newLine
    case Cube(width, depth, height) =>
      spaces(indent)
      writer.write("cube([ " + width + ", " + depth + ", " + height + "]);")
      writer.newLine
    case Sphere(radius) =>
      spaces(indent)
      writer.write("sphere( " + radius + ");")
      writer.newLine
    case Cylinder(radiusBot, radiusTop, height) =>
      spaces(indent)
      writer.write("cylinder( r1 = " + radiusBot + ", r2 = " + radiusTop + ", h = " + height + ");")
      writer.newLine
    case Polyhedron(triangles) =>
      val points = triangles.foldLeft(Set[Point]())( (acc, face) => acc + face.p1 + face.p2 + face.p3 )
      val indexed = points.toSeq.zipWithIndex
      val idx: Map[Point, Int] = indexed.toMap
      spaces(indent)
      writer.write("polyhedron( points=[ ")
      writer.write(indexed.map{ case (Point(x,y,z), _) => "["+x+","+y+","+z+"]" }.mkString(", "))
      writer.write(" ], faces=[ ")
      writer.write(triangles.map{ case Face(a,b,c) => "["+idx(a)+","+idx(b)+","+idx(c)+"]" }.mkString(", "))
      writer.write(" ]);")
      writer.newLine
    case FromFile(path, format) =>
      spaces(indent)
      writer.write("import(\"")
      writer.write(path)
      writer.write("\");")
      writer.newLine
    //operations
    case Union(objs @ _*) =>
      spaces(indent)
      writer.write("union(){")
      writer.newLine
      objs.foreach(print(_, indent+2))
      writer.write("}")
      writer.newLine
    case Intersection(objs @ _*) =>
      spaces(indent)
      writer.write("intersection(){")
      writer.newLine
      objs.foreach(print(_, indent+2))
      writer.write("}")
      writer.newLine
    case Difference(pos, negs @ _*) =>
      spaces(indent)
      writer.write("difference(){")
      writer.newLine
      print(pos, indent+2)
      negs.foreach(print(_, indent+2))
      writer.write("}")
      writer.newLine
    case Minkowski(objs @ _*) =>
      spaces(indent)
      writer.write("minkowski(){")
      writer.newLine
      objs.foreach(print(_, indent+2))
      writer.write("}")
      writer.newLine
    case Hull(objs @ _*) =>
      spaces(indent)
      writer.write("hull(){")
      writer.newLine
      objs.foreach(print(_, indent+2))
      writer.write("}")
      writer.newLine
    //transforms
    case Scale(x, y, z, obj) =>
      spaces(indent)
      writer.write("scale(["+x+","+y+","+z+"])")
      writer.newLine
      print(obj, indent+2)
    case Rotate(x, y, z, obj) =>
      spaces(indent)
      writer.write("rotate(["+math.toDegrees(x)+","+math.toDegrees(y)+","+math.toDegrees(z)+"])")
      writer.newLine
      print(obj, indent+2)
    case Translate(x, y, z, obj) =>
      spaces(indent)
      writer.write("translate(["+x+","+y+","+z+"])")
      writer.newLine
      print(obj, indent+2)
    case Mirror(x, y, z, obj) =>
      spaces(indent)
      writer.write("mirror(["+x+","+y+","+z+"])")
      writer.newLine
      print(obj, indent+2)
    case Multiply(m, obj) =>
      spaces(indent)
      writer.write("multmatrix([["+m.m00+","+m.m01+","+m.m02+","+m.m03+"],["+m.m10+","+m.m11+","+m.m12+","+m.m13+"],["+m.m20+","+m.m21+","+m.m22+","+m.m23+"],["+m.m30+","+m.m31+","+m.m32+","+m.m33+"],])")
      writer.newLine
      print(obj, indent+2)
  }
  
  def print(obj: Solid, writer: BufferedWriter, header: Iterable[String] = Nil) {
    for (h <- header) {
      writer.write(h)
      writer.newLine
    }
    print(obj, 0)(writer)
  }
  
  val defaultHeader = List("$fa=4;", "$fs=0.5;")

  protected def toTmpFile(obj: Solid, header: Iterable[String]) = {
    val tmpFile = java.io.File.createTempFile("scadlaModel", ".scad")
    val writer = new BufferedWriter(new PrintWriter(tmpFile))
    print(obj, writer, header)
    writer.close
    tmpFile
  }

  def toSTL(obj: Solid, outputFile: String, header: Iterable[String] = defaultHeader, options: Iterable[String] = Nil) = {
    val tmpFile = toTmpFile(obj, header)
    val cmd = Array("openscad", tmpFile.getPath, "-o", outputFile) ++ options
    val res = SysCmd(cmd)
    tmpFile.delete
    res
  }

  def view(obj: Solid, header: Iterable[String] = defaultHeader, optionsRender: Iterable[String] = Nil, optionsView: Iterable[String] = Nil) = {
    val tmpFile = java.io.File.createTempFile("scadlaModel", ".stl")
    toSTL(obj, tmpFile.getPath, header, optionsRender)
    val cmd = Array("meshlab", tmpFile.getPath) ++ optionsView
    val res = SysCmd(cmd)
    tmpFile.delete
    res
  }

  def runOpenSCAD(obj: Solid, header: Iterable[String] = defaultHeader, options: Iterable[String] = Nil) = {
    val tmpFile = toTmpFile(obj, header)
    val cmd = Array("openscad", tmpFile.getPath) ++ options
    val res = SysCmd(cmd)
    tmpFile.delete
    res
  }

  def getResult(obj: Solid, outputFile: String, header: Iterable[String] = defaultHeader, options: Iterable[String] = Nil) = {
    val tmpFile = java.io.File.createTempFile("scadlaModel", ".stl")
    toSTL(obj, tmpFile.getPath, header, options)
    val parsed = StlParser(tmpFile.getPath)
    tmpFile.delete
    parsed
  }

}
