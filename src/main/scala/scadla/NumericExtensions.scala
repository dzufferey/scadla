package scadla

import squants.Quantity

extension (lhs: Int)
    def *[A <: Quantity[A]](rhs : A): A = rhs * lhs

extension (lhs: Double)
    def *[A <: Quantity[A]](rhs : A): A = rhs * lhs
