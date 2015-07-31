package dzufferey.scadla.backends.utils

import dzufferey.scadla._
import dzufferey.scadla.assembly._
  
import scala.pickling._
import scala.pickling.Defaults._
//import scala.pickling.json._
import scala.pickling.static._
//import scala.pickling.functions._


object Pickles {

  //Primitives
  implicit val pointPickler         = PicklerUnpickler.generate[Point]
  implicit val vectorPickler        = PicklerUnpickler.generate[Vector]
  implicit val facePickler          = PicklerUnpickler.generate[Face]
  implicit val quaternionPickler    = PicklerUnpickler.generate[Quaternion]
  implicit val matrixPickler        = PicklerUnpickler.generate[Matrix]

  //basic solids
  implicit val cubePickler          = PicklerUnpickler.generate[Cube]
  implicit val spherePickler        = PicklerUnpickler.generate[Sphere]
  implicit val cylinderPickler      = PicklerUnpickler.generate[Cylinder]
  implicit val fromFilePickler      = PicklerUnpickler.generate[FromFile]
  implicit val emptyPickler         = PicklerUnpickler.generate[Empty.type]
  implicit val polyhedronPickler    = PicklerUnpickler.generate[Polyhedron]

  implicit val unionPickler = new Pickler[Union] with Unpickler[Union] {
    def tag = FastTypeTag[Union]
    def pickle(picklee: Union, builder: PBuilder): Unit = {
      builder.hintTag(tag)
      builder.beginEntry(picklee)
      putSolids(picklee.objs, builder)
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      Union(getSolids(reader):_*)
    }
  }

  implicit val intersectionPickler = new Pickler[Intersection] with Unpickler[Intersection] {
    def tag = FastTypeTag[Intersection]
    def pickle(picklee: Intersection, builder: PBuilder): Unit = {
      builder.hintTag(tag)
      builder.beginEntry(picklee)
      putSolids(picklee.objs, builder)
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      Intersection(getSolids(reader):_*)
    }
  }

  implicit val differencePickler = new Pickler[Difference] with Unpickler[Difference] {
    def tag = FastTypeTag[Difference]
    def pickle(picklee: Difference, builder: PBuilder): Unit = {
      builder.hintTag(tag)
      putSolid("pos", picklee.pos, builder)
      builder.beginEntry(picklee)
      putSolids(picklee.negs, builder)
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      val pos = getSolid("pos", reader)
      val negs = getSolids(reader)
      Difference(pos, negs:_*)
    }
  }

  implicit val minkowskiPickler = new Pickler[Minkowski] with Unpickler[Minkowski] {
    def tag = FastTypeTag[Minkowski]
    def pickle(picklee: Minkowski, builder: PBuilder): Unit = {
      builder.hintTag(tag)
      builder.beginEntry(picklee)
      putSolids(picklee.objs, builder)
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      Minkowski(getSolids(reader):_*)
    }
  }

  implicit val hullPickler = new Pickler[Hull] with Unpickler[Hull] {
    def tag = FastTypeTag[Hull]
    def pickle(picklee: Hull, builder: PBuilder): Unit = {
      builder.hintTag(tag)
      builder.beginEntry(picklee)
      putSolids(picklee.objs, builder)
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      Hull(getSolids(reader):_*)
    }
  }

  implicit val scalePickler = new Pickler[Scale] with Unpickler[Scale] {
    def tag = FastTypeTag[Scale]
    def pickle(picklee: Scale, builder: PBuilder): Unit = {
      builder.hintTag(tag)
      builder.beginEntry(picklee)
      putDouble("x", picklee.x, builder)
      putDouble("y", picklee.y, builder)
      putDouble("z", picklee.z, builder)
      putSolid("obj", picklee.obj, builder)
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      val x = getDouble("x", reader)
      val y = getDouble("y", reader)
      val z = getDouble("z", reader)
      val obj = getSolid("obj", reader)
      Scale(x, y, z, obj)
    }
  }

