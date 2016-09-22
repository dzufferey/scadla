package scadla.examples.extrusion

object Demo {

  def main(args: Array[String]) {
    val r = scadla.backends.Renderer.default
    r.view(_2020(100))
    r.view(C(20,20,10,4)(100))
    r.view(H(20,20,4)(100))
    r.view(L(20,20,4)(100))
    r.view(T(20,20,4)(100))
    r.view(U(20,20,4)(100))
    r.view(Z(20,20,4)(100))
  }

}
