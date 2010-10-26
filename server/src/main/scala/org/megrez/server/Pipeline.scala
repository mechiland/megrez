package org.megrez.server

class Pipeline

class Job(val name : String, val resources : Set[String], val tasks : List[Task])

class Task

object Configuration {
  def hasNextStage(pipeline: String, stage: String) = {
    true
  }
  def nextStage(pipeline: String, stage: String) = {
    "stage2"
  }
}