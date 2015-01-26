package dzufferey.scadla.backends

import dzufferey.scadla._
import scala.util.parsing.combinator._
import dzufferey.utils._
import dzufferey.utils.LogLevel._
import java.io._


object AsciiStlParser extends JavaTokenParsers {
  
  def parseVertex: Parser[Point] =
    "vertex" ~> repN(3, floatingPointNumber) ^^ {
      case List(a, b,c) => Point(a.toDouble, b.toDouble, c.toDouble)
    }

  def parseFacet: Parser[Face] =
    "facet" ~> "normal" ~> repN(3, floatingPointNumber) ~> "outer" ~> "loop" ~> repN(3, parseVertex) <~ "endloop" <~ "endfacet" ^^ {
      case List(a, b,c) => Face(a, b, c)
    }

  def parseSolid: Parser[Polyhedron] =
    "solid" ~> opt(ident) ~> rep(parseFacet) <~ "endsolid" <~ opt(ident) ^^ ( lst => Polyhedron(lst) )

  def apply(reader: java.io.Reader): Polyhedron = {
    val result = parseAll(parseSolid, reader)
    if (result.successful) {
      result.get
    } else {
      Logger.logAndThrow("AsciiStlParser", dzufferey.utils.LogLevel.Error, "parsing error: " + result.toString)
    }
  }


  def apply(fileName: String): Polyhedron = {
    val reader = new BufferedReader(new FileReader(fileName))
    apply(reader)
  }

}

object BinaryStlParser {

  import java.nio.file.FileSystems
  import java.nio.channels.FileChannel
  import java.nio.ByteBuffer

  protected def point(buffer: ByteBuffer) = {
    val p1 = buffer.getFloat
    val p2 = buffer.getFloat
    val p3 = buffer.getFloat
    Point(p1, p2, p3)
  }

  def apply(fileName: String) = {
    val path = FileSystems.getDefault.getPath(fileName)
    val file = FileChannel.open(path)
    val buffer = file.map(FileChannel.MapMode.READ_ONLY, 0, file.size)
    buffer.order(java.nio.ByteOrder.LITTLE_ENDIAN)
    buffer.position(80) //skip the header
    val nbrTriangles = buffer.getInt
    val triangles = for (_ <- 0 until nbrTriangles) yield {
      buffer.position(buffer.position + 9) //skip normal
      val p1 = point(buffer)
      val p2 = point(buffer)
      val p3 = point(buffer)
      buffer.position(buffer.position + 2) //skip attributes
      Face(p1, p2, p3)
    }
    Polyhedron(triangles)       
  }

}

object StlParser {

  val txtHeader = "solid"
  val bytesHeader = txtHeader.getBytes("US-ASCII")
  val headerSize = bytesHeader.size

  protected def isTxt(fileName: String) = {
    val stream = new FileInputStream(fileName)
    val b = Array.ofDim[Byte](headerSize)
    stream.read(b)
    (0 until headerSize).forall( i => b(i) == bytesHeader(i) )
  }

  def apply(fileName: String): Polyhedron = {
    if (isTxt(fileName)) {
      AsciiStlParser(fileName)
    } else {
      BinaryStlParser(fileName)
    }
  }

}
