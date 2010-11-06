package org.megrez.server

import _root_.main.scala.org.megrez.server.trigger.VersionControl
import java.io.File

abstract class Material(val url: String) {
  def versionControl(workingDir: File): VersionControl
}

