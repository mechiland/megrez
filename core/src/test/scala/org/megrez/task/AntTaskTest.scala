package org.megrez.task

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Spec

class AntTaskTest extends Spec with ShouldMatchers {
  describe("Ant task") {
    it("should build commands containing buildfile"){
      new AntTask(null, "build.xml").commandLine should be === "ant -f build.xml"
    }
    it("should build commands containing target"){
      new AntTask("test", null).commandLine should be === "ant test"
    }
    it("should build commands containing buildfile and target"){
      new AntTask("test", "build.xml").commandLine should be === "ant -f build.xml test"
    }
  }
}