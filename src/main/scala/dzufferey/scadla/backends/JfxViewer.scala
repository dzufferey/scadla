package dzufferey.scadla.backends

import dzufferey.scadla._
import dzufferey.scadla.assembly._
import dzufferey.utils.SysCmd

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.beans.property.DoubleProperty
import scalafx.scene.image.Image
import scalafx.scene.input.{MouseEvent,ScrollEvent}
import scalafx.scene.paint.{Color, PhongMaterial}
import scalafx.scene.shape.{MeshView, TriangleMesh}
import scalafx.scene.transform.{Rotate,Scale}
import scalafx.scene.{AmbientLight, Group, Node, PerspectiveCamera, PointLight, Scene, SceneAntialiasing}

object JfxViewer extends Viewer {
  
  def apply(obj: Polyhedron) {
    val cmd = Array("java", "-classpath", getClassPath, "dzufferey.scadla.backends.JfxViewerApp")
    val input = Some(serialize(obj))
    SysCmd.execWithoutOutput(cmd, input)
  }

  def serialize(obj: Polyhedron): String = {
    import scala.pickling.Defaults._
    import scala.pickling.json._
    JfxViewerObjPoly(obj).pickle.value
  }
  
  def serialize(a: Assembly): String = {
    import scala.pickling.Defaults._
    import scala.pickling.json._
    JfxViewerObjAssembly(a).pickle.value
  }
  
  def deserialize(str: String): JfxViewerObj = {
    import scala.pickling.Defaults._
    import scala.pickling.json._
    JSONPickle(str).unpickle[JfxViewerObj]
  }

  def getClassPath = System.getProperty("java.class.path")

//for testing
//def main(args: Array[String]) {
//  val obj = OpenSCAD(Union(Translate(-2,-2,-2,Sphere(5.0)), Cube(1.0, 1.0, 1.0)))
//  apply(obj)
//}

}

//TODO add parameters from the JfxViewerObj
object JfxViewerApp extends JFXApp {

  protected def readPolyFromStdIn = {
    val buffer = new StringBuffer
    var line = scala.io.StdIn.readLine
    while (line != null) {
      buffer.append(line)
      line = scala.io.StdIn.readLine
    }
    JfxViewer.deserialize(buffer.toString)
  }

  stage = new PrimaryStage {
    title = "Scadla JavaFX model viewer"
    //TODO adapt resolution
    scene = new Scene(800, 600, true, SceneAntialiasing.Balanced) {
      fill = Color.LightBlue
     
      val obj = readPolyFromStdIn
      
      val ((minX,maxX), 
           (minY,maxY),
           (minZ,maxZ)) = obj.boundingBox
     
      //center at origin
      val centered = new Group(obj.mesh) {
        translateX = - (maxX - minX) / 2
        translateY = - (maxY - minY) / 2
        translateZ = - (maxZ - minZ) / 2
      }
      //scale to camera FOV
      val scaled = new Group(centered) {
        val sX = 600 / (maxX - minX)
        val sY = 600 / (maxY - minY)
        val sZ = 600 / (maxZ - minZ)
        val s = math.min(sX, math.min(sY, sZ))
        scaleX = s
        scaleY = s
        scaleZ = s
      }
     
      // Put shapes in a group so they can be rotated together
      val shapes = new Group(scaled)
     
      val pointLight = new PointLight {
        color = Color.LightGray
        translateX = -10000.0
        translateY = -10000.0
        translateZ = -10000.0
      }
     
      val ambientLight = new AmbientLight {
        color = Color.DarkGray
      }

      root = new Group {
        children = new Group(shapes, pointLight, ambientLight)
        translateX = 400.0
        translateY = 300.0
        translateZ = 200.0
      }
     
      //TODO place the camera to get openscad-like coordinates
      camera = new PerspectiveCamera(false) {
        nearClip = 0.1
        farClip = 10000.0
      }
     
      addMouseEvents(this, shapes)
    }
  }

