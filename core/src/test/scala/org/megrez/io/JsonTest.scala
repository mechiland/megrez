package org.megrez.io

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.vcs.Subversion
import org.megrez._
import java.util.UUID

class JsonTest extends Spec with ShouldMatchers {
  val JsonParser = scala.util.parsing.json.JSON

  describe("Domain object json serialization") {
    it("should parse material from json") {
      val json = """{"type" : "svn", "url" : "svn_url"}"""
      val material = JSON.read[Material](JsonParser.parseFull(json).get)
      material match {
        case subversion : Subversion =>
          subversion.url should equal("svn_url")
        case _ => fail
      }
    }

    it("should parse pipeline from json") {
      val json = """{"name" : "pipeline", "materials" : [{"type" : "svn", "url" : "svn_url"}] }"""
      val pipeline = JSON.read[Pipeline](JsonParser.parseFull(json).get)
      pipeline.isInstanceOf[Pipeline] should equal(true)
    }
  }

  describe("Message json serialization") {
    it("should parse job assignment from json") {
      val build = UUID.randomUUID
      val json = """{"build" : """ + '"' + build.toString + '"' + """, "materials" : [{ "material" : {"type" : "svn", "url" : "svn_url"}, "workset" : {"revision" : "1"} }] }"""      
      val assignment = JSON.read[JobAssignment](JsonParser.parseFull(json).get)
      assignment.build should equal(build)
      assignment.materials should have size(1)
      val (material, workSet) = assignment.materials.head
      material match {
        case subversion : Subversion =>
          subversion.url should equal("svn_url")
        case _ => fail
      }
      workSet match {
        case Some(revision : Int) =>
          revision should equal(1)
        case _ => fail
      }

    }
  }
}