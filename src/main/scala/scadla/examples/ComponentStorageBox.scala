package scadla.examples

import scadla._
import utils._
import Trig._
import InlineOps._
import squants.space.{Length, Angle}
import scala.language.postfixOps
import squants.space.LengthConversions._
import scala.collection.parallel.CollectionConverters._

class ComponentStorageBox(
    width: Length,
    depth: Length,
    wallThickness: Length,
    numberOfDrawers: Int,
    drawerHeight: Length,
    drawerWallThickness: Length,
    gap: Length,
    labelWallThickness: Length
  ) {

  val height = numberOfDrawers * (drawerHeight + gap) + 2 * wallThickness
  
  val w2 = wallThickness * 2
  val w2g = w2 + gap

  protected def rail = {
    Cube(2*wallThickness, depth, wallThickness) + Cylinder(wallThickness/2, depth).rotateX(-Pi/2).move(2*wallThickness, 0 mm, wallThickness/2)
  }

  protected def rails = {
    rail + rail.mirror(1,0,0).moveX(width - w2)
  }

  protected def spread(s: Solid, direction: Vector, min: Length, max: Length, n: Int) = {
    val delta = ((max - min) / n).toMillimeters
    Union((0 until n).map( i => s.move(direction * (min.toMillimeters + delta/2 + i * delta))) :_*)
  }

  def shelf = {
    val outer = Cube(width, depth, height)
    val inner = Cube(width - w2g, depth - wallThickness - gap, height - w2g)
    val base = outer - inner.move(wallThickness, 0 mm, wallThickness)
    val rs = (1 to numberOfDrawers).map( i => rails.move( wallThickness, 0 mm, i * (drawerHeight + gap)) )
    val full = base ++ rs
    val hSize = (drawerHeight - w2) / math.sqrt(2)
    //holes to reduce the amount of plastic needed
    val hole = CenteredCube(hSize, hSize, 4 * wallThickness).rotateZ(Pi/4)
    val nbrHolesWidth = math.floor( (width - w2g) / (drawerHeight + w2) ).toInt
    val nbrHolesDepth = math.floor( (depth - w2g) / (drawerHeight + w2) ).toInt
    val holesBack = spread(hole.rotateX(Pi/2), Vector.x, wallThickness, width - wallThickness, nbrHolesWidth).moveY(depth)
    val holesLeft = spread(hole.rotateY(Pi/2), Vector.y, wallThickness, depth - wallThickness, nbrHolesDepth)
    val holesRight = holesLeft.moveX(width) 
    val holesRow = holesBack + holesLeft + holesRight
    val holes = spread(holesRow, Vector.z, wallThickness, height-wallThickness, numberOfDrawers)
    full - holes.moveZ(-wallThickness/2)
  }

  protected def labelHolder(width: Length) = {
    val c1 = Cube(width, 2* labelWallThickness, drawerHeight)
    val c2 = Cube(width - 2 * labelWallThickness, labelWallThickness, drawerHeight)
    val c3 = Cube(width - w2, 2 * labelWallThickness, drawerHeight)
    c1 - c2.move(labelWallThickness, 0 mm, labelWallThickness) - c3.move(wallThickness, 0 mm, drawerWallThickness)
  }

  def drawer(divisionsX: Int, divisionsY: Int) = {
    val dw2g = 2 * drawerWallThickness
    val handle = {
      val c1 = Cube(wallThickness, drawerWallThickness, drawerHeight)
      val c2 = Cylinder(drawerHeight/2,wallThickness)
      val h = Hull(c1, c2.rotateY(Pi/2).move(0 mm, drawerHeight/2, drawerHeight/2))
      val radius = drawerHeight/2 - drawerWallThickness
      val s = Sphere(radius).scale((drawerWallThickness- (1 mm))/radius/2, 1, 1)
      h - s.move(0 mm, drawerHeight/2, drawerHeight/2) - s.move(wallThickness, drawerHeight/2, drawerHeight/2) 
    }
    val dwidth = width - w2g
    val ddepth = depth - wallThickness - gap
    val c1 = Cube(dwidth, ddepth, drawerHeight)
    val c2 = Cube(dwidth - dw2g, ddepth - dw2g, drawerHeight).move(drawerWallThickness, drawerWallThickness, drawerWallThickness)
    val lh = labelHolder( (dwidth-wallThickness)/2 + labelWallThickness)
    //base with handle and thing to hold paper description
    val box = Union(c1 - c2,
        handle.mirror(0,1,0).moveX( (dwidth-wallThickness)/2 ),
        lh.mirror(0,1,0),
        lh.mirror(0,1,0).moveX( (dwidth+wallThickness)/2 -labelWallThickness)
      )
    //add inner dividers
    val dividersX = (1 until divisionsX).map( i => Cube( drawerWallThickness, ddepth, drawerHeight).moveX( -drawerWallThickness/2 + i * dwidth / divisionsX) )
    val dividersY = (1 until divisionsY).map( i => Cube( dwidth, drawerWallThickness, drawerHeight).moveY( -drawerWallThickness/2 + i * (depth-w2g) / divisionsY) )
    val withDividers = box ++ dividersX ++ dividersY
    //add space for the rails
    val r0 = (rails + rails.moveZ(-wallThickness)).move(-gap/2, 0 mm, drawerHeight+gap/2)
    val r1 = r0 + r0.moveY(-2*labelWallThickness)
    val rLeft = r1 * Cube(4*wallThickness, depth + 2*labelWallThickness, drawerHeight + (1 mm)).moveY(-2*labelWallThickness)
    val rRight= r1 * Cube(4*wallThickness, depth + 2*labelWallThickness, drawerHeight + (1 mm)).move(dwidth-4*wallThickness, -2*labelWallThickness, 0 mm)
    val railing = Bigger(Union(Hull(rLeft), Hull(rRight)), gap)
    withDividers - railing
  }

}

object ComponentStorageBox {


  def main(args: Array[String]) = {
    val box = new ComponentStorageBox(
        120 mm, //width
        120 mm, //depth
        2.4 mm, //wallThickness
        5,      //numberOfDrawers
        20 mm,  //drawerHeight
        1.2 mm, //drawerWallThickness
        0.8 mm, //gap
        0.4 mm  //labelWallThickness
      )
    val r = backends.Renderer.default
    //r.view(box.shelf)
    //r.view(box.drawer(1, 1))
    //r.view(box.drawer(3, 2))
    //r.view(box.shelf + box.drawer(1, 1).move(2.8, 0, 2.8))
    val elts = Seq(
      (box.shelf.rotateX(-Pi/2), "shelf.stl"),
      (box.drawer(1, 1), "drawer_1x1.stl"),
      (box.drawer(2, 1), "drawer_2x1.stl"),
      (box.drawer(2, 2), "drawer_2x2.stl"),
      (box.drawer(3, 1), "drawer_3x1.stl"),
      (box.drawer(3, 2), "drawer_3x2.stl"),
      (box.drawer(3, 3), "drawer_3x3.stl"),
      (box.drawer(4, 1), "drawer_4x1.stl"),
      (box.drawer(4, 2), "drawer_4x2.stl"),
      (box.drawer(4, 3), "drawer_4x3.stl"),
      (box.drawer(4, 4), "drawer_4x4.stl")
     )
    elts.par.foreach{ case (s, f) => r.toSTL(s, f) }
  }

}
