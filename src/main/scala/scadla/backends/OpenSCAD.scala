package scadla.backends

import scadla.*
import scadla.utils.fold
import dzufferey.utils.SysCmd
import java.io.*
import squants.space.{Length, Millimeters, LengthUnit}

class OpenSCAD(header: List[String], unit: LengthUnit = Millimeters) extends Renderer(unit) {

  protected val command = "openscad"

  lazy val isPresent =
    try SysCmd(Array(command, "-v"))._1 == 0
    catch { case _: Throwable => false }

  override def isSupported(s: Solid): Boolean = s match {
    case s: Shape => super.isSupported(s)
    case t: Transform => isSupported(t.child)
    case u @ Union(_) => u.children.forall(isSupported)
    case i @ Intersection(_) => i.children.forall(isSupported)
    case d @ Difference(_,_) => d.children.forall(isSupported)
    case c @ Hull(_) => c.children.forall(isSupported)
    case m @ Minkowski(_) => m.children.forall(isSupported)
    case _ => false
  }
  protected def getMultiplicity(s: Solid): Map[Solid, Int] = {
    def incr(map: Map[Solid, Int], s: Solid) = {
      val mult = map.getOrElse(s, 0) + 1
      map + (s -> mult)
    }
    fold(incr, Map[Solid, Int](), s)
  }

  protected def decreaseMultiplicity(map: Map[Solid, Int], s: Solid, n: Int): Map[Solid, Int] = {
    def decr(map: Map[Solid, Int], s2: Solid) = {
      val mult = map(s2) - n
      //TODO get to -1, something funny is going on ...
      //assert(mult >= 0, "new mult = " + mult + " should be ≥ 0\nn = " + n + "\ns2 = "+s2+"\ns = "+s+").")
      map + (s2 -> mult)
    }
    fold(decr, map, s)
  }

  protected def printWithModules(obj: Solid, writer: BufferedWriter): Unit = {
    var mult = getMultiplicity(obj)
    //println(mult.mkString("\n"))
    //utils.traverse( s => mult(s) > 0, obj)
    var modules = Map[Solid, String]()
    var cnt = 0
    def prnt(obj: Solid, indent: Int): Unit = {
      if (modules.contains(obj)) {
        spaces(indent)(writer)
        writer.write(modules(obj) + "();")
        writer.newLine
      } else if (mult(obj) > 1) {
        val name = "m_" + cnt
        cnt += 1
        mult = decreaseMultiplicity(mult, obj, mult(obj) - 1)
        modules += (obj -> name)
        spaces(indent)(writer)
        writer.write(name + "();")
        writer.newLine
      } else {
        prnt2(obj, indent)
      }
    }
    def prnt2(obj: Solid, indent: Int): Unit = {
      spaces(indent)(writer)
      obj match {
        case Empty =>
          writer.newLine
        case Cube(width, depth, height) =>
          writer.write("cube([ " + length2Double(width) + ", " + length2Double(depth) + ", " + length2Double(height) + "]);")
          writer.newLine
        case Sphere(radius) =>
          writer.write("sphere( " + length2Double(radius) + ");")
          writer.newLine
        case Cylinder(radiusBot, radiusTop, height) =>
          writer.write("cylinder( r1 = " + length2Double(radiusBot) + ", r2 = " + length2Double(radiusTop) + ", h = " + length2Double(height) + ");")
          writer.newLine
        case p @ Polyhedron(_) =>
          val (indexedP,indexedF) = p.indexed
          writer.write("polyhedron( points=[ ")
          writer.write(indexedP.map(p => "["+ length2Double(p.x) +","+ length2Double(p.y) +","+ length2Double(p.z) +"]").mkString(", "))
          writer.write(" ], faces=[ ")
          writer.write(indexedF.map{ case (a,b,c) => "["+a+","+b+","+c+"]" }.mkString(", "))
          writer.write(" ]);")
          writer.newLine
        case FromFile(path, format) =>
          writer.write("import(\"")
          writer.write(path)
          writer.write("\");")
          writer.newLine
        //operations
        case Union(objs @ _*) =>
          writer.write("union(){")
          writer.newLine
          objs.foreach(prnt(_, indent+2))
          spaces(indent)(writer)
          writer.write("}")
          writer.newLine
        case Intersection(objs @ _*) =>
          writer.write("intersection(){")
          writer.newLine
          objs.foreach(prnt(_, indent+2))
          spaces(indent)(writer)
          writer.write("}")
          writer.newLine
        case Difference(pos, negs @ _*) =>
          writer.write("difference(){")
          writer.newLine
          prnt(pos, indent+2)
          negs.foreach(prnt(_, indent+2))
          spaces(indent)(writer)
          writer.write("}")
          writer.newLine
        case Minkowski(objs @ _*) =>
          writer.write("minkowski(){")
          writer.newLine
          objs.foreach(prnt(_, indent+2))
          spaces(indent)(writer)
          writer.write("}")
          writer.newLine
        case Hull(objs @ _*) =>
          writer.write("hull(){")
          writer.newLine
          objs.foreach(prnt(_, indent+2))
          spaces(indent)(writer)
          writer.write("}")
          writer.newLine
        //transforms
        case Scale(x, y, z, obj) =>
          writer.write("scale(["+x+","+y+","+z+"])")
          writer.newLine
          prnt(obj, indent+2)
        case Rotate(x, y, z, obj) =>
          writer.write("rotate(["+x.toDegrees+","+y.toDegrees+","+z.toDegrees+"])")
          writer.newLine
          prnt(obj, indent+2)
        case Translate(x, y, z, obj) =>
          writer.write("translate(["+length2Double(x)+","+length2Double(y)+","+length2Double(z)+"])")
          writer.newLine
          prnt(obj, indent+2)
        case Mirror(x, y, z, obj) =>
          writer.write("mirror(["+x+","+y+","+z+"])")
          writer.newLine
          prnt(obj, indent+2)
        case Multiply(m, obj) =>
          writer.write("multmatrix([["+m.m00+","+m.m01+","+m.m02+","+m.m03+"],["+m.m10+","+m.m11+","+m.m12+","+m.m13+"],["+m.m20+","+m.m21+","+m.m22+","+m.m23+"],["+m.m30+","+m.m31+","+m.m32+","+m.m33+"],])")
          writer.newLine
          prnt(obj, indent+2)
      }
    }
    assert(mult(obj) == 1)
    prnt(obj, 0)
    def printModules(printed: Set[String]): Unit = {
      modules.find{ case (_, name) => !printed(name) } match {
        case Some((obj, name)) =>
          writer.newLine // linter:ignore IdenticalStatements
          writer.newLine
          writer.write("module " + name + "() {")
          writer.newLine
          prnt2(obj, 2)
          writer.write("}")
          writer.newLine
          printModules(printed + name)
        case None =>
      }
    }
    printModules(Set[String]())
  }

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

