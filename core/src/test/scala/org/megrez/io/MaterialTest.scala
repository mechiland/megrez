package org.megrez.io

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.vcs.Subversion
import org.megrez._

class JsonTest extends Spec with ShouldMatchers {
  val JsonParser = scala.util.parsing.json.JSON

  describe("Domain object json serialization") {
    it("should parse material from json") {
      val json = """{"type" : "svn", "url" : "svn_url"}"""
      val material = JSON.read[Material](JsonParser.parseFull(json).get)
      material.isInstanceOf[Subversion] should equal(true)
      material.asInstanceOf[Subversion].url should equal("svn_url")
    }

    it("should parse pipeline from json") {
      val json = """{"name" : "pipeline", "materials" : [{"type" : "svn", "url" : "svn_url"}] }"""      
      val pipeline = JSON.read[Pipeline](JsonParser.parseFull(json).get)
      pipeline.isInstanceOf[Pipeline] should equal(true)
    }
  }
}