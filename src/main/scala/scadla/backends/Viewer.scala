package scadla.backends

import scadla._
import dzufferey.utils.SysCmd
import java.io._

trait Viewer {

  def apply(obj: Polyhedron): Unit
  
}

object Viewer {

  def default: Viewer = {
    if (MeshLab.isPresent) MeshLab
    else if (x3d.X3DomViewer.isPresent) x3d.X3DomViewer
    else sys.error("No viewer available")
  }

}
