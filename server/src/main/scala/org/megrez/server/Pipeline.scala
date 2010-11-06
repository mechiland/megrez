package org.megrez.server

import java.io.File
import org.megrez.{Job, Task}

class Pipeline(val name: String, val material: Material, val stages: List[Pipeline.Stage]) {
  private def workingDir() = {
    new File(System.getProperty("user.dir") + "/pipelines/" + name)
  }
  def checkChange() = material.versionControl(workingDir()).checkChange
}

object Pipeline {
  case class Stage(val name: String, val jobs: Set[Job])

}