  /** Add mouse interaction to a scene, rotating given node. */
  def addMouseEvents(scene: Scene, node: Node) {
    val angleZ = DoubleProperty(0)
    val zRotate = new Rotate {
      angle <== angleZ
      axis = Rotate.ZAxis
    }
    var anchorX: Double = 0
    var anchorAngleZ: Double = 0
    
    val angleX = DoubleProperty(90)
    val xRotate = new Rotate {
      angle <== angleX
      axis = Rotate.XAxis
    }
    var anchorY: Double = 0
    var anchorAngleX: Double = 0

    val scale = DoubleProperty(1)
    val scaleT = new Scale(1,1,1,0,0,0) {
      x <== scale
      y <== scale
      z <== scale
    }

    node.transforms = Seq(scaleT, xRotate, zRotate)

    scene.onMousePressed = (event: MouseEvent) => {
      anchorX = event.sceneX
      anchorY = event.sceneY
      anchorAngleZ = angleZ()
      anchorAngleX = angleX()
    }
    scene.onMouseDragged = (event: MouseEvent) => {
      angleZ() = anchorAngleZ - (anchorX - event.sceneX) / 5.0
      angleX() = anchorAngleX - (anchorY - event.sceneY) / 5.0
    }
    node.onScroll = (event: ScrollEvent) => {
      val delta = event.deltaY
      val s = scale() * (1.0 + delta / 500.0)
      //println("d = " + delta + "\ts = " + s)
      scale() = math.max(0.1, math.min(s, 4))
    }
  }

}

abstract class JfxViewerObj {
  def parameters: List[(String, Double, Double)] //name, min, max
  def setParameters(params: List[Double]): Unit
  def mesh: Node
  def boundingBox: ((Double,Double),(Double,Double),(Double,Double))
}
case class JfxViewerObjPoly(p: Polyhedron) extends JfxViewerObj {
  def parameters = Nil
  def setParameters(params: List[Double]) {}
  def mesh = JfxViewerObj.objToMeshView(p)
  def boundingBox = JfxViewerObj.getBoundingBox(p)
}
//TODO for displaying Assembly
case class JfxViewerObjAssembly(a: Assembly) extends JfxViewerObj {
  def parameters = List(("time", 0, 10), ("expansion",0,1))
  def setParameters(params: List[Double]) {
    //TODO when updating, only the values of the tranforms needs to be updated
    ???
  }
  //TODO turn the Seq[(Frame,Polyhedron)] into a series of object and tranforms (and keep a ref for the update)
  def mesh = ???
  def boundingBox = ??? 
}

object JfxViewerObj {

  def getBoundingBox(obj: Polyhedron) = {
    if (obj.faces.isEmpty) {
      (
        (0.0, 1.0),
        (0.0, 1.0),
        (0.0, 1.0)
      )
    } else {
      obj.boundingBox
    }
  }

  def objToMeshView(obj: Polyhedron) = {
    val mesh = new TriangleMesh

    mesh.texCoords = Array(0.0f, 0.0f,
                           1.0f, 0.0f,
                           0.0f, 1.0f)

    val (indexedP,indexedF) = obj.indexed
    val ptsArray = Array.ofDim[Float](indexedP.size * 3)
    for ( i <- indexedP.indices ) {
      val p = indexedP(i)
      ptsArray(3*i)   = p.x.toFloat
      ptsArray(3*i+1) = p.y.toFloat
      ptsArray(3*i+2) = p.z.toFloat
    }
    mesh.points = ptsArray

    mesh.faces = indexedF.flatMap{ case (i1, i2, i3) =>
      Array(i1, 0, i2, 1, i3, 2)
    }.toArray

    mesh.faceSmoothingGroups = Array.fill(obj.faces.size)(0)
      
    new MeshView(mesh) {
      material = new PhongMaterial(Color.Gray) {
        specularColor = Color.White
      }
    }
  }

  
}
