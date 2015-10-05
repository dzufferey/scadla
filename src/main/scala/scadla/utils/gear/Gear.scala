package scadla.utils.gear
  
import scadla._
import scadla.InlineOps._
import scala.math._

//some references read when coding this
//  https://en.wikipedia.org/wiki/Involute_gear
//  https://en.wikipedia.org/wiki/Gear#Nomenclature
//  https://upload.wikimedia.org/wikipedia/commons/2/28/Gear_words.png
//  https://en.wikipedia.org/wiki/List_of_gear_nomenclature
//  http://lcamtuf.coredump.cx/gcnc/ and
//  http://www.hessmer.org/blog/2014/01/01/online-involute-spur-gear-builder/

object Gear {

  /** ~accuracy of the tooth profile */
  var toothProfileAccuracy = 30

  /** This determines the number of vertical steps for helical and herringbone gear.
   *  This should more or less corresponds to the thickness of the layer when printing */
  var zResolution = 0.2

  /** default value for the addenum given the pitch and the number of teeth */
  def addenum(pitch: Double, nbrTeeth: Int) = pitch.abs * 2 / nbrTeeth
  
  /** suggested value for the pitch given the number of teeth and the addenum */
  def pitch(nbrTeeth: Int, addenum: Double) = addenum / 2 * nbrTeeth
  
  /** aprroximative value for the number of teeth given the pitch and the addenum */
  def approxNbrTeeth(pitch: Double, addenum: Double) = pitch / addenum * 2

  /** Simplified interface for spur gear (try to guess some parameters).
   *  To mesh gears of different sizes, the pitch/nbrTeeth ratio must be the same for all the gears.
   */
  def spur(pitch: Double, nbrTeeth: Int, height: Double, backlash: Double) = {
    val add = addenum( pitch, nbrTeeth)
    InvoluteGear(pitch, nbrTeeth, toRadians(25), add, add, height, backlash)
  }
  
  /** Simplified interface for helical gear (try to guess some parameters)
   *  Typical helix is 0.05
   *  To mesh gears of different sizes, the pitch/nbrTeeth and pitch/helix ratio must be the same for all the gears.
   */
  def helical(pitch: Double, nbrTeeth: Int, height: Double, helix: Double, backlash: Double) = {
    val add = addenum( pitch, nbrTeeth)
    HelicalGear(pitch, nbrTeeth, toRadians(25), add, add, height, helix, backlash)
  }
  
  /** simplified interface for herringbone gear (try to guess some parameters)
   *  To mesh gears of different sizes, the pitch/nbrTeeth and pitch/helix ratio must be the same for all the gears.
   */
  def herringbone(pitch: Double, nbrTeeth: Int, height: Double, helix: Double, backlash: Double) = {
    val add = addenum( pitch, nbrTeeth)
    HerringboneGear(pitch, nbrTeeth, toRadians(25), add, add, height, helix, backlash)
  }
  
  /** Simplified interface for rack (try to guess some parameters).
   * TODO what needs to match for gears to mesh
   */
  def rack(toothWidth: Double, nbrTeeth: Int, height: Double, backlash: Double) = {
    val add = toothWidth / 2 
    Rack(toothWidth, nbrTeeth, toRadians(25), add, add, height, backlash)
  }
  
/* some examples
  def main(args: Array[String]) {
    //val obj = spur(10, 12, 2, 0.1)
    //val obj = spur(20, 30, 5, 0.1)
    //val obj = helical(10, 18, 5, 0.05, 0.1)
    //val obj = herringbone(10, 18, 5, 0.05, 0.1)
    //val obj = InvoluteGear(10, 12, toRadians(25), 1.5, 1.5, 2, 0.1)
    //val obj = InvoluteGear(10, 30, toRadians(25), 1, 1, 5, 0)
    //val obj = InvoluteGear(10, 12, toRadians(40), 1.5, 1.5, 2, 0.1, toRadians(-15))
    //val obj = InvoluteGear(10, 18, toRadians(30), 1.2, 1.2, 2, 0.1, toRadians(-12))
    //val obj = InvoluteGear(10, 18, toRadians(30), 1.2, 1.2, 2, 0.1, toRadians(12))
    //val obj = HelicalGear(10, 12, toRadians(25), 1.5, 1.5, 10, 0.1, 0.1, 0, 0.4)
    //val obj = HerringboneGear(10, 12, toRadians(25), 1.5, 1.5, 10, Pi/20, 0.1, 0, 0.4)
    //val obj = spur(-10, 12, 2, 0.1)
    //val obj = spur(-20, 30, 2, 0.1)
    //val obj = helical(-10, 18, 5, 0.05, 0.1)
    //val obj = herringbone(-10, 18, 5, 0.05, 0.1)
    val obj = rack(2, 12, 2, 0.1)
    backends.OpenSCAD.view(obj)
  }
*/

}
