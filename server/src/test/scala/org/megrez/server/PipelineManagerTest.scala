package org.megrez.server

import org.scalatest.mock.MockitoSugar
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import org.mockito.Matchers._
import actors.{TIMEOUT, Actor}
import actors.Actor._
import trigger.Trigger
import org.megrez.Pipeline
import scala.collection.JavaConversions._
import org.neo4j.graphdb._
import org.neo4j.graphdb.Traverser.Order
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Spec}

class PipelineManagerTest extends Spec with ShouldMatchers with MockitoSugar with BeforeAndAfterEach with BeforeAndAfterAll with Neo4jHelper {
  describe("Pipeline manager") {
    it("should launch trigger when new pipeline added") {
      object Context {
        val triggerFactory = mock[Pipeline => Trigger]
      }

      when(Context.triggerFactory.apply(any(classOf[Pipeline]))).thenReturn(new ActorBasedTrigger("pipeline", self))

      val manager = new PipelineManager(Context)
      manager ! ToPipelineManager.AddPipeline(new Pipeline("pipeline", null, List[Pipeline.Stage]()))

      receiveWithin(1000) {
        case "TRIGGER START pipeline" =>
        case TIMEOUT =>
        case _ => fail
      }
    }

    it("should save pipeline to database when new pipeline added") {
      object Context {
        val triggerFactory = mock[Pipeline => Trigger]
      }

      when(Context.triggerFactory.apply(any(classOf[Pipeline]))).thenReturn(new ActorBasedTrigger("pipeline", self))

      val manager = new PipelineManager(Context)
      manager ! ToPipelineManager.AddPipeline(new Pipeline("pipeline", null, List[Pipeline.Stage]()))

      receiveWithin(1000) {
        case "TRIGGER START pipeline" =>
        case TIMEOUT =>
        case _ => fail
      }

      val nodes = allPipelineNodes
      nodes.size should be === 1
      nodes.iterator.next.getProperty("name") should be === "pipeline"

    }

    it("should shutdown trigger and start new one when pipeline config changed") {
      object Context {
        val triggerFactory = mock[Pipeline => Trigger]
      }

      when(Context.triggerFactory.apply(any(classOf[Pipeline]))).thenReturn(new ActorBasedTrigger("pipeline1", self),
        new ActorBasedTrigger("pipeline2", self))

      val manager = new PipelineManager(Context)
      manager ! ToPipelineManager.AddPipeline(new Pipeline("pipeline", null, List[Pipeline.Stage]()))

      receiveWithin(1000) {
        case "TRIGGER START pipeline1" =>
        case TIMEOUT =>
        case _ => fail
      }

      manager ! ToPipelineManager.PipelineChanged(new Pipeline("pipeline", null, List[Pipeline.Stage]()))

      receiveWithin(1000) {
        case "TRIGGER STOP pipeline1" =>
        case TIMEOUT =>
        case _ => fail
      }

      receiveWithin(1000) {
        case "TRIGGER START pipeline2" =>
        case TIMEOUT =>
        case _ => fail
      }
    }
  }


  override def beforeEach(){
    cleanData
  }

  override def afterEach() {
    cleanData
  }

  override def afterAll() {
    cleanupDatabase
  }

  class ActorBasedTrigger(val name: String, val actor: Actor) extends Trigger {
    val pipeline: Pipeline = null
    val target: Actor = null

    def start {
      actor ! ("TRIGGER START " + name)
    }

    def stop {
      actor ! ("TRIGGER STOP " + name)
    }
  }
}