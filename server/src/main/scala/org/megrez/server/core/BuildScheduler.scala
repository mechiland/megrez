package org.megrez.server.core

import scala.actors.Actor
import org.megrez.server.model.{Build, Change, Pipeline}
import org.megrez.Stop

class BuildScheduler(val dispatcher: Actor) extends Actor {
  def act() {
    loop {
      react {
        case TriggerToScheduler.TriggerBuild(pipeline, changes) =>
          schedule(Build(pipeline, changes))
        case DispatcherToScheduler.JobFinished(build, operation) =>
          operation()
          schedule(build)
        case Stop => exit
        case _ =>
      }
    }
  }

  private def schedule(build: Build) {
    val assignment = build.next.map((build, _))
    if (!assignment.isEmpty) dispatcher ! assignment
  }

  start
}
