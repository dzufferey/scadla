package scadla.backends.renderers

import scadla.backends.renderers.Solids._
import scadla.{Cube, Cylinder, FromFile, Polyhedron, Sphere, Empty}
import squants.space.{Length, Millimeters}

object OpenScad {

  protected def length2Double(l: Length): Double = l to Millimeters

  implicit val emptyRenderer = new Renderable[Empty] {
    override def render(s: Empty, indent: Int): String = ""
  }

  implicit val cubeRenderer = new Renderable[Cube] {
    override def render(s: Cube, indent: Int): String = {
      (" "*indent) + "cube([ " + length2Double(s.width) + ", " + length2Double(s.depth) + ", " + length2Double(s.height) + "]);"+ "\n"
    }
  }

  implicit val sphereRenderer = new Renderable[Sphere] {
    override def render(s: Sphere, indent: Int): String = (" "*indent) + "sphere( " + length2Double(s.radius) + ");"+ "\n"
  }

  implicit val cylinderRenderer = new Renderable[Cylinder] {
    override def render(s: Cylinder, indent: Int): String = (" "*indent) + "cylinder( r1 = " + length2Double(s.radiusBot) + ", r2 = " + length2Double(s.radiusTop) + ", h = " + length2Double(s.height) + ");"+ "\n"
  }

  implicit val polyhedronRenderer = new Renderable[Polyhedron] {
    override def render(p: Polyhedron, indent: Int): String = {
      val (indexedP,indexedF) = p.indexed
      (" "*indent) +
        "polyhedron( points=[ " +
        indexedP.map(p => "["+ length2Double(p.x) +","+ length2Double(p.y) +","+ length2Double(p.z) +"]").mkString(", ") +
        " ], faces=[ " +
        indexedF.map{ case (a,b,c) => "["+a+","+b+","+c+"]" }.mkString(", ") +
        " ]);" + "\n"
    }
  }

  implicit val fromFileRenderer = new Renderable[FromFile] {
    override def render(s: FromFile, indent: Int): String = {
      (" "*indent) + "import(\"" + s.path + "\");" + "\n"
    }
  }

  implicit val unionRenderer = new Renderable[Union] {
    override def render(s: Union, indent: Int): String = {
      (" "*indent) + "union(){\n" +
        s.objs.map(ra => ra.render(indent+2)).mkString("") +
      (" "*indent) +  "}" + "\n"
    }
  }
  implicit val intersectionRenderer = new Renderable[Intersection] {
    override def render(s: Intersection, indent: Int): String = {
      (" "*indent) + "intersection(){\n" +
        s.objs.map(ra => ra.render(indent+2)).mkString("") +
        (" "*indent) +  "}" + "\n"
    }
  }
  implicit val differenceRenderer = new Renderable[Difference] {
    override def render(s: Difference, indent: Int): String = {
      (" "*indent) + "difference(){\n" +
        s.pos.render(indent+2) +
        s.negs.map(ra => ra.render(indent+2)).mkString("") +
        (" "*indent) +  "}" + "\n"
    }
  }
  implicit val minkowskiRenderer = new Renderable[Minkowski] {
    override def render(s: Minkowski, indent: Int): String = {
      (" "*indent) + "minkowski(){\n" +
        s.objs.map(ra => ra.render(indent+2)).mkString("") +
        (" "*indent) +  "}" + "\n"
    }
  }
  implicit val hullRenderer = new Renderable[Hull] {
    override def render(s:  Hull, indent:  Int): String = {
      (" "*indent) + "hull(){\n" +
        s.objs.map(ra => ra.render(indent+2)).mkString("") +
        (" "*indent) +  "}" + "\n"
    }
  }

  implicit val scaleRenderer = new Renderable[Scale] {
    override def render(s: Scale, indent: Int): String = {
      (" "*indent) + "scale(["+s.x+","+s.y+","+s.z+"])" + "\n" +
        s.obj.render(indent + 2)
    }
  }
  
  implicit val rotateRenderer = new Renderable[Rotate] {
    override def render(s: Rotate, indent: Int): String = {
      (" "*indent) + "rotate(["+s.x.toDegrees+","+s.y.toDegrees+","+s.z.toDegrees+"])" + "\n" +
        s.obj.render(indent + 2)
    }
  }
  implicit val translateRenderer = new Renderable[Translate] {
    override def render(s: Translate, indent: Int): String = {
      (" "*indent) + "translate(["+length2Double(s.x)+","+length2Double(s.y)+","+length2Double(s.z)+"])" + "\n" +
        s.obj.render(indent + 2)
    }
  }
  
  implicit val mirrorRenderer = new Renderable[Mirror] {
    override def render(s: Mirror, indent: Int): String = {
      (" "*indent) + "mirror(["+s.x+","+s.y+","+s.z+"])" + "\n" +
        s.obj.render(indent + 2)
    }
  }
  
  implicit val multiplyRenderer = new Renderable[Multiply] {
    override def render(s: Multiply, indent: Int): String = {
      (" "*indent) + "multmatrix([["+s.m.m00+","+s.m.m01+","+s.m.m02+","+s.m.m03+"],["+s.m.m10+","+s.m.m11+","+s.m.m12+","+s.m.m13+"],["+s.m.m20+","+s.m.m21+","+s.m.m22+","+s.m.m23+"],["+s.m.m30+","+s.m.m31+","+s.m.m32+","+s.m.m33+"],])" + "\n" +
        s.obj.render(indent + 2)
    }
  }

}
