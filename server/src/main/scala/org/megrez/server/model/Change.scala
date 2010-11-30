package org.megrez.server.model

import data.{Pluggable, Entity}
trait Change extends Entity {
  val material : Material
}

object Change extends Pluggable[Change]   