  implicit val rotatePickler = new Pickler[Rotate] with Unpickler[Rotate] {
    def tag = FastTypeTag[Rotate]
    def pickle(picklee: Rotate, builder: PBuilder): Unit = {
      builder.hintTag(tag)
      builder.beginEntry(picklee)
      putDouble("x", picklee.x, builder)
      putDouble("y", picklee.y, builder)
      putDouble("z", picklee.z, builder)
      putSolid("obj", picklee.obj, builder)
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      val x = getDouble("x", reader)
      val y = getDouble("y", reader)
      val z = getDouble("z", reader)
      val obj = getSolid("obj", reader)
      Rotate(x, y, z, obj)
    }
  }

  implicit val translatePickler = new Pickler[Translate] with Unpickler[Translate] {
    def tag = FastTypeTag[Translate]
    def pickle(picklee: Translate, builder: PBuilder): Unit = {
      builder.hintTag(tag)
      builder.beginEntry(picklee)
      putDouble("x", picklee.x, builder)
      putDouble("y", picklee.y, builder)
      putDouble("z", picklee.z, builder)
      putSolid("obj", picklee.obj, builder)
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      val x = getDouble("x", reader)
      val y = getDouble("y", reader)
      val z = getDouble("z", reader)
      val obj = getSolid("obj", reader)
      Translate(x, y, z, obj)
    }
  }

  implicit val mirrorPickler = new Pickler[Mirror] with Unpickler[Mirror] {
    def tag = FastTypeTag[Mirror]
    def pickle(picklee: Mirror, builder: PBuilder): Unit = {
      builder.hintTag(tag)
      builder.beginEntry(picklee)
      putDouble("x", picklee.x, builder)
      putDouble("y", picklee.y, builder)
      putDouble("z", picklee.z, builder)
      putSolid("obj", picklee.obj, builder)
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      val x = getDouble("x", reader)
      val y = getDouble("y", reader)
      val z = getDouble("z", reader)
      val obj = getSolid("obj", reader)
      Mirror(x, y, z, obj)
    }
  }

  implicit val multiplyPickler = new Pickler[Multiply] with Unpickler[Multiply] {
    def tag = FastTypeTag[Multiply]
    def pickle(picklee: Multiply, builder: PBuilder): Unit = {
      builder.hintTag(tag)
      builder.beginEntry(picklee)
      builder.putField("m", b => {
        b.hintTag(implicitly[FastTypeTag[Matrix]])
        matrixPickler.pickle(picklee.m, b)
      })
      putSolid("obj", picklee.obj, builder)
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      val r = reader.readField("m")
      val m = matrixPickler.unpickleEntry(r).asInstanceOf[Matrix]
      val obj = getSolid("obj", reader)
      Multiply(m, obj)
    }
  }

