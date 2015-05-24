package dzufferey.scadla.utils
  
import dzufferey.scadla._
import dzufferey.scadla.InlineOps._
import scala.math._

//some references read when coding this
//  https://en.wikipedia.org/wiki/Involute_gear
//  https://en.wikipedia.org/wiki/Gear#Nomenclature
//  https://upload.wikimedia.org/wikipedia/commons/2/28/Gear_words.png
//  https://en.wikipedia.org/wiki/List_of_gear_nomenclature
//  http://lcamtuf.coredump.cx/gcnc/ and
//  http://www.hessmer.org/blog/2014/01/01/online-involute-spur-gear-builder/


object InvoluteGear {
  

  /** Create a gear by carving the tooths along an involute curve.
   * The method to generate spur gear inspired by
   *  http://lcamtuf.coredump.cx/gcnc/ and
   *  http://www.hessmer.org/blog/2014/01/01/online-involute-spur-gear-builder/
   * It is a certain computation cost but has the advantage of properly generating the fillet and undercut.
   * @param pitch the effective radius of the gear
   * @param nbrTooths the number of tooth in the gear
   * @param rackToothProfile the profile of a tooth on a rack (infinite gear) the profile must be centered ad 0,0.
   * @param addenum how much to add to the pitch to get the outer radius of the gear
   * @param height the height of the gear
   */
  def carve( pitch: Double,
             nbrTooths: Int,
             rackToothProfile: Solid,
             addenum: Double,
             height: Double) = {

    val samples = 50 //TODO as a parameter
    val range = Pi
    val trajectory = for (i <- 1 until samples) yield {
      val a = 0 - range / 2 + i * range / samples
      val x = Involute.x(pitch, 0, a)
      val y = Involute.y(pitch, 0, a)
      rackToothProfile.rotateZ(a).move(x, y, 0)
    }
    val hulled = trajectory.sliding(2).map( l => if (l.size > 1) Hull(l:_*) else l.head ).toSeq
    val negative = Union(hulled:_*)
    
    val angle = Pi / nbrTooths //between tooths
    val negatives = for (i <- 0 until nbrTooths) yield negative.rotateZ((2 * i) * angle)// + offset)

    val outsideRadius = pitch + addenum
    val outer = Cylinder(outsideRadius, height)

    outer -- negatives
  }


  /** Create an involute spur gear.
   * @param pitch the effective radius of the gear
   * @param nbrTooths the number of tooth in the gear
   * @param pressureAngle the angle between meshing gears at the pitch radius (0 mean "square" tooths, π/2 no tooths)
   * @param addenum how much to add to the pitch to get the outer radius of the gear
   * @param dedenum how much to remove to the pitch to get the root radius of the gear
   * @param height the height of the gear
   * @param backlash add some space (manufacturing tolerance)
   * @param skew generate a gear with an asymmetric profile by skewing the tooths
   */
  def apply( pitch: Double,
             nbrTooths: Int,
             pressureAngle: Double,
             addenum: Double,
             dedenum: Double,
             height: Double,
             backlash: Double,
             skew: Double = 0.0) = {

    assert(pitch > 0, "pitch must be greater than 0")
    assert(pitch - dedenum > 0, "dedenum must be smaller than the pitch")
    assert(addenum > 0, "addenum must be greater than 0")
    assert(dedenum  > 0, "dedenum must be greater than 0")
    assert(nbrTooths > 0, "number of tooths must be greater than 0")
    assert(pressureAngle < Pi / 2 && pressureAngle >= 0, "pressureAngle must be between in [0;π/2)")
    
    val angle = Pi / nbrTooths //between tooths
    val toothWidth = pitch * 2 * sin(angle/2)
    val base = toothWidth + 2 * addenum * sin(pressureAngle) + backlash
    val tip  = toothWidth - 2 * dedenum * sin(pressureAngle) + backlash
    assert(tip > 0, "tip of the profile is negative, try decreasing the pressureAngle, or addenum/dedenum.")
    val tHeight = addenum + dedenum + backlash
    val rackTooth = Trapezoid(tip, base, height, tHeight, skew).rotateX(Pi/2).move(-base/2 + addenum*tan(skew), addenum, 0).rotateZ(-Pi/2)

    carve(pitch, nbrTooths, rackTooth, addenum, height)
  }

