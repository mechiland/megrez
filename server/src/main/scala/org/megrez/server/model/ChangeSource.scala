package org.megrez.server.model

import data.{Pluggable, Entity}
import java.io.File

abstract class ChangeSource extends Entity with org.megrez.model.ChangeSource {
  def getChange(workingDir: File, material : Material): Option[Change]
}

object ChangeSource extends Pluggable[ChangeSource]