package org.megrez.server

import actors._
import scala.actors.Actor._
import org.scalatest._
import org.scalatest.matchers._

class AgentTest extends Spec with ShouldMatchers{
  describe("receiving job") {
	it("should start job if idle") {
	  val agent = new Agent()
	  agent start
	  
	  val scheduler = actor {
	    receiveWithin(1000) {
	      case message : AgentStateChange => 
	        message.state should equal("BUSY")
	    }
	  }	    
	
	  agent !? new Job(scheduler)	  
	}
	
	it("should ignore job if busy") {
	  val agent = new Agent()
	  agent start
	  
	  val scheduler = actor {
	    receiveWithin(1000) {
	      case message : AgentStateChange => 
	        message.state should equal("BUSY")
	      case message : AgentBusy => 
	    }
	  }	    
	
	  agent !? new Job(scheduler)
	  agent !? new Job(scheduler)
	}
  }
}