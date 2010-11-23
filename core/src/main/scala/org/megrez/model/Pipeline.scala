package org.megrez.model

trait Pipeline {
  val name : String
  val stages : List[Stage]
}

trait Stage {
  val name : String
  val jobs : Set[Job]
}

trait Job {
  val name : String
  val tasks : List[Task]
}

trait Task {
  
}