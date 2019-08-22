package scadla.utils.gear

import scadla._
import scadla.InlineOps._
import scadla.utils._
import scala.math._
import squants.space.Length
import squants.space.Millimeters
import squants.space.Angle
import squants.space.Radians

object InvoluteGear {

  protected def placeOnInvolute(pitch: Length, profile: Solid, angle: Angle) = {
    val _x = Involute.x(pitch, 0, angle.toRadians)
    val y = Involute.y(pitch, 0, angle.toRadians)
    profile.rotateZ(angle).move(_x, y, Millimeters(0))
  }

  protected def makeToothCarvingProfile(pitch: Length, profile: Solid) = {
    val samples = Gear.toothProfileAccuracy
    assert(samples > 1, "toothProfileAccuracy must be larger than 1")
    val range = Radians(2*Pi/3) //TODO vary angle and samples according to pressureAngle
    val trajectory = for (i <- 1 until samples) yield {
      val a = Radians(0) - range / 2 + i * range / samples
      val s = placeOnInvolute(pitch.abs, profile, a)
      if (pitch.value > 0) s else s.moveX(2*pitch)
    }
    val hulled = trajectory.sliding(2).map( l => if (l.size > 1) Hull(l:_*) else l.head ).toSeq
    Union(hulled:_*)
  }
  
  /** Create a gear by carving the tooths along an involute curve.
   * The method to generate spur gear inspired by
   *  http://lcamtuf.coredump.cx/gcnc/ and
   *  http://www.hessmer.org/blog/2014/01/01/online-involute-spur-gear-builder/
   * It is a certain computation cost but has the advantage of properly generating the fillet and undercut.
   * @param baseShape the base cylinder/tube for the gear
   * @param pitch the effective radius of the gear
   * @param nbrTeeth the number of tooth in the gear
   * @param rackToothProfile the profile of a tooth on a rack (infinite gear) the profile must be centered ad 0,0.
   */
  def carve( baseShape: Solid,
             pitch: Length,
             nbrTeeth: Int,
             rackToothProfile: Solid) = {
    val negative = makeToothCarvingProfile(pitch, rackToothProfile)
    
    val angle = Radians(Pi) / nbrTeeth //between tooths
    val negatives = for (i <- 0 until nbrTeeth) yield negative.rotateZ((2 * i) * angle)

    baseShape -- negatives
  }

  /** Create an involute spur gear.
   * @param pitch the effective radius of the gear
   * @param nbrTeeth the number of tooth in the gear
   * @param pressureAngle the angle between meshing gears at the pitch radius (0 mean "square" tooths, π/2 no tooths)
   * @param addenum how much to add to the pitch to get the outer radius of the gear
   * @param dedenum how much to remove to the pitch to get the root radius of the gear
   * @param height the height of the gear
   * @param backlash add some space (manufacturing tolerance)
   * @param skew generate a gear with an asymmetric profile by skewing the tooths
   */
  def apply( pitch: Length,
             nbrTeeth: Int,
             pressureAngle: Double,
             addenum: Length,
             dedenum: Length,
             height: Length,
             backlash: Length,
             skew: Angle = Radians(0.0)) = {

    assert(addenum.value > 0, "addenum must be greater than 0")
    assert(dedenum.value > 0, "dedenum must be greater than 0")
    assert(nbrTeeth > 0, "number of tooths must be greater than 0")
    assert(pitch != 0.0, "pitch must be different from 0")
    
    val angle = Pi / nbrTeeth //between tooths
    val effectivePitch = pitch.abs
    val toothWidth = effectivePitch * 2 * sin(angle/2) //TODO is that right or should we use the cordal value ?
    val ad = if (pitch.value >= 0) addenum else dedenum 
    val de = if (pitch.value >= 0) dedenum else addenum 
    val rackTooth = Rack.tooth(toothWidth, pressureAngle, ad, de, height, backlash, skew)

    if (pitch == Millimeters(0)) {
      val space = 2*toothWidth
      val teeth = for (i <- 0 until nbrTeeth) yield rackTooth.moveX(i * space)
      val bt = dedenum + Gear.baseThickness
      val base = Cube((nbrTeeth+1) * space, bt, height).move(-space/2, -bt, Millimeters(0))
      base ++ teeth
    } else {
      //TODO
      import backends.renderers.OpenScad._
      import backends.renderers.Renderable._
      val base =
        if (pitch.value > 0) Cylinder(pitch + addenum, height)
        else Tube(effectivePitch + addenum+Gear.baseThickness, effectivePitch - dedenum, height).toSolid
      carve(base, pitch, nbrTeeth, rackTooth)
    }
  }

  /** An involute gear with z tiled into many layers. */
  def stepped( pitch: Length,
               nbrTeeth: Int,
               pressureAngle: Double,
               addenum: Length,
               dedenum: Length,
               height: Length,
               backlash: Length,
               skew: Angle = Radians(0.0)) = {
    val zStep = Gear.zResolution
    assert(zStep.value > 0.0, "zResolution must be greater than 0")
    val stepCount = ceil(height / zStep).toInt
    val stepSize = height / stepCount
    def isZ(p: Point, z: Length) = (p.z - z).abs.value <= 1e-10 //TODO better way of dealing with numerical error
    val base = apply(pitch, nbrTeeth, pressureAngle, addenum, dedenum, height, backlash, skew)
    val (bot, rest) = base.toPolyhedron.faces.partition{ case Face(p1, p2, p3) => isZ(p1, Millimeters(0)) && isZ(p2, Millimeters(0)) && isZ(p3, Millimeters(0)) }
    val (top, middle) = rest.partition{ case Face(p1, p2, p3) => isZ(p1, height) && isZ(p2, height) && isZ(p3, height) }

    def mvz(i: Int, z: Length) = {
      if ((z - height).abs.value <= 1e-10) {
        if (i == stepCount - 1) z
        else (i+1) * stepSize
      } else {
        i * stepSize
      }
    }
    def mvp(i: Int, p: Point) = Point(p.x, p.y, mvz(i, p.z))
    def mv(i: Int, f: Face) = Face(mvp(i, f.p1), mvp(i, f.p2), mvp(i, f.p3))

    val newMiddle = middle.flatMap( f => for (i <- 0 until stepCount) yield mv(i, f) )
    val faces = bot ++ top ++ newMiddle
    //TODO why/when do we need to flip ?
    //TODO we need to have a short analysis that compute the normals ...
    Polyhedron(faces.map(_.flipOrientation))
    //Polyhedron(faces)
  }

}

