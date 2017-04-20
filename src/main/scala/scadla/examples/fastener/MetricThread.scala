package scadla.examples.fastener

import scadla._
import scadla.utils._
import InlineOps._
import Trig._
import squants.space.{Length, Angle}
import scala.language.postfixOps
import squants.space.LengthConversions._

/** Generate threaded rods, screws and nuts.
 * based on metric_iso_screw.scad by stth
 *     http://www.thingiverse.com/thing:269863
 * which extends polyScrewThread_r1.scad by aubenc
 *     http://www.thingiverse.com/thing:8796
 * CC Public Domain
 */
class MetricThread(tolerance: Length = 0.05 mm, fn: Int = 30) {
  
  /** Creates a thread with one rotation below z = 0 and one rotation above the number of rotations
   * a cylinder fills the thread, if icut &gt; 0, the valleys are stump
   * 
   * @param ttn  number of rotations between 0 and ttn*st
   * @param st   thread pitch
   * @param sn   number of shapes per revolution ($fn)
   * @param or   radius of the outer thread peak independent of the icut or ocut
   * @param ir   radius of the inner thread peak independent of the icut or ocut
   * @param icut amount to cut from the inner thread peaks
   *
   * when rendering $fn should be 30 minimum (do not use $fs or $fa)
   */
  def fullThread(ttn: Int,
                  st: Length,
                  sn: Int,
                  or: Length,
                  ir: Length,
                  icut: Length ) = {
    // pitch per rotation
    val zt = st / sn
    // revolution angle of one shape
    val lfxy = 2 * Pi / sn

    assert(ir >= (0.2 mm), """Step Degrees too agresive, the thread will not be made!!
  try to:
    increase de value for the degrees and/or...
    decrease the pitch value and/or...
    increase the outer diameter value!""" )


    def mkPoly(i: Int, j: Int) = {
      // j+1 for trigonometric functions
      // rounding errors make ghost shapes if j+1 is used
      val j1_a = (j+1) % sn
      // j+1 for absolute coordinates
      val j1_s = j+1

      val pt = Array(
        // lower inner thread peak
        Point(ir * cos(j*lfxy),    ir * sin(j*lfxy),    (i-1)  *st + j*zt),
        Point(ir * cos(j1_a*lfxy), ir * sin(j1_a*lfxy), (i-1)  *st + j1_s*zt),
        // middle outer thread peak
        Point(or * cos(j*lfxy),    or * sin(j*lfxy),    (i-0.5)*st + j*zt),
        Point(or * cos(j1_a*lfxy), or * sin(j1_a*lfxy), (i-0.5)*st + j1_s*zt),
        // upper inner thread peak
        Point(ir * cos(j*lfxy),    ir * sin(j*lfxy),    i      *st + j*zt),
        Point(ir * cos(j1_a*lfxy), ir * sin(j1_a*lfxy), i      *st + j1_s*zt)
      )

      def face(a: Int, b: Int, c: Int) = Face(pt(a), pt(b), pt(c))

      val triangles = Array(
        // below
        face(0, 2, 1), face(1, 2, 3),
        // above
        face(3, 2, 4), face(3, 4, 5),
        // right/left
        face(5, 1, 3), face(0, 4, 2),
        // inner
        face(1, 4, 0), face(1, 5, 4)
      )

      Polyhedron(triangles)
    }

    val thread = for(i <- 0 until ttn; j <- 0 until sn) yield mkPoly(i, j)

    val r = ir + icut
    val center = Translate(0 mm, 0 mm, -st, Cylinder(r, (ttn+1)*st))

    Union( (center +: thread):_* )
  }

