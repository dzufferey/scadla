package scadla.backends

import scadla.*
import dzufferey.utils.SysCmd
import java.io.*

object MeshLab extends Viewer {

  def apply(stl: String, options: Iterable[String] = Nil): SysCmd.ExecResult = {
    SysCmd( Array("meshlab", stl) ++ options )
  }

  def apply(file: File): SysCmd.ExecResult = apply(file.getPath)

  def apply(obj: Polyhedron): Unit = {
    val tmpFile = java.io.File.createTempFile("scadlaModel", ".stl")
    stl.Printer.storeBinary(obj, tmpFile.getPath)
    apply(tmpFile)
    tmpFile.delete
  }

  lazy val isPresent = {
    val isWindows = java.lang.System.getProperty("os.name").toLowerCase().contains("windows")
    val cmd = if (isWindows) "where" else "which"
    SysCmd(Array(cmd, "meshlab"))._1 == 0
  }

}
