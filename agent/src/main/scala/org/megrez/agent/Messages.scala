package org.megrez.agent

import util.parsing.json.JSON
import vcs.VersionControl

case class JobAssignment(val pipelineId: String, val versionControl: VersionControl, val workSet: Any, val job: Job)
case class JobCompleted(val pipelineId: String, val workSet: Any)

object JobAssignment {
  def parse(json: String) = {
    JSON.parseFull(json) match {
      case Some(json: Map[String, Any]) =>
        val pipeline = json("pipeline") match {
          case pipeline: Map[String, Any] => pipeline
          case _ => null
        }
        val vcs = pipeline("vcs") match {
          case vcs: Map[String, Any] => vcs
          case _ => null
        }
        val job = json("job") match {
          case job: Map[String, Any] => job
          case _ => null
        }

        val tasks = job("tasks") match {
          case tasks: List[Any] => List[Task]()
          case _ => null
        }

        val workSet = json("workSet") match {
          case workSet: Map[String, Any] => workSet
          case _ => null
        }

        val versionControl = VersionControl.find(vcs("type").asInstanceOf[String])

        new JobAssignment(pipeline("id").asInstanceOf[String], versionControl.parse(vcs), versionControl.parseWorkSet(workSet), Job(tasks))
      case None => null
    }
  }
}
