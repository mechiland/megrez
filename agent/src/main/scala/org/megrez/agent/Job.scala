package org.megrez.agent

import java.io.File

case class Job(val tasks : List[Task]) 

abstract class Task {
  def run(workingDir : File)
}