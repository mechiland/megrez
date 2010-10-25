package org.megrez.agent

class Job(val tasks : List[Task]) 

abstract class Task {
  def run()
}