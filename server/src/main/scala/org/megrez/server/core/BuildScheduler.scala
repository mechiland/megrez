package org.megrez.server.core

import scala.actors.Actor
import org.megrez.server.model.{Build, Change, Pipeline}

class BuildScheduler(val dispatcher: Actor) extends Actor {
  def act() {
    loop {
      react {
        case TriggerBuild(pipeline, changes) =>
          val build = Build(pipeline, changes)
          dispatcher ! build.next.map((build, _))
        case JobFinishedOnBuild(build) =>
          dispatcher ! build.next.map((build, _))
        case "STOP" => exit
      }
    }
  }
  start
}

case class TriggerBuild(val pipeline: Pipeline, val changes: Set[Change])
case class JobFinishedOnBuild(val build: Build)