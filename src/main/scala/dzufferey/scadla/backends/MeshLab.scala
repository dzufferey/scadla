package dzufferey.scadla.backends

import dzufferey.scadla._
import dzufferey.utils.SysCmd
import java.io._

object MeshLab extends Viewer {

  def apply(stl: String, options: Iterable[String] = Nil): SysCmd.ExecResult = {
    SysCmd( Array("meshlab", stl) ++ options )
  }
  
  def apply(file: File): SysCmd.ExecResult = apply(file.getPath)
  
  def apply(obj: Polyhedron) {
    val tmpFile = java.io.File.createTempFile("scadlaModel", ".stl")
    stl.Printer.storeBinary(obj, tmpFile.getPath)
    apply(tmpFile)
    tmpFile.delete
  }
  
}