  implicit val solidPickler: Pickler[Solid] with Unpickler[Solid] = new Pickler[Solid] with Unpickler[Solid] {
    def tag = FastTypeTag[Solid]
    def pickle(picklee: Solid, builder: PBuilder): Unit = picklee match {
      case c: Cube          => cubePickler.pickle(c, builder)
      case c: Sphere        => spherePickler.pickle(c, builder)
      case c: Cylinder      => cylinderPickler.pickle(c, builder)
      case c: FromFile      => fromFilePickler.pickle(c, builder)
      case Empty            => emptyPickler.pickle(Empty, builder)
      case c: Polyhedron    => polyhedronPickler.pickle(c, builder)
      case c: Union         => unionPickler.pickle(c, builder)
      case c: Intersection  => intersectionPickler.pickle(c, builder)
      case c: Difference    => differencePickler.pickle(c, builder)
      case c: Minkowski     => minkowskiPickler.pickle(c, builder)
      case c: Hull          => hullPickler.pickle(c, builder)
      case c: Scale         => scalePickler.pickle(c, builder)
      case c: Rotate        => rotatePickler.pickle(c, builder)
      case c: Translate     => translatePickler.pickle(c, builder)
      case c: Mirror        => mirrorPickler.pickle(c, builder)
      case c: Multiply      => multiplyPickler.pickle(c, builder)
    }

    def unpickle(tag: String, reader: PReader): Any = {
      if (tag == cubePickler.tag.key)               cubePickler.unpickle(tag, reader)
      else if (tag == spherePickler.tag.key)        spherePickler.unpickle(tag, reader)
      else if (tag == cylinderPickler.tag.key)      cylinderPickler.unpickle(tag, reader)
      else if (tag == fromFilePickler.tag.key)      fromFilePickler.unpickle(tag, reader)
      else if (tag == emptyPickler.tag.key)         emptyPickler.unpickle(tag, reader)
      else if (tag == polyhedronPickler.tag.key)    polyhedronPickler.unpickle(tag, reader)
      else if (tag == unionPickler.tag.key)         unionPickler.unpickle(tag, reader)
      else if (tag == intersectionPickler.tag.key)  intersectionPickler.unpickle(tag, reader)
      else if (tag == differencePickler.tag.key)    differencePickler.unpickle(tag, reader)
      else if (tag == minkowskiPickler.tag.key)     minkowskiPickler.unpickle(tag, reader)
      else if (tag == hullPickler.tag.key)          hullPickler.unpickle(tag, reader)
      else if (tag == scalePickler.tag.key)         scalePickler.unpickle(tag, reader)
      else if (tag == rotatePickler.tag.key)        rotatePickler.unpickle(tag, reader)
      else if (tag == translatePickler.tag.key)     translatePickler.unpickle(tag, reader)
      else if (tag == mirrorPickler.tag.key)        mirrorPickler.unpickle(tag, reader)
      else if (tag == multiplyPickler.tag.key)      multiplyPickler.unpickle(tag, reader)
      else sys.error("solidPickler.unpickle, unknown tag: " + tag)
    }
  } 

  //✓ case class Dummy(yada: Seq[Int])
  //✗ case class Dummy(yada: Int*)
  //implicit val dummyPickler   =   Pickler.generate[Dummy]
  //implicit val dummyUnpickler = Unpickler.generate[Dummy]