  /** Creates the filling shape for a <b>screw</b> thread according to the mode in parameter cs
   *
   * @param cs   decides
   *             -- whether a inner or outer thread is created
   *             and
   *             -- whether a cone-cut at the ends of the thread is applied (for easier handling)
   *             values:
   *             --   -2  for nut: none
   *             --   -1  for screw: only on the head side of the thread
   *             --    0  for screw: none
   *             --    1  for screw: only at the end of the screw
   *             --    2  for screw: at both ends if the thread
   * @param lt   length of thread
   * @param or   radius of outer thread helix independent of the icut or ocut
   * @param ir   radius of inner thread helix independent of the icut or ocut
   * @param st   thread pitch
   * @param ocut amount to cut from the outer thread peaks
   *
   * when rendering $fn should be 30 minimum (do not use $fs or $fa)
   */
  def threadShape(cs: Int, lt: Length, or: Length, ir: Length, st: Length, ocut: Length) = {
    if ( cs == 0 ) {
      Cylinder(or-ocut, lt)
    } else {
      Intersection(
        Union(
          Cylinder(or, lt-st+(0.005 mm)).moveZ(st/2),

          if ( cs == -1 || cs == 2 ) Cylinder(ir, or, st/2)
          else Cylinder(or, st/2),
          
          Translate(0 mm, 0 mm, lt-st/2,
            if ( cs == 1 || cs == 2 ) Cylinder(or, ir, st/2)
            else Cylinder(or, st/2)
          )
        ),
        
        Cylinder(or-ocut, lt+st).moveZ(-st/2.0)
      )
    }
  }


  /** Creates a screw thread and it's filling according to the mode in parameter cs
   *
   * @param od   outer diameter (thread peak to thread peak) independent of the icut or ocut
   * @param st   thread pitch
   * @param lf0  thread angle
   * @param lt   length of thread
   * @param cs   decides
   *             -- if a inner or outer thread is created
   *             and
   *             -- if a cone-cut at the ends of the thread is applied (for easier handling)
   *             values:
   *             --   -2  for nut: none
   *             --   -2  for screw: not even flat ends
   *             --   -1  for screw: only on the head side of the thread
   *             --    0  for screw: none
   *             --    1  for screw: only at the end of the screw
   *             --    2  for screw: at both ends if the thread
   * @param icut amount to cut from the inner thread peaks
   * @param ocut amount to cut from the outer thread peaks
   *
   * assume for rendering $fn should be 30 minimum (do not use $fs or $fa)
   */
  def screwThread(od: Length, st: Length, lf0: Angle, lt: Length, cs: Int, icut: Length, ocut: Length) = {
    // radius of outer thread helix
    val or = od/2
    // radius of inner thread helix
    val ir = or - st/2*cos(lf0)/sin(lf0)
    // number of revolutions
    val ttn = math.ceil(lt/st).toInt + 1
    
    assert(od > (0 mm), "outer diameter (od = "+od+") <= 0")
    assert(st > (0 mm), "thread pitch (st = "+st+") <= 0")
    assert(lf0 > (0°) && lf0 < (90°), "thread angle (fl0 = "+lf0+") not between 0 and π/2 (non-inclusive), try π/6!")
    assert(lt > (0 mm), "thread length (lt = "+lt+") <= 0")
    assert(cs >= -2 && cs <= 2, "invalid mode (cs = "+cs+"), try -2, -1, 0, 1, or 2!")
    assert(icut >= (0 mm), "inner thread plane cut (icut = "+icut+") <= 0, try 0 or litte greater!")
    assert(ocut >= (0 mm), "outer thread plane cut (ocut = "+ocut+") <= 0, try 0 or litte greater!")
    assert(icut < or-ir, "inner thread plane cut (icut = "+icut+") to big, try 0 or litte greater!")
    assert(ocut < or-ir, "outer thread plane cut (ocut = "+ocut+") to big, try 0 or litte greater!")
    assert(icut+ocut <= or-ir, "inner plus outer thread plane cut (icut + ocut = "+(icut+ocut)+") to big, try 0 or litte greater for both")

    Intersection(
      threadShape(cs, lt, or, ir, st, ocut),
      fullThread(ttn, st, fn, or, ir, icut)
    )
  }

  /** Creates a hexagon head for a screw or a nut
   * 
   * @param hg   height of the head
   * @param df   width across flat (take 0.1 less!)
   *
   * assume for rendering $fn should be 30 minimum (do not use $fs or $fa)
   */
  def hexHead(hg: Length, df: Length) = {
    Intersection(
      Hexagon(df/2, hg),
      Cylinder(df/2 + hg, df/2, hg),
      Cylinder(df/2, df/2 + hg, hg)
    )
  }

