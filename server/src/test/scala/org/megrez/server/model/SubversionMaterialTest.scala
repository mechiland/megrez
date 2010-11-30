package org.megrez.server.model

import data.Graph
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterAll, Spec}
import tasks.{CommandLine, Ant}
import vcs.{Subversion, Git}
import java.io.File
import org.megrez.server.{SvnSupport, IoSupport, Neo4JSupport}

class SubversionMaterialTest extends Spec with ShouldMatchers with BeforeAndAfterAll with IoSupport with Neo4JSupport with SvnSupport {
  describe("Material") {
    it("should found changes when material doesn't have any previous change") {
      val material = Material(Map("source" -> subversion))
      material.getChange(workingdir) match {
        case Some(r: Subversion.Revision) =>
          r.material should equal(material)
        case _ => fail
      }
    }

    it("should not found changes when material has previous change and svn not changed") {
      val material = Material(Map("source" -> subversion))
      material.getChange(workingdir)
      material.getChange(workingdir) match {
        case None =>
        case _ => fail
      }
    }
  }

  val workingdir = new File(System.getProperty("user.dir"))
  var subversion: ChangeSource = null

  override def beforeAll() {
    Neo4J.start
    Graph.of(neo).consistOf(Task, Job, Stage, ChangeSource, Material, Pipeline, Build, Agent, Change, Subversion.Revision)
    Graph.consistOf(CommandLine, Ant, Subversion, Git)

    subversion = ChangeSource(Map("type" -> "svn", "url" -> Svn.create("test")))
  }

  override def afterAll() {
    Neo4J.shutdown
    Svn.clean
  }
}


