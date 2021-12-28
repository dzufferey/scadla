package scadla.backends.almond

import com.github.dzufferey.x3DomViewer.X3D
import scalatags.Text.all._
import squants.space.{Millimeters, LengthUnit}

class Config extends com.github.dzufferey.x3DomViewer.Config {

   var shapeAppearance = X3D.appearance(X3D.material(X3D.diffuseColor := "0.7 0.7 0.7"))
   val unit: LengthUnit = Millimeters
}
