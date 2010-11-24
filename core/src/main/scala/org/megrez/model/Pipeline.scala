package org.megrez.model

trait Pipeline {
  val name : String
  val stages : List[_ <: Stage]
}

trait Stage {
  val name : String
  val jobs : Set[_ <: Job]
}

trait Job {
  val name : String
  val tasks : List[_ <: Task]
}

trait Task {
  
}