package org.megrez

import org.megrez.Pipeline.Stage
import java.io.File

class Pipeline(val name: String, val materials: Set[Material], val stages: List[Stage])

object Pipeline {
  class Stage(val name: String, val jobs : Set[Job]) {
    def before(stage: Stage)(implicit pipeline: Pipeline) = {
      pipeline.stages.indexOf(this) < pipeline.stages.indexOf(stage)
    }
  }
}

class Job(val name: String, val resources: Set[String], val tasks: List[Task], val artifacts: List[Artifact] = List())

trait Task {
  def execute(workingDir: File): String

  def cancel()
}

trait ChangeSource {
  def changes(workingDir: File, previous: Option[Any]): Option[Any]
}

class Material(val source: ChangeSource, val destination: String) {
  def this(source: ChangeSource) = this (source, "$main")
}

class Artifact(val path: String, val tags: Set[String])
