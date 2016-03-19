package scadla.backends.amf

import scadla._
import dzufferey.utils._
import dzufferey.utils.LogLevel._
import scala.xml._

object Printer {
  
  def store(obj: Polyhedron, fileName: String) = {
    val (points, faces) = obj.indexed
    val pointNodes =
      new Group(points.map{ case Point(x,y,z) =>
        <vertex><coordinates><x>{x}</x><y>{y}</y><z>{z}</z></coordinates></vertex>
      })
    val faceNodes =
      new Group(faces.map{ case (a,b,c) =>
        <triangle><v1>{a}</v1><v2>{b}</v2><v3>{c}</v3></triangle>
      }.toSeq)
    val node =
      <amf unit="milimeter">
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
