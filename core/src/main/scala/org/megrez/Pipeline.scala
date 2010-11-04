package org.megrez

import org.megrez.Pipeline.Stage

class Pipeline(val name: String, val materials: Set[Material], val stages: List[Stage])

object Pipeline {
  class Stage(val name: String)
}

class Job(val name: String, val tasks: List[Task])

trait Task {
  def execute() : Boolean
  def cancel() 
}

trait Reader[Resource, Format] {
  def read(representation: Format): Resource
}
