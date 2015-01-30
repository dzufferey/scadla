package dzufferey.scadla

package object utils {

  import dzufferey.scadla._

  def centeredCube(x: Double, y: Double, z:Double) = Translate(-x/2, -y/2, -z/2, Cube(x,y,z))

  def biggerS(obj: Solid, s: Double) = Minkowski(obj, Sphere(s))

  def bigger(obj: Solid, s: Double) = Minkowski(obj, centeredCube(s,s,s))

  def tube(outerRadius: Double, innerRadius: Double, height: Double) = {
    Difference(
      Cylinder(outerRadius, outerRadius, height),
      Translate( 0, 0, -1, Cylinder(innerRadius, innerRadius, height + 2))
    )
  }

  def pieSlice(outerRadius: Double, innerRadius: Double, angle: Double, height: Double) = {
    val o1 = outerRadius + 1
    val t = tube(outerRadius, innerRadius, height)
    val blocking_half = Translate(-o1, -o1, -0.5, Cube(2* o1, o1, height + 1))
    val blocking_quarter = Translate(0, 0, -0.5, Cube(o1, o1, height + 1))
    if (angle <= 0) {
      Empty
    } else {
      val block =
        if (angle <= math.Pi/2) {
          Union(
            blocking_half,
            Translate(-o1, -0.5, 0, blocking_quarter),
            Rotate(0, 0, angle, blocking_quarter))
        } else if (angle <= math.Pi) {
          Union(
            blocking_half,
            Rotate(0, 0, angle, blocking_quarter))
        } else if (angle <= 3*math.Pi/2) {
          Union(
            Translate(0, -o1, 0, blocking_quarter),
            Rotate(0, 0, angle, blocking_quarter))
        } else if (angle <= 2*math.Pi) {
          Intersection(
            Translate(0, -o1, 0, blocking_quarter),
            Rotate(0, 0, angle, blocking_quarter))
        } else {
          Empty
        }
      Difference(t, block)
    }
  }

  def trapeze(xTop: Double, xBottom: Double, y: Double, z: Double) = {
    val x = math.max(xTop, xBottom)
    val cube = Cube(x, y, z)
    val blocking = Cube(x, y, 2*z)
    val a = math.atan2(z, (xBottom-xTop)/2)
    Difference(
      cube,
      Rotate(0, a, 0, Translate( xBottom, 0, 0, blocking)),
      Translate(-xBottom, 0, 0, Rotate(0,-a, 0, blocking))
    )
  }
  
  def traverse(f: Solid => Unit, s: Solid): Unit = s match {
    case Translate(x, y, z, s2) => traverse(f, s2); f(s)
    case Rotate(x, y, z, s2) => traverse(f, s2); f(s)
    case Scale(x, y, z, s2) => traverse(f, s2); f(s)
    case Mirror(x, y, z, s2) => traverse(f, s2); f(s)
    case Multiply(m, s2) => traverse(f, s2); f(s)

    case Union(lst @ _*) => lst.foreach(traverse(f, _)); f(s)
    case Intersection(lst @ _*) => lst.foreach(traverse(f, _)); f(s)
    case Difference(s2, lst @ _*) => traverse(f, s2); lst.foreach(traverse(f, _)); f(s)
    case Minkowski(lst @ _*) => lst.foreach(traverse(f, _)); f(s)
    case Hull(lst @ _*) => lst.foreach(traverse(f, _)); f(s)

    case other => f(other)
  }
  
  def map(f: Solid => Solid, s: Solid): Solid = s match {
    case Translate(x, y, z, s2) => f(Translate(x, y, z, map(f, s2)))
    case Rotate(x, y, z, s2) => f(Rotate(x, y, z, map(f, s2)))
    case Scale(x, y, z, s2) => f(Scale(x, y, z, map(f, s2)))
    case Mirror(x, y, z, s2) => f(Mirror(x, y, z, map(f, s2)))
    case Multiply(m, s2) => f(Multiply(m, map(f, s2)))

    case Union(lst @ _*) => f(Union(lst.map(map(f, _)):_*))
    case Intersection(lst @ _*) => f(Intersection(lst.map(map(f, _)):_*))
    case Difference(s2, lst @ _*) => f(Difference(map(f, s2), lst.map(map(f, _)):_*))
    case Minkowski(lst @ _*) => f(Minkowski(lst.map(map(f, _)):_*))
    case Hull(lst @ _*) => f(Hull(lst.map(map(f, _)):_*))

    case other => f(other)
  }

  def simplify(s: Solid): Solid = {
    def rewrite(s: Solid): Solid = s match {
      case Cube(width, depth, height) if width <= 0.0 || depth <= 0.0 || height <= 0.0 => Empty
      case Sphere(radius) if radius <= 0.0 => Empty
      case Cylinder(radiusBot, radiusTop, height) if height <= 0.0 => Empty
      case Polyhedron(triangles) if triangles.isEmpty => Empty

      case Translate(x1, y1, z1, Translate(x2, y2, z2, s2)) => Translate(x1+x2, y1+y2, z1+z2, s2)
      case Rotate(x1, 0, 0, Rotate(x2, 0, 0, s2)) => Rotate(x1+x2, 0, 0, s2)
      case Rotate(0, y1, 0, Rotate(0, y2, 0, s2)) => Rotate(0, y1+y2, 0, s2)
      case Rotate(0, 0, z1, Rotate(0, 0, z2, s2)) => Rotate(0, 0, z1+z2, s2)
      case Scale(x1, y1, z1, Scale(x2, y2, z2, s2)) => Scale(x1*x2, y1*y2, z1*z2, s2)
      
      //TODO flatten ops
      case Union(lst @ _*) =>
        val lst2 = lst.toSet - Empty
        if (lst2.isEmpty) Empty else Union(lst2.toSeq: _*)
      case Intersection(lst @ _*) =>
        val lst2 = lst.toSet
        if (lst2.contains(Empty) || lst2.isEmpty) Empty else Intersection(lst2.toSeq: _*)
      case Difference(s2, lst @ _*) =>
        val lst2 = lst.toSet - Empty
        if (lst2.isEmpty) s2
        else if (lst2 contains s2) Empty
        else Difference(s2, lst2.toSeq: _*)
      case Minkowski(lst @ _*) =>
        val lst2 = lst.toSet - Empty
        if (lst2.isEmpty) Empty else Minkowski(lst2.toSeq: _*)
      case Hull(lst @ _*) =>
        val lst2 = lst.toSet - Empty
        if (lst2.isEmpty) Empty else Hull(lst2.toSeq: _*)

      case s => s
    }

    var sOld = s
    var sCurr = map(rewrite, s)
    while (sOld != sCurr) {
      sOld = sCurr
      sCurr = map(rewrite, sCurr)
    }
    sCurr
  }

}

