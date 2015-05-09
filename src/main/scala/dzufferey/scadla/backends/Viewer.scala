
package dzufferey.scadla.backends

import dzufferey.scadla._
import dzufferey.utils.SysCmd
import java.io._

trait Viewer {

  def apply(obj: Polyhedron): Unit
  
}

object Viewer {

  //TODO configurable
  def default = MeshLab

}
