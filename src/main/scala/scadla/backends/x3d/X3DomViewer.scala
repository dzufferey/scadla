package scadla.backends.x3d

import scadla._
import scadla.backends.Viewer
import dzufferey.utils.SysCmd
import java.io._
import java.awt.Desktop

object X3DomViewer extends Viewer {

  //TODO change the default camera position

  // give enough time for the browser to start and load the file (in second)
  var sleepTime = 5

  val prefix = """<!DOCTYPE html>
                 |<html>
                 |<head>
                 |    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
                 |    <title>test</title>
                 |    <script type='text/javascript' src='https://www.x3dom.org/download/x3dom.js'> </script>
                 |    <link rel='stylesheet' type='text/css' href='https://www.x3dom.org/download/x3dom.css'>
                 |    <style>
                 |        x3d
                 |        {
                 |            width: 98vw;
                 |            height: 98vh;
                 |            background: rgba(64, 64, 196, 0.4);
                 |        }
                 |    </style>
                 |</head>
                 |<body>""".stripMargin

  val suffix = """</body>
                 |</html>""".stripMargin

  //the files are cleaned only when the JVM terminate
  def apply(obj: Polyhedron) {
    val tmpFile = java.io.File.createTempFile("scadlaModel", ".html")
    tmpFile.deleteOnExit
    val writer = new BufferedWriter(new FileWriter(tmpFile))
    writer.write(prefix)
    writer.newLine
    Printer.write(obj, writer, withHeader = false)
    writer.write(suffix)
    writer.newLine
    writer.close
    Desktop.getDesktop().open(tmpFile)
    Thread.sleep(sleepTime * 1000)
  }
  
  lazy val isPresent = {
    Desktop.isDesktopSupported && Desktop.getDesktop.isSupported(Desktop.Action.OPEN)
  }
  
}
