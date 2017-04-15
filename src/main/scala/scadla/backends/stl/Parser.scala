package scadla.backends.stl

import scadla._
import scala.util.parsing.combinator._
import dzufferey.utils._
import dzufferey.utils.LogLevel._
import java.io._
import squants.space.Millimeters
import squants.space.SquareMeters


object AsciiParser extends JavaTokenParsers {
  
  def parseVertex: Parser[Point] =
    "vertex" ~> repN(3, floatingPointNumber) ^^ {
      case List(a, b,c) => Point(Millimeters(a.toDouble), Millimeters(b.toDouble), Millimeters(c.toDouble))
    }

  def parseFacet: Parser[Face] =
    ("facet" ~> "normal" ~> repN(3, floatingPointNumber)) ~ ("outer" ~> "loop" ~> repN(3, parseVertex) <~ "endloop" <~ "endfacet") ^^ {
      case List(nx, ny, nz) ~ List(a, b, c) =>
        val n = Vector(nx.toDouble, ny.toDouble, nz.toDouble, Millimeters)
        val f = Face(a, b, c)
        scadla.backends.stl.Parser.checkNormal(f, n)
    }

  def parseSolid: Parser[Polyhedron] =
    "solid" ~> opt(ident) ~> rep(parseFacet) <~ "endsolid" <~ opt(ident) ^^ ( lst => Polyhedron(lst) )

  def apply(reader: java.io.Reader): Polyhedron = {
    val result = parseAll(parseSolid, reader)
    if (result.successful) {
      result.get
    } else {
      Logger.logAndThrow("AsciiParser", dzufferey.utils.LogLevel.Error, "parsing error: " + result.toString)
    }
  }


  def apply(fileName: String): Polyhedron = {
    val reader = new BufferedReader(new FileReader(fileName))
    apply(reader)
  }

}

object BinaryParser {

  import java.nio.file.FileSystems
  import java.nio.channels.FileChannel
  import java.nio.ByteBuffer

  protected def vector(buffer: ByteBuffer) = {
    val p1 = buffer.getFloat
    val p2 = buffer.getFloat
    val p3 = buffer.getFloat
    Vector(p1, p2, p3, Millimeters)
  }

  protected def point(buffer: ByteBuffer) = {
    val p1 = buffer.getFloat
    val p2 = buffer.getFloat
    val p3 = buffer.getFloat
    Point(Millimeters(p1), Millimeters(p2), Millimeters(p3))
  }

  def apply(fileName: String) = {
    val path = FileSystems.getDefault.getPath(fileName)
    val file = FileChannel.open(path)
    val buffer = file.map(FileChannel.MapMode.READ_ONLY, 0, file.size)
    buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
    buffer.position(80) //skip the header
    val nbrTriangles = buffer.getInt
    val triangles = for (_ <- 0 until nbrTriangles) yield {
      val n = vector(buffer)
      val p1 = point(buffer)
      val p2 = point(buffer)
      val p3 = point(buffer)
      buffer.position(buffer.position + 2) //skip attributes
      Parser.checkNormal(Face(p1, p2, p3), n)
    }
    Polyhedron(triangles)       
  }

}

object Parser {

  val txtHeader = "solid"
  val bytesHeader = txtHeader.getBytes("US-ASCII")
  val headerSize = bytesHeader.size

  def checkNormal(f: Face, n: Vector): Face = {
    if (n.dot(f.normal) < SquareMeters(1e-16)) f.flipOrientation
    else f
  }

  protected def isTxt(fileName: String) = {
    val stream = new FileInputStream(fileName)
    val b = Array.ofDim[Byte](headerSize)
    stream.read(b)
    (0 until headerSize).forall( i => b(i) == bytesHeader(i) )
  }

  def apply(fileName: String): Polyhedron = {
    if (isTxt(fileName)) {
      AsciiParser(fileName)
    } else {
      BinaryParser(fileName)
    }
  }

}
