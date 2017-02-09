package scadla.backends.utils

import scadla._
import scadla.assembly._
import scadla.backends._
//import com.esotericsoftware.kryo.Kryo
import com.twitter.chill._

object KryoSerializer {

  private val inst = new ScalaKryoInstantiator

  def serializer = {
    val kryo = inst.newKryo
    //
    kryo.register(classOf[Point])
    kryo.register(classOf[Vector])
    kryo.register(classOf[Face])
    kryo.register(classOf[Quaternion])
    kryo.register(classOf[Matrix])
    //
    kryo.register(classOf[Cube])
    kryo.register(classOf[Sphere])
    kryo.register(classOf[Cylinder])
    kryo.register(classOf[FromFile])
    kryo.register(Empty.getClass) //kryo.register(classOf[Empty.type])
    kryo.register(classOf[Polyhedron])
    //
    kryo.register(classOf[Union])
    kryo.register(classOf[Intersection])
    kryo.register(classOf[Difference])
    kryo.register(classOf[Minkowski])
    kryo.register(classOf[Hull])
    kryo.register(classOf[Scale])
    kryo.register(classOf[Rotate])
    kryo.register(classOf[Translate])
    kryo.register(classOf[Mirror])
    kryo.register(classOf[Multiply])
    //
    kryo.register(classOf[Joint])
    kryo.register(classOf[Frame])
    kryo.register(classOf[Part])
    kryo.register(classOf[EmptyAssembly])
    kryo.register(classOf[SingletonAssembly])
    kryo.register(classOf[(Frame,Joint,Assembly,Frame)])
    //
    kryo.register(classOf[JfxViewerObj])
    kryo.register(classOf[JfxViewerObjPoly])
    kryo.register(classOf[JfxViewerObjAssembly])
    //
    kryo
  }

}
