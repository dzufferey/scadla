package scadla.backends.almond

import scadla._
import com.github.dzufferey.x3DomViewer.X3D._
import scalatags.Text.all._

object Viewer { // does not extend the normal Viewer class as it needs to return a value

  var conf = new Config

  def apply(obj: Polyhedron) = {
    val (points, faces) = obj.indexed
    val indices = faces.map{ case (a,b,c) => s"$a $b $c -1" }.mkString(" ")
    val coord = points.map( p => s"${p.x.to(conf.unit)} ${p.y.to(conf.unit)} ${p.z.to(conf.unit)}" ).mkString(" ")
    val ifaces = indexedFaceSet(coordIndex := indices.toString, coordinate( point := coord) )
    val content = shape(conf.shapeAppearance, ifaces)
    com.github.dzufferey.x3DomViewer.Viewer.display(content, conf)
  }

}
