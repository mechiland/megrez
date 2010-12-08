package org.megrez.server.model

import data.{Pluggable, Entity}
trait Change extends Entity {
  val material : Material

  def toChange : Option[Any]
}

object Change extends Pluggable[Change]   
