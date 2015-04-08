package dzufferey.scadla.examples.fastener

import dzufferey.scadla._
import dzufferey.scadla.utils._
import math._
import InlineOps._

/** Generate threaded rods, screws and nuts.
 * based on metric_iso_screw.scad by stth
 *     http://www.thingiverse.com/thing:269863
 * which extends polyScrewThread_r1.scad by aubenc
 *     http://www.thingiverse.com/thing:8796
 * CC Public Domain
 */
class MetricThread(tolerance: Double = 0.05, fn: Int = 30) {
  
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
                  st: Double,
                  sn: Int,
                  or: Double,
                  ir: Double,
                  icut: Double ) = {
    // pitch per rotation
    val zt = st / sn
    // revolution angle of one shape
    val lfxy = 2 * Pi / sn

    assert(ir >= 0.2, """Step Degrees too agresive, the thread will not be made!!
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
    val center = Translate(0, 0, -st, Cylinder(r, r, (ttn+1)*st))

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
  def threadShape(cs: Int, lt: Double, or: Double, ir: Double, st: Double, ocut: Double) = {
    if ( cs == 0 ) {
      Cylinder(or-ocut, or-ocut, lt)
    } else {
      Intersection(
        Union(
          Cylinder(or, or, lt-st+0.005).moveZ(st/2),

          if ( cs == -1 || cs == 2 ) Cylinder(ir, or, st/2)
          else Cylinder(or, or, st/2),
          
          Translate(0, 0, lt-st/2,
            if ( cs == 1 || cs == 2 ) Cylinder(or, ir, st/2)
            else Cylinder(or, or, st/2)
          )
        ),
        
        Cylinder(or-ocut, or-ocut, lt+st).moveZ(-st/2.0)
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
  def screwThread(od: Double, st: Double, lf0: Double, lt: Double, cs: Int, icut: Double, ocut: Double) = {
    // radius of outer thread helix
    val or = od/2
    // radius of inner thread helix
    val ir = or - st/2*cos(lf0)/sin(lf0)
    // number of revolutions
    val ttn = ceil(lt/st).toInt + 1
    
    assert(od > 0, "outer diameter (od = "+od+") <= 0")
    assert(st > 0, "thread pitch (st = "+st+") <= 0")
    assert(lf0 > 0 && lf0 < Pi/2, "thread angle (fl0 = "+lf0+") not between 0 and π/2 (non-inclusive), try π/6!")
    assert(lt > 0, "thread length (lt = "+lt+") <= 0")
    assert(cs >= -2 && cs <= 2, "invalid mode (cs = "+cs+"), try -2, -1, 0, 1, or 2!")
    assert(icut >= 0, "inner thread plane cut (icut = "+icut+") <= 0, try 0 or litte greater!")
    assert(ocut >= 0, "outer thread plane cut (ocut = "+ocut+") <= 0, try 0 or litte greater!")
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
  def hexHead(hg: Double, df: Double) = {
    Intersection(
      Hexagon(df, hg),
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
  def hexCountersinkEnds(chg: Double, od: Double, lf0: Double, hg: Double) = {
    // overlength of cones
    val olen = 0.1
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
  def hexNut(df: Double, hg: Double, st: Double, lf0: Double, od: Double, icut: Double, ocut: Double) = {
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
  def hexScrew(od: Double, st: Double, lf0: Double, lt: Double,
                  cs: Int, df: Double, hg: Double, ntl: Double,
                  ntd: Double, icut: Double, ocut: Double) = {

    val ntr = od/2 - (st/2)*cos(lf0)/sin(lf0) + icut

    Union(
      hexHead(hg, df),
         
      Translate(0, 0, hg,
        if ( ntl == 0 ) Cylinder(ntr, ntr, 0.01)
        else if ( ntd == -1 ) Cylinder(ntr, ntr, ntl+0.01)
        else if ( ntd == 0 ) {
          val r = od/2 - ocut
          Union(
            Cylinder(r, r, ntl-st/2),
            Cylinder( r, ntr, st/2).moveZ(ntl-st/2)
          )
        } else Cylinder(ntd/2, ntd/2, ntl)
      ),

      screwThread(od-tolerance, st, lf0, lt, cs, icut, ocut).moveZ(ntl+hg)
    )
  }

  /** Returns the thread pitch for an ISO M-Number */
  val getIsoPitch = Map(
     1.0-> 0.25,
     1.2-> 0.25,
     1.6-> 0.35,
     2.0-> 0.4 ,
     2.5-> 0.45,
     3.0-> 0.5 ,
     4.0-> 0.7 ,
     5.0-> 0.8 ,
     6.0-> 1.0 ,
     8.0-> 1.25,
    10.0-> 1.5 ,
    12.0-> 1.75,
    16.0-> 2.0 ,
    20.0-> 2.5 ,
    24.0-> 3.0 ,
    30.0-> 3.5 ,
    36.0-> 4.0 ,
    42.0-> 4.5 ,
    48.0-> 5.0 ,
    56.0-> 5.5 ,
    64.0-> 6.0 
  )

  /** returns the width across flat (tool size) for an ISO M-Number (take 0.1 less!) */ 
  val getIsoWaf = Map[Double, Double]( 
     1.0->  2.0,
     1.2->  2.5,
     1.6->  3.2,
     2.0->  4.0,
     2.5->  5.0,
     3.0->  5.5,
     4.0->  7.0,
     5.0->  8.0,
     6.0-> 10.0,
     8.0-> 13.0,
    10.0-> 17.0,
    12.0-> 19.0,
    16.0-> 24.0,
    20.0-> 30.0,
    24.0-> 36.0,
    30.0-> 46.0,
    36.0-> 55.0,
    42.0-> 65.0,
    48.0-> 75.0,
    56.0-> 85.0,
    64.0-> 96.0
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
  def screwThreadIsoOuter(d: Double, lt: Double, cs: Int) = {
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
  def screwThreadIsoInner(d: Double, lt: Double) = {
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
  def hexScrewIso(d: Double, lt: Double, cs: Int, ntl: Double, ntd: Double, hg: Double) = {
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
      getIsoWaf(d) - 0.1,
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
  def hexNutIso(d: Double, hg: Double) = {
    // pitch
    val st = getIsoPitch(d)
    
    // thread peak to thread peak (without cuts)
    val t = st*cos(Pi/6)
    
    // ratio of (height of the 120 degree circle segment) and (its radius)
    // h/r = 2 sin(120°/4)^2 = 0.5
    // hfree_div_r = 0.5;
    
    hexNut(
      getIsoWaf(d) - 0.1,
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

  def demo = {

    val m = new MetricThread()

    val set = Map(
      1-> 1,
      2-> 2,
      3-> 3,
      4-> 4,
      5-> 1,
      6-> 8,
      7-> 6,
      8-> 5
    )

    def boltAndNut(x: Int, y: Int) = {
      val i = 4*x + y
      val di = set(i)
      val bolt = m.hexScrewIso( di, di*2, 2, di*0.6, 0, di/1.7)
      val nut = m.hexNutIso(di, di/1.2)
      val pair = bolt + nut.moveZ( di/2 + di*0.6 + 6*m.getIsoPitch(di))
      pair.translate(x*12, (y-1)*12 + x*(y-2)*8, 0)
    }

    val all = for (x <- 0 to 1; y <- 1 to 4) yield boltAndNut(x, y)
    Union(all:_*)
  }

  def test = {
    val m = new MetricThread()
    //m.hexHead(4, 8)
    m.hexScrewIso( 8, 12.5, 2, 7.5, 0, 3)
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
    backends.OpenSCAD.view(obj, renderingOption, Nil, Nil)
  }

}