  /** Creates cones to cut from thread ends of a nut (for easier handling)
   * 
   * @param chg  height of the cone
   * @param od   outer diameter of the thread
   * @param lf0  angle of the thread to cut (uses same angle for cones)
   * @param hg   height of the thread
   *
   * assume for rendering $fn should be 30 minimum (do not use $fs or $fa)
   */
  def hexCountersinkEnds(chg: Length, od: Length, lf0: Angle, hg: Length) = {
    // overlength of cones
    val olen = (0.1 mm)
    Union(
      // lower cone
      Cylinder( od/2,
                od/2 - (chg+olen)*cos(lf0)/sin(lf0),
                chg+olen
      ).moveZ(-olen),
      // upper cone
      Cylinder( od/2 - (chg+olen)*cos(lf0)/sin(lf0),
                od/2,
                chg+olen
      ).moveZ(hg-chg+olen)
    )
  }

  /** Creates a nut
   * 
   * @param df   width across flat (take 0.1 less!)
   * @param hg   height of the nut,
   * @param st   thread pitch
   * @param lf0  thread angle
   * @param od   outer diameter of the thread independent of the icut or ocut
   * @param icut amount to cut from the inner thread peaks
   * @param ocut amount to cut from the outer thread peaks
   *
   * assume for rendering $fn should be 30 minimum (do not use $fs or $fa)
   */
  def hexNut(df: Length, hg: Length, st: Length, lf0: Angle, od: Length, icut: Length, ocut: Length) = {
    Difference(
      hexHead(hg, df),
      hexCountersinkEnds(st/2, od, lf0, hg),
      screwThread(od+tolerance, st, lf0, hg, -2, icut, ocut)
    )
  }

  /** Creates a screw
   *
   * @param od   outer diameter (thread peak to thread peak) independent of the icut or ocut
   * @param st   thread pitch
   * @param lf0  thread angle
   * @param lt   length of thread
   * @param cs   create cone-cut at the end of thread (for easier handling)
   *             --   -2  not even flat ends
   *             --   -1  only on the head side of the thread
   *             --    0  none
   *             --    1  only at the end of the screw
   *             --    2  at both ends if the thread
   * @param df   width across flat (take 0.1 less!)
   * @param hg   height of the head,
   * @param ntl  length of the part between head and upper end of thread
   * @param ntd  diameter of the part between head and upper end of thread
   *             --&gt;0  explicit diameter
   *             --    0  use value of parameter od
   *             --   -1  use diameter of the thread valleys
   * @param icut  amount to cut from the inner thread peaks
   * @param ocut  amount to cut from the outer thread peaks
   *
   * assume for rendering $fn should be 30 minimum (do not use $fs or $fa)
   */
  def hexScrew(od: Length, st: Length, lf0: Angle, lt: Length,
               cs: Int, df: Length, hg: Length, ntl: Length,
               ntd: Length, icut: Length, ocut: Length) = {

    val ntr = od/2 - (st/2)*cos(lf0)/sin(lf0) + icut

    Union(
      hexHead(hg, df),
         
      Translate(0 mm, 0 mm, hg,
        if ( ntl == (0 mm) ) Cylinder(ntr, 0.01 mm)
        else if ( ntd == (-1 mm) ) Cylinder(ntr, ntl+ (0.01 mm))
        else if ( ntd == (0 mm) ) {
          val r = od/2 - ocut
          Union(
            Cylinder(r, ntl-st/2),
            Cylinder( r, ntr, st/2).moveZ(ntl-st/2)
          )
        } else Cylinder(ntd/2, ntl)
      ),

      screwThread(od-tolerance, st, lf0, lt, cs, icut, ocut).moveZ(ntl+hg)
    )
  }

  /** Returns the thread pitch for an ISO M-Number */
  val getIsoPitch = Map[Length, Length](
    ( 1.0 mm) -> (0.25 mm),
    ( 1.2 mm) -> (0.25 mm),
    ( 1.6 mm) -> (0.35 mm),
    ( 2.0 mm) -> (0.4  mm),
    ( 2.5 mm) -> (0.45 mm),
    ( 3.0 mm) -> (0.5  mm),
    ( 4.0 mm) -> (0.7  mm),
    ( 5.0 mm) -> (0.8  mm),
    ( 6.0 mm) -> (1.0  mm),
    ( 8.0 mm) -> (1.25 mm),
    (10.0 mm) -> (1.5  mm),
    (12.0 mm) -> (1.75 mm),
    (16.0 mm) -> (2.0  mm),
    (20.0 mm) -> (2.5  mm),
    (24.0 mm) -> (3.0  mm),
    (30.0 mm) -> (3.5  mm),
    (36.0 mm) -> (4.0  mm),
    (42.0 mm) -> (4.5  mm),
    (48.0 mm) -> (5.0  mm),
    (56.0 mm) -> (5.5  mm),
    (64.0 mm) -> (6.0  mm)
  )

