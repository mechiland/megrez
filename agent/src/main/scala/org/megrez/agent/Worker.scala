package org.megrez.agent

import actors.Actor

class Worker extends Actor {
  def act() {
    loop {
      react {
        case _ => 
      }
    }
  }
}