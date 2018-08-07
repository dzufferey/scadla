package scadla.backends.amf

import scadla._
import dzufferey.utils._
import dzufferey.utils.LogLevel._
import scala.xml._
import squants.space.{LengthUnit, Millimeters, Microns, Meters, Inches}

object Printer extends Printer(Millimeters) {
}

class Printer(unit: LengthUnit = Millimeters) {

  val targetUnit: String = unit match {
    case Millimeters => "millimeter"
    case Meters => "meter"
    case Microns => "micrometer"
    case Inches => "inch"
    case other => Logger.logAndThrow("amf.Printer", Error, "unsupported unit: " + other)
  }
  
  def store(obj: Polyhedron, fileName: String) = {
    val (points, faces) = obj.indexed
    val pointNodes =
      new Group(points.map{ p =>
        <vertex><coordinates><x>{p.x.to(unit)}</x><y>{p.y.to(unit)}</y><z>{p.z.to(unit)}</z></coordinates></vertex>
      })
    val faceNodes =
      new Group(faces.map{ case (a,b,c) =>
        <triangle><v1>{a}</v1><v2>{b}</v2><v3>{c}</v3></triangle>
      }.toSeq)
    val node =
      <amf unit={ targetUnit }>
        <metadata type="producer">Scadla</metadata>
        <object id="0">
          <mesh>
            <vertices>
              { pointNodes }
            </vertices>
            <volume>
              { faceNodes }
            </volume>
          </mesh>
        </object>
      </amf>
    XML.save(fileName, node, "UTF-8", true)
  }

}
