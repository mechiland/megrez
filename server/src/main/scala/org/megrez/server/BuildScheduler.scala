package org.megrez.server

import actors.Actor
import collection.mutable.HashMap
import java.util.UUID

class BuildScheduler(val dispatcher: Actor) extends Actor {
  private val builds = HashMap[UUID, Build]()

  def act {
    loop {
      react {
        case TriggerBuild(config: PipelineConfig) =>
          val id = UUID.randomUUID
          val build = new Build(config)          
          builds.put(id, build)
          build.next match {
            case Some(jobs: Set[Job]) =>
              dispatcher ! JobScheduled(id, jobs)
            case None =>
          }
        case _ =>
      }
    }
  }

  start
}