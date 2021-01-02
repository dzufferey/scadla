package scadla.utils.box

import scadla._
import squants.space.Length
import squants.space.Millimeters

case class Interval(min: Length, max: Length) {

  //TODO consider replacing Interval with QuantityRange, PR missing operators into squants
  //TODO better handling of degenerate case where min==max

  def isEmpty = min > max

  def contains(x: Length) = x >= min && x <= max

  def size = (max - min).max(min.unit(0))

  def contains(i: Interval) =
    i.isEmpty || (i.min >= min && i.max <= max)

  def overlaps(i: Interval) =
    isEmpty || i.isEmpty ||
    (min <= i.max && i.min <= max)

  def center = (max - min) / 2

  def move(x: Length) =
    if (isEmpty) this
    else Interval(x + min, x + max)

  def scale(x: Double) =
    if (isEmpty) this
    else Interval(x * min, x * max)

  def intersection(b: Interval) = Interval(min.max(b.min), max.min(b.max))

  def add(b: Interval) =
    if (isEmpty || b.isEmpty) Interval.empty
    else Interval(min + b.min, max + b.max)

  def hull(b: Interval) =
    if (isEmpty) b
    else if (b.isEmpty) this
    else Interval(min.min(b.min), max.max(b.max))

  // hull(this \ b)
  def remove(b: Interval) =
    if (b.isEmpty || b.max <= min || b.min >= max) {
      this
    } else if (b contains this) {
      Interval.empty
    } else if (b.max >= max) {
      Interval(min, b.min)
    } else if (b.min <= min) {
      Interval(b.max, max)
    } else {
      sys.error("bug in remove " + this + ", " + b)
    }

  def unionUnderApprox(b: Interval) =
    if (overlaps(b)) hull(b) else Interval.empty

}

object Interval {
  val empty = Interval(Millimeters(1), Millimeters(-1))
  val unit = Interval(Millimeters(0), Millimeters(1))
}