  /** returns the width across flat (tool size) for an ISO M-Number (take 0.1 less!) */ 
  val getIsoWaf = Map[Length, Length]( 
    ( 1.0 mm) -> ( 2.0 mm),
    ( 1.2 mm) -> ( 2.5 mm),
    ( 1.6 mm) -> ( 3.2 mm),
    ( 2.0 mm) -> ( 4.0 mm),
    ( 2.5 mm) -> ( 5.0 mm),
    ( 3.0 mm) -> ( 5.5 mm),
    ( 4.0 mm) -> ( 7.0 mm),
    ( 5.0 mm) -> ( 8.0 mm),
    ( 6.0 mm) -> (10.0 mm),
    ( 8.0 mm) -> (13.0 mm),
    (10.0 mm) -> (17.0 mm),
    (12.0 mm) -> (19.0 mm),
    (16.0 mm) -> (24.0 mm),
    (20.0 mm) -> (30.0 mm),
    (24.0 mm) -> (36.0 mm),
    (30.0 mm) -> (46.0 mm),
    (36.0 mm) -> (55.0 mm),
    (42.0 mm) -> (65.0 mm),
    (48.0 mm) -> (75.0 mm),
    (56.0 mm) -> (85.0 mm),
    (64.0 mm) -> (96.0 mm)
  )

  /** Creates an outer (screw) iso screw thread and it's filling according to the mode in parameter cs
   * the fillet in the valleys is not round but a flat.
   * it has the same depht as the lowest part of the round would have.
   *
   * @param d    ISO diameter M[od] (not radius!)
   * @param lt   length of thread
   * @param cs   create cone-cut at the end of thread (for easier handling)
   *             --   -2  not even flat ends
   *             --   -1  only on the head side of the thread
   *             --    0  none
   *             --    1  only at the end of the screw
   *             --    2  at both ends if the thread
   *
   * assume for rendering $fn should be 30 minimum (do not use $fs or $fa)
   */
  def screwThreadIsoOuter(d: Length, lt: Length, cs: Int) = {
    // pitch
    val st = getIsoPitch(d)
    // thread peak to thread peak (without cuts)
    val t = st*cos(Pi/6)
    
    // ratio of (height of the 120 degree circle segment) and (its radius)
    // h/r = 2 sin(120°/4)^2 = 0.5
    // hfree_div_r = 0.5;
    
    screwThread(
      d + t/8 - tolerance,
      st,
      Pi/6,
      lt,
      cs,
      // flat of the nut - height of the arc of the screw
      // t/4 - hfree_div_r*t/6 = t/6
      t/6,
      t/8
    )
  }

  /** Creates an inner (nut) iso screw thread and it's filling according to the mode in parameter cs
   * the fillet in the valleys is not round but a flat.
   * it has the same depht as the lowest part of the round would have.
   * 
   * it must be cut out of a solid
   *
   * @param d    ISO diameter M[od] (not radius!)
   * @param lt   length of thread
   *
   * assume for rendering $fn should be 30 minimum (do not use $fs or $fa)
   */
  def screwThreadIsoInner(d: Length, lt: Length) = {
    // pitch
    val st = getIsoPitch(d)
    
    // thread peak to thread peak (without cuts)
    val t = st*cos(Pi/6)
    
    // ratio of (height of the 120 degree circle segment) and (its radius)
    // h/r = 2 sin(120°/4)^2 = 0.5
    // hfree_div_r = 0.5;
    
    screwThread(
      d + t/8 + tolerance,
      st,
      Pi/6,
      lt,
      0,
      t/4,
      // flat of the screw - height of the arc of the nut
      // t/8 - hfree_div_r*t/12 = t/12
      t/12
    )
  }

