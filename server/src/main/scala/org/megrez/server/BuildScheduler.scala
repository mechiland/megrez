package org.megrez.server

import actors.Actor

class BuildScheduler() extends Actor {
  
  def act {
    loop {
      react {
        case _ =>
      }
    }
  }

  start
}