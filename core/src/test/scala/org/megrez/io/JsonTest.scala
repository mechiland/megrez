package org.megrez.io

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.vcs.Subversion
import org.megrez._
import task.CommandLineTask

class JsonTest extends Spec with ShouldMatchers {
  val JsonParser = scala.util.parsing.json.JSON

  describe("Domain object json serialization") {
    it("should parse material from json") {
      val json = """{"type" : "svn", "url" : "svn_url", "dest" : "dest"}"""
      val material = JSON.read[Material](JsonParser.parseFull(json).get)
      material.destination should equal("dest")
      material.source match {
        case subversion : Subversion =>
          subversion.url should equal("svn_url")
        case _ => fail
      }
    }

    it("should parse job from json") {
      val json = """{"name" : "unit test", "tasks" : [{ "type" : "cmd", "command": "ls"}] }"""
      val job = JSON.read[Job](JsonParser.parseFull(json).get)
      job.name should equal("unit test")
      job.tasks should have size(1)
      job.tasks.head match {
        case cmd : CommandLineTask =>
          cmd.command should equal("ls")
        case _ => fail
      }
    }

    it("should parse pipeline from json") {
      val json = """{"name" : "pipeline", "materials" : [{"type" : "svn", "url" : "svn_url", "dest" : "dest"}] }"""
      val pipeline = JSON.read[Pipeline](JsonParser.parseFull(json).get)
      pipeline.isInstanceOf[Pipeline] should equal(true)
    }
  }

  describe("Message json serialization") {
    it("should parse job assignment from json") {
      val json = """{"pipeline" : "pipeline", "materials" : [{ "material" : {"type" : "svn", "url" : "svn_url", "dest" : "dest"}, "workset" : {"revision" : "1"} }], "job" : {"name" : "unit test", "tasks" : [{ "type" : "cmd", "command": "ls"}] } }"""      
      val assignment = JSON.read[JobAssignment](JsonParser.parseFull(json).get)
      assignment.pipeline should equal("pipeline")
      assignment.materials should have size(1)
      val (material, workSet) = assignment.materials.head
      material.source match {
        case subversion : Subversion =>
          subversion.url should equal("svn_url")
        case _ => fail
      }
      workSet match {
        case Some(revision : Int) =>
          revision should equal(1)
        case _ => fail
      }
      assignment.job.name should equal("unit test")
      assignment.job.tasks should have size(1)
      assignment.job.tasks.head match {
        case cmd : CommandLineTask =>
          cmd.command should equal("ls")
        case _ => fail
      }
    }
  }
}