package org.megrez.util

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec
import org.megrez.Material
import org.megrez.vcs.Subversion

class JsonTest extends Spec with ShouldMatchers {
  describe("Domain object serialization") {
    it("should serialize material") {
      val material = new Material(new Subversion("svn_url"), "dest")
      JSON.write(material) should equal("""{"type":"svn","url":"svn_url","dest":"dest"}""")
    }

    it("should deserialize material") {
      val json = """{"type" : "svn", "url" : "svn_url", "dest" : "dest"}"""
      val material = JSON.read[Material](json)
      material.source.isInstanceOf[Subversion] should equal(true)
      material.source.asInstanceOf[Subversion].url should equal("svn_url")
      material.destination should equal("dest")
    }

    
  }
}
