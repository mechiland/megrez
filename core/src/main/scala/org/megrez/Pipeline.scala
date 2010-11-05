package org.megrez

import org.megrez.Pipeline.Stage
import java.io.File

class Pipeline(val name: String, val materials: Set[Material], val stages: List[Stage])

object Pipeline {
  class Stage(val name: String)
}

class Job(val name: String, val tasks: List[Task])

trait Task {
  def execute(workingDir : File) : Boolean
  def cancel() 
}

trait ChangeSource {
  def changes(workingDir: File): Option[Any]
}

class Material(val source : ChangeSource, val destination : String) {
  def this(source : ChangeSource) = this(source, "$main")
}

trait Reader[Resource, Format] {
  def read(representation: Format): Resource
}