  //TODO Assembly ...
  implicit val jointPickler = PicklerUnpickler.generate[Joint]
  implicit val framePickler = PicklerUnpickler.generate[Frame]
  implicit val partPickler = new Pickler[Part] with Unpickler[Part] {
    def tag = FastTypeTag[Part]
    def pickle(picklee: Part, builder: PBuilder): Unit = {
      builder.hintTag(tag)
      builder.beginEntry(picklee)
      putString("name", picklee.name, builder)
      putSolid("model", picklee.model, builder)
      putSolids(picklee.printableModel.toSeq, builder)
      putString("description", picklee.description, builder)
      putBoolean("vitamin", picklee.vitamin, builder)
      assert(picklee.poly != null && picklee.polyPrint != null, "part should be rendered before serialization (TODO relax that)")
      putPoly("poly", picklee.poly, builder)
      putPoly("polyPrint", picklee.polyPrint, builder)
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      val name = getString("name", reader)
      val model = getSolid("model", reader)
      val printable = getSolids(reader).headOption
      val part = new Part(name, model, printable)
      part.description = getString("description", reader)
      part.vitamin = getBoolean("vitamin", reader)
      part.poly = getPoly("poly", reader)
      part.polyPrint = getPoly("polyPrint", reader)
      part
    }
  }
  implicit val eAssemblyPickler = new Pickler[EmptyAssembly] with Unpickler[EmptyAssembly] {
    def tag = FastTypeTag[EmptyAssembly]
    def pickle(picklee: EmptyAssembly, builder: PBuilder): Unit = {
      builder.hintTag(tag)
      builder.beginEntry(picklee)
      putString("name", picklee.name, builder)
      putChildren(picklee.children, builder)
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      val name = getString("name", reader)
      val children = getChildren(reader)
      EmptyAssembly(name, children)
    }
  }
  implicit val sAssemblyPickler = new Pickler[SingletonAssembly] with Unpickler[SingletonAssembly] {
    def tag = FastTypeTag[SingletonAssembly]
    def pickle(picklee: SingletonAssembly, builder: PBuilder): Unit = {
      builder.hintTag(tag)
      builder.beginEntry(picklee)
      builder.putField("part", b => {
        b.hintTag(implicitly[FastTypeTag[Part]])
        b.hintStaticallyElidedType()
        partPickler.pickle(picklee.part, b)
      })
      putChildren(picklee.children, builder)
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      val r1 = reader.readField("part")
      r1.hintStaticallyElidedType()
      val tag1 = r1.beginEntry
      val part = partPickler.unpickle(tag, r1).asInstanceOf[Part]
      r1.endEntry
      val children = getChildren(reader)
      SingletonAssembly(part, children)
    }
  }
  implicit val assemblyPickler: Pickler[Assembly] with Unpickler[Assembly] = new Pickler[Assembly] with Unpickler[Assembly] {
    def tag = FastTypeTag[Assembly]
    def pickle(picklee: Assembly, builder: PBuilder): Unit = picklee match {
      case e: EmptyAssembly     => eAssemblyPickler.pickle(e, builder)
      case s: SingletonAssembly => sAssemblyPickler.pickle(s, builder)
    }
    def unpickle(tag: String, reader: PReader): Any = {
      if (tag == eAssemblyPickler.tag.key)      eAssemblyPickler.unpickle(tag, reader)
      else if (tag == sAssemblyPickler.tag.key) sAssemblyPickler.unpickle(tag, reader)
      else sys.error("assemblyPickler.unpickle, unknown tag: " + tag)
    }
  }
  implicit val cAssemblyPickler = new Pickler[(Frame,Joint,Assembly,Frame)] with Unpickler[(Frame,Joint,Assembly,Frame)] {
    def tag = FastTypeTag[(Frame,Joint,Assembly,Frame)]
    def pickle(picklee: (Frame,Joint,Assembly,Frame), builder: PBuilder): Unit = {
      builder.hintTag(tag)
      builder.beginEntry(picklee)
      builder.putField("_1", b => {
        b.hintTag(implicitly[FastTypeTag[Frame]])
        b.hintStaticallyElidedType()
        framePickler.pickle(picklee._1, b)
      })
      builder.putField("_2", b => {
        b.hintTag(implicitly[FastTypeTag[Joint]])
        b.hintStaticallyElidedType()
        jointPickler.pickle(picklee._2, b)
      })
      builder.putField("_3", b => {
        b.hintTag(implicitly[FastTypeTag[Assembly]])
        assemblyPickler.pickle(picklee._3, b)
      })
      builder.putField("_4", b => {
        b.hintTag(implicitly[FastTypeTag[Frame]])
        b.hintStaticallyElidedType()
        framePickler.pickle(picklee._4, b)
      })
      builder.endEntry()
    }
    def unpickle(tag: String, reader: PReader): Any = {
      val r1 = reader.readField("_1")
      r1.hintStaticallyElidedType()
      val tag1 = r1.beginEntry
      val _1 = framePickler.unpickle(tag, r1).asInstanceOf[Frame]
      r1.endEntry
      val r2 = reader.readField("_2")
      r2.hintStaticallyElidedType()
      val tag2 = r2.beginEntry
      val _2 = jointPickler.unpickle(tag, r2).asInstanceOf[Joint]
      r2.endEntry
      val r3 = reader.readField("_3")
      val tag3 = r3.beginEntry
      val _3 = assemblyPickler.unpickle(tag, r3).asInstanceOf[Assembly]
      r3.endEntry
      val r4 = reader.readField("_4")
      r4.hintStaticallyElidedType()
      val tag4 = r4.beginEntry
      val _4 = framePickler.unpickle(tag, r4).asInstanceOf[Frame]
      r4.endEntry
      (_1, _2, _3, _4)
    }
  }
  

  ///////////
  // utils //
  ///////////

  private def putString(name: String, value: String, builder: PBuilder) {
    builder.putField(name, b => {
      b.hintTag(implicitly[FastTypeTag[String]])
      b.hintStaticallyElidedType()
      stringPickler.pickle(value, b)
    })
  }
  
