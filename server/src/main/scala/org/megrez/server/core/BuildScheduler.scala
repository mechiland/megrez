package org.megrez.server.core

import scala.actors.Actor
import org.megrez.server.model.{Build, Change, Pipeline}

class BuildScheduler(val dispatcher: Actor) extends Actor {
  def act() {
    loop {
      react {
        case TriggerBuild(pipeline, changes) =>
          schedule(Build(pipeline, changes))
        case JobFinished(build, operation) =>
          operation()
          schedule(build)
        case "STOP" => exit
      }
    }
  }

  private def schedule(build: Build) {
    val assignment = build.next.map((build, _))
    if (!assignment.isEmpty) dispatcher ! assignment
  }

  start
}

case class TriggerBuild(val pipeline: Pipeline, val changes: Set[Change])
case class JobFinished(val build: Build, val operation: () => Unit)