  def print(obj: Solid, writer: BufferedWriter): Unit = {
    for (h <- header) {
      writer.write(h)
      writer.newLine
    }
    printWithModules(obj, writer)
    writer.flush
  }

  protected def writeInFile(file: java.io.File, obj: Solid) = {
    val fw = new PrintWriter(file)
    val writer = new BufferedWriter(fw)
    print(obj, writer)
    writer.close
    fw.close
  }

  protected def toTmpFile(obj: Solid) = {
    val tmpFile = java.io.File.createTempFile("scadlaModel", ".scad")
    writeInFile(tmpFile, obj)
    tmpFile
  }

  def saveFile(fileName: String, obj: Solid) = {
    val file = new java.io.File(fileName)
    writeInFile(file, obj)
  }

  override def toSTL(obj: Solid, outputFile: String): Unit = {
    toSTL(obj, outputFile, Nil)
  }

  def toSTL(obj: Solid, outputFile: String, options: Iterable[String]) = {
    val tmpFile = toTmpFile(obj)
    val cmd = Array(command, tmpFile.getPath, "-o", outputFile) ++ options
    val res = SysCmd(cmd)
    tmpFile.delete
    res
  }

  def view(obj: Solid, optionsRender: Iterable[String]) = {
    val tmpFile = java.io.File.createTempFile("scadlaModel", ".stl")
    toSTL(obj, tmpFile.getPath, optionsRender)
    val res = MeshLab(tmpFile)
    tmpFile.delete
    res
  }

  override def view(obj: Solid) = {
    view(obj, Nil)
  }

  def runOpenSCAD(obj: Solid, options: Iterable[String] = Nil) = {
    val tmpFile = toTmpFile(obj)
    val cmd = Array(command, tmpFile.getPath) ++ options
    val res = SysCmd(cmd)
    tmpFile.delete
    res
  }

  def getResult(obj: Solid, options: Iterable[String] = Nil) = {
    val tmpFile = java.io.File.createTempFile("scadlaModel", ".stl")
    toSTL(obj, tmpFile.getPath, options)
    val parsed = stl.Parser(tmpFile.getPath) //TODO makes it parametric in the Length unit
    tmpFile.delete
    parsed
  }

  def apply(obj: Solid): Polyhedron = obj match {
    case p @ Polyhedron(_) => p
    case other => getResult(other)
  }

}

object OpenSCAD extends OpenSCAD(List("$fa=4;", "$fs=0.5;"), Millimeters) {

}

object OpenSCADnightly extends OpenSCAD(List("$fa=4;", "$fs=0.5;"), Millimeters) {

  override protected val command = "openscad-nightly"

}