  private def getString(name: String, reader: PReader) = {
    val r = reader.readField(name)
    r.hintStaticallyElidedType()
    val tag = r.beginEntry
    val result = stringPickler.unpickle(tag, r).asInstanceOf[String]
    r.endEntry
    result
  }
  
  private def putBoolean(name: String, value: Boolean, builder: PBuilder) {
    builder.putField(name, b => {
      b.hintTag(implicitly[FastTypeTag[Boolean]])
      b.hintStaticallyElidedType()
      booleanPickler.pickle(value, b)
    })
  }

  private def getBoolean(name: String, reader: PReader) = {
    val r = reader.readField(name)
    r.hintStaticallyElidedType()
    val tag = r.beginEntry
    val result = booleanPickler.unpickle(tag, r).asInstanceOf[Boolean]
    r.endEntry
    result
  }

  private def putDouble(name: String, value: Double, builder: PBuilder) {
    builder.putField(name, b => {
      b.hintTag(implicitly[FastTypeTag[Double]])
      b.hintStaticallyElidedType()
      doublePickler.pickle(value, b)
    })
  }

  private def getDouble(name: String, reader: PReader) = {
    val r = reader.readField(name)
    r.hintStaticallyElidedType()
    val tag = r.beginEntry
    val result = doublePickler.unpickle(tag, r).asInstanceOf[Double]
    r.endEntry
    result
  }
  
  private def putPoly(name: String, value: Polyhedron, builder: PBuilder) {
    builder.putField(name, b => {
      b.hintTag(implicitly[FastTypeTag[Polyhedron]])
      b.hintStaticallyElidedType()
      polyhedronPickler.pickle(value, b)
    })
  }
  
  private def getPoly(name: String, reader: PReader) = {
    val r = reader.readField(name)
    r.hintStaticallyElidedType()
    val tag = r.beginEntry
    val result = polyhedronPickler.unpickle(tag, r).asInstanceOf[Polyhedron]
    r.endEntry
    result
  }

  private def putSolid(name: String, value: Solid, builder: PBuilder) {
    builder.putField(name, b => solidPickler.pickle(value, b) )
  }

  private def getSolid(name: String, reader: PReader) = {
    val r = reader.readField(name)
    val tag = r.beginEntry
    val result = solidPickler.unpickle(tag, r).asInstanceOf[Solid]
    r.endEntry
    result
  }

  private def putSolids(value: Seq[Solid], builder: PBuilder) {
    builder.beginCollection(value.length)
    value.foreach(v => builder.putElement(b => solidPickler.pickle(v, b) ) )
    builder.endCollection
  }

  private def getSolids(reader: PReader): Seq[Solid] = {
    val r = reader.beginCollection
    val length = reader.readLength()
    val builder = new scala.collection.immutable.VectorBuilder[Solid]
    builder.sizeHint(length)

    var i = 0
    while (i < length) {
      val re = reader.readElement()
       builder += solidPickler.unpickleEntry(re).asInstanceOf[Solid]
       i += 1
    }

    reader.endCollection
    builder.result
  }
  
  private def putChildren(value: Seq[(Frame,Joint,Assembly,Frame)], builder: PBuilder) {
    builder.beginCollection(value.length)
    value.foreach(v => builder.putElement(b => cAssemblyPickler.pickle(v, b) ) )
    builder.endCollection
  }

  private def getChildren(reader: PReader): List[(Frame,Joint,Assembly,Frame)] = {
    val r = reader.beginCollection
    val length = reader.readLength()
    val builder = scala.collection.mutable.ListBuffer[(Frame,Joint,Assembly,Frame)]()
    builder.sizeHint(length)

    var i = 0
    while (i < length) {
      val re = reader.readElement()
       builder += cAssemblyPickler.unpickleEntry(re).asInstanceOf[(Frame,Joint,Assembly,Frame)]
       i += 1
    }

    reader.endCollection
    builder.result
  }
  
}