  /** Creates a screw with hex head an iso thread
   * the fillet in the valleys is not round but a flat.
   * it has the same depht as the lowest part of the round would have.
   *
   * @param d    ISO diameter M[od] (not radius!)
   *             using values between the standardised diameters
   *             may result in strange measures for the width across flat (tool size)
   * @param lt   length of thread
   * @param cs   create cone-cut at the end of thread (for easier handling)
   *             --   -1  only on the head side of the thread
   *             --    0  none
   *             --    1  only at the end of the screw
   *             --    2  at both ends if the thread
   * @param ntl  length of the part between head and upper end of thread
   * @param ntd  diameter  of the part between head and upper end of thread
   *             --&gt;0  explicit diameter
   *             --    0  use value of parameter od
   *             --   -1  use diameter of the thread valleys
   * @param hg   height of the head
   *
   * assume for rendering $fn should be 30 minimum (do not use $fs or $fa)
   */
  def hexScrewIso(d: Length, lt: Length, cs: Int, ntl: Length, ntd: Length, hg: Length) = {
    // pitch
    val st = getIsoPitch(d)
    
    // thread peak to thread peak (without cuts)
    val t = st*cos(Pi/6)
    
    // ratio of (height of the 120 degree circle segment) and (its radius)
    // h/r = 2 sin(120°/4)^2 = 0.5
    // hfree_div_r = 0.5;
    
    hexScrew(
      d + t/8,
      st,
      Pi/6,
      lt,
      cs,
      getIsoWaf(d) - (0.1 mm),
      hg,
      ntl,
      ntd,
      // flat of the nut - height of the arc of the screw
      // t/4 - hfree_div_r*t/6 = t/6
      t/6,
      t/8
    )
  }

  /** Creates a hex nut with an iso thread
   * the fillet in the valleys is not round but a flat.
   * it has the same depht as the lowest part of the round would have.
   *
   * @param d    ISO diameter M[od] (not radius!)
   *             using values between the standardised diameters
   *             may result in strange measures for the width across flat (tool size)
   * @param hg   height of the head
   *
   * assume for rendering $fn should be 30 minimum (do not use $fs or $fa)
   */
  def hexNutIso(d: Length, hg: Length) = {
    // pitch
    val st = getIsoPitch(d)
    
    // thread peak to thread peak (without cuts)
    val t = st*cos(Pi/6)
    
    // ratio of (height of the 120 degree circle segment) and (its radius)
    // h/r = 2 sin(120°/4)^2 = 0.5
    // hfree_div_r = 0.5;
    
    hexNut(
      getIsoWaf(d) - (0.1 mm),
      hg,
      st,
      Pi/6,
      d+t/8,
      t/4,
      // flat of the screw - height of the arc of the nut
      // t/8 - hfree_div_r*t/12 = t/12
      t/12
    )

  }

}

object MetricThread {

  //TODO some screws by name

  def renderingOption = List("$fn=30;")
  def renderer = new backends.OpenSCAD(renderingOption)

  def demo = {

    val m = new MetricThread()

    val set = Map(
      1-> (1 mm),
      2-> (2 mm),
      3-> (3 mm),
      4-> (4 mm),
      5-> (1 mm),
      6-> (8 mm),
      7-> (6 mm),
      8-> (5 mm)
    )

    def boltAndNut(x: Int, y: Int) = {
      val i = 4*x + y
      val di = set(i)
      val bolt = m.hexScrewIso( di, di*2, 2, di*0.6, 0 mm, di/1.7)
      val nut = m.hexNutIso(di, di/1.2)
      val pair = bolt + nut.moveZ( di/2 + di*0.6 + 6*m.getIsoPitch(di))
      pair.translate(x*12 mm, (y-1)*12 + x*(y-2)*8 mm, 0 mm)
    }

    val all = for (x <- 0 to 1; y <- 1 to 4) yield boltAndNut(x, y)
    Union(all:_*)
  }

  def test = {
    val m = new MetricThread()
    //m.hexHead(4, 8)
    m.hexScrewIso( 8 mm, 12.5 mm, 2, 7.5 mm, 0 mm, 3 mm)
    //m.hexNutIso( 8, 4)
    //m.screwThreadIsoOuter( 8, 10, 2)
    //m.screwThreadIsoInner( 8, 6)
  }
  
  def main(args: Array[String]) {
    //val obj = test
    val obj = demo
    //render and display the wheel
    //backends.OpenSCAD.saveFile("test.scad", obj, renderingOption)
    Console.println("rendering set of metric ISO bolts and nuts. This may take a while ...")
    //new backends.ParallelRenderer(renderer).toSTL(obj, "metric_threads.stl")
    renderer.view(obj)
  }

}
