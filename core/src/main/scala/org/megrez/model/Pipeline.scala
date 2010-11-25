package org.megrez.model

import java.io.File

trait Pipeline {
  val name : String
  val materials : Set[_ <: Material]
  val stages : List[_ <: Stage]
}

trait Stage {
  val name : String
  val jobs : Set[_ <: Job]
}

trait Job {
  val name : String
  val resources : Set[String]
  val tasks : List[_ <: Task]
}

trait Material {
  val destination : String
  val changeSource : ChangeSource  
}

trait Task {
  
}

trait ChangeSource {
  def changes(workingDir: File, previous: Option[Any]): Option[Any]
}