  /** An involute gear with z tiled into many layers. */
  def stepped( pitch: Double,
               nbrTooths: Int,
               pressureAngle: Double,
               addenum: Double,
               dedenum: Double,
               height: Double,
               backlash: Double,
               skew: Double = 0.1,
               zStep: Double = 0.1) = {
    val stepCount = ceil(height / zStep).toInt
    val stepSize = height / stepCount
    def isZ(p: Point, z: Double) = (p.z - z).abs <= 1e-10 //TODO better way of dealing with numerical error
    val base = apply(pitch, nbrTooths, pressureAngle, addenum, dedenum, height, backlash, skew)
    val (bot, rest) = base.toPolyhedron.faces.partition{ case Face(p1, p2, p3) => isZ(p1, 0) && isZ(p2, 0) && isZ(p3, 0) }
    val (top, middle) = rest.partition{ case Face(p1, p2, p3) => isZ(p1, height) && isZ(p2, height) && isZ(p3, height) }

    def mvz(i: Int, z: Double) = {
      if ((z - height).abs <= 1e-10) {
        if (i == stepCount - 1) z
        else (i+1) * stepSize
      } else {
        i * stepSize
      }
    }
    def mvp(i: Int, p: Point) = Point(p.x, p.y, mvz(i, p.z))
    def mv(i: Int, f: Face) = Face(mvp(i, f.p1), mvp(i, f.p2), mvp(i, f.p3))

    val newMiddle = middle.flatMap( f => for (i <- 0 until stepCount) yield mv(i, f) )
    Polyhedron(bot ++ top ++ newMiddle)
  }

}

object HelicalGear {

  
  /** Create an helical gear by twisting an spur gear.
   * the following tranform is applied:
   *   x′ = x * cos(helixAngle * z) - y * sin(helixAngle * z)
   *   y′ = x * sin(helixAngle * z) + y * cos(helixAngle * z)
   *   z′ = z
   * @param pitch the effective radius of the gear
   * @param nbrTooths the number of tooth in the gear
   * @param pressureAngle the angle between meshing gears at the pitch radius (0 mean "square" tooths, π/2 no tooths)
   * @param addenum how much to add to the pitch to get the outer radius of the gear
   * @param dedenum how much to remove to the pitch to get the root radius of the gear
   * @param height the height of the gear
   * @param helixAngle how much twisting (in rad / mm)
   * @param backlash add some space (manufacturing tolerance)
   * @param skew generate a gear with an asymmetric profile by skewing the tooths
   * @param zStep (level of detail) how thick should the layers be before applying the transform
   */
  def apply( pitch: Double,
             nbrTooths: Int,
             pressureAngle: Double,
             addenum: Double,
             dedenum: Double,
             height: Double,
             helixAngle: Double,
             backlash: Double,
             skew: Double = 0.0,
             zStep: Double = 0.1) = {
    val stepped = InvoluteGear.stepped(pitch, nbrTooths, pressureAngle, addenum, dedenum, height, backlash, skew, zStep)
    def turnPoint(p: Point): Point = {
      val z = p.z
      val a = helixAngle * z
      val x = p.x * cos(a) - p.y * sin(a)
      val y = p.x * sin(a) + p.y * cos(a)
      Point(x, y, z)
    }
    Polyhedron(stepped.faces.map( f => Face(turnPoint(f.p1), turnPoint(f.p2), turnPoint(f.p3)) ))
  }
  
}

object HerringboneGear {

