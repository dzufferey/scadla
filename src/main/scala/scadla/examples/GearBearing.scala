package scadla.examples

import math._
import scadla._
import utils._
import utils.gear._
import InlineOps._

//inspired by Emmet's Gear Bearing (http://www.thingiverse.com/thing:53451)

object GearBearing {

  def apply(outerRadius: Double,
            height: Double,
            nbrPlanets: Int,
            nbrTeethPlanet: Int,
            nbrTeethSun: Int,
            helixAngleOuter: Double,
            pressureAngle: Double,
            centerHexagonMinRadius: Double,
            backlash: Double) = {
    new GearBearing(outerRadius, height, nbrPlanets, nbrTeethPlanet, nbrTeethSun,
                    helixAngleOuter, pressureAngle, centerHexagonMinRadius, backlash)
  }

  def main(args: Array[String]) {
    //val gears = apply(35, 10, 5, 10, 15, 0.02, toRadians(40), 5, 0.1)
    val gears = apply(35, 10, 5, 6, 10, 0.02, toRadians(60), 5, 0.1)
    backends.OpenSCAD.toSTL(gears.outer,  "outer.stl")
    backends.OpenSCAD.toSTL(gears.planet, "planet.stl")
    backends.OpenSCAD.toSTL(gears.sun,    "sun.stl")
  }

}

class GearBearing(val outerRadius: Double,
                  val height: Double,
                  val nbrPlanets: Int,
                  val nbrTeethPlanet: Int,
                  val nbrTeethSun: Int,
                  val helixAngleOuter: Double,
                  val pressureAngle: Double,
                  val centerHexagonMinRadius: Double,
                  val backlash: Double) {

  //play with addenum to allow pressureAngle > 45 degree
  protected def addenum(pitch: Double, nbrTeeth: Int) = {
    val default = Gear.addenum( pitch, nbrTeeth)
    val toothWidth = pitch.abs * 2 * sin(Pi/nbrTeeth/2)
    val coeff = min(1.0, toothWidth / 2 / default / tan(pressureAngle))
    //println("coeff " + coeff)
    default * coeff
  }

  protected def gear(pitch: Double, nbrTeeth: Int, helix: Double) = {
    val add = addenum( pitch, nbrTeeth)
    HerringboneGear(pitch, nbrTeeth, pressureAngle, add, add, height, helix, backlash)
  }

  //constants
  val sunToPlanetRatio = nbrTeethSun.toDouble / nbrTeethPlanet
  val planetRadius = outerRadius / (2 + sunToPlanetRatio)
  val sunRadius = planetRadius * sunToPlanetRatio
  val nbrTeethOuter = 2 * nbrTeethPlanet + nbrTeethSun
  val helixAnglePlanet = helixAngleOuter * outerRadius / planetRadius
  val helixAngleSun = -(helixAngleOuter * outerRadius / sunRadius)

  def externalRadius = outerRadius + addenum(outerRadius, nbrTeethOuter) + Gear.baseThickness

  //TODO check for interferences

  def outer = gear(-outerRadius, nbrTeethOuter, helixAngleOuter)
  def planet = gear(planetRadius, nbrTeethPlanet, helixAnglePlanet) 
  def sun = {
    val sunCenter = Hexagon(centerHexagonMinRadius + backlash, height).rotateX(Pi).moveZ(10)
    gear(sunRadius, nbrTeethSun, helixAngleSun) - sunCenter
  }

  protected def positionPlanet(p: Solid) = {
    val r = sunRadius+planetRadius
    val α = 2 * Pi / nbrPlanets
    val β = -α * outerRadius / planetRadius
    for (i <- 0 until nbrPlanets) yield p.rotateZ(i*β).moveX(r).rotateZ(i*α)
  }

  def all = {
    val p = planet
    val planets = positionPlanet(p)
    outer ++ planets + sun //TODO the sun should also rotate a bit ???
  }

  def planetHelper(baseThickness: Double, tolerance: Double) = {
    val add = addenum(outerRadius, nbrTeethOuter) + tolerance
    val planet = Cylinder(planetRadius+add, height).moveZ(baseThickness)
    val planets = positionPlanet(planet)
    Tube(outerRadius - add, sunRadius + add, height) -- planets
  }

}
