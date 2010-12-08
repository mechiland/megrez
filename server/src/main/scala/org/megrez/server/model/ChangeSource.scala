package org.megrez.server.model

import data.{Pluggable, Entity}
import java.io.File

abstract class ChangeSource extends Entity {
  def getChange(workingDir: File, material : Material): Option[Change]

  def toChangeSource : org.megrez.ChangeSource
}

object ChangeSource extends Pluggable[ChangeSource]