  /** Create an herringbone gear (from two helical gears of opposite rotations)
   * @param pitch the effective radius of the gear
   * @param nbrTooths the number of tooth in the gear
   * @param pressureAngle the angle between meshing gears at the pitch radius (0 mean "square" tooths, π/2 no tooths)
   * @param addenum how much to add to the pitch to get the outer radius of the gear
   * @param dedenum how much to remove to the pitch to get the root radius of the gear
   * @param height the height of the gear
   * @param helixAngle how much twisting (in rad / mm)
   * @param backlash add some space (manufacturing tolerance)
   * @param skew generate a gear with an asymmetric profile by skewing the tooths
   * @param zStep (level of detail) how thick should the layers be before applying the transform
   */
  def apply( pitch: Double,
             nbrTooths: Int,
             pressureAngle: Double,
             addenum: Double,
             dedenum: Double,
             height: Double,
             helixAngle: Double,
             backlash: Double,
             skew: Double = 0.0,
             zStep: Double = 0.1) = {
    val stepped = InvoluteGear.stepped(pitch, nbrTooths, pressureAngle, addenum, dedenum, height, backlash, skew, zStep)
    def turnPoint(p: Point): Point = {
      val z = p.z
      val a = if (z < height/2) helixAngle * z
              else helixAngle * (height - z)
      val x = p.x * cos(a) - p.y * sin(a)
      val y = p.x * sin(a) + p.y * cos(a)
      Point(x, y, z)
    }
    Polyhedron(stepped.faces.map( f => Face(turnPoint(f.p1), turnPoint(f.p2), turnPoint(f.p3)) ))
  }
  
}

object Gear {

  /** Simplified interface for spur gear (try to guess some parameters).
   *  To mesh gears of different sizes, the pitch/nbrTooths ratio must be the same for all the gears.
   */
  def spur(pitch: Double, nbrTooths: Int, height: Double, backlash: Double) = {
    val add = pitch * 2 / nbrTooths
    InvoluteGear(pitch, nbrTooths, toRadians(25), add, add, height, backlash)
  }
  
  /** Simplified interface for helical gear (try to guess some parameters)
   *  Typical helix is 0.05
   *  To mesh gears of different sizes, the pitch/nbrTooths and pitch/helix ratio must be the same for all the gears.
   */
  def helical(pitch: Double, nbrTooths: Int, height: Double, helix: Double, backlash: Double) = {
    val add = pitch * 2 / nbrTooths
    HelicalGear(pitch, nbrTooths, toRadians(25), add, add, height, helix, backlash)
  }
  
  /** simplified interface for herringbone gear (try to guess some parameters)
   *  To mesh gears of different sizes, the pitch/nbrTooths and pitch/helix ratio must be the same for all the gears.
   */
  def herringbone(pitch: Double, nbrTooths: Int, height: Double, helix: Double, backlash: Double) = {
    val add = pitch * 2 / nbrTooths
    HerringboneGear(pitch, nbrTooths, toRadians(25), add, add, height, helix, backlash)
  }
  
/* some examples
  def main(args: Array[String]) {
    val obj = spur(10, 12, 2, 0.1)
    //val obj = spur(20, 30, 5, 0.1)
    //val obj = helical(10, 18, 5, 0.05)
    //val obj = herringbone(10, 18, 5, 0.05)
    //val obj = InvoluteGear(10, 12, toRadians(25), 1.5, 1.5, 2, 0.1)
    //val obj = InvoluteGear(10, 30, toRadians(25), 1, 1, 5, 0)
    //val obj = InvoluteGear(10, 12, toRadians(40), 1.5, 1.5, 2, 0.1, toRadians(-15))
    //val obj = InvoluteGear(10, 18, toRadians(30), 1.2, 1.2, 2, 0.1, toRadians(-12))
    //val obj = InvoluteGear(10, 18, toRadians(30), 1.2, 1.2, 2, 0.1, toRadians(12))
    //val obj = HelicalGear(10, 12, toRadians(25), 1.5, 1.5, 10, 0.1, 0.1, 0, 0.4)
    //val obj = HerringboneGear(10, 12, toRadians(25), 1.5, 1.5, 10, Pi/20, 0.1, 0, 0.4)
    backends.OpenSCAD.view(obj)
    //backends.OpenSCAD.toSTL(obj, "wheel.stl")
  }
*/

}
