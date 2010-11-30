package org.megrez.server

import java.io.File
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.EmbeddedGraphDatabase
import io.Source
import java.util.UUID

trait IoSupport {
  def delete(file: File) {
    file.listFiles.foreach {
      file =>
        if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }
}

trait Neo4JSupport {
  self: IoSupport =>
  private val database = new File(System.getProperty("user.dir"), "database_" + UUID.randomUUID.toString)

  protected var neo: GraphDatabaseService = null

  object Neo4J {
    def start() {
      database.mkdirs
      neo = new EmbeddedGraphDatabase(database.getAbsolutePath)
    }

    def shutdown() {
      neo.shutdown
      delete(database)
    }
  }
}

trait SvnSupport {
  self: IoSupport =>

  private val subversion: File = new File(System.getProperty("user.dir"), "subversion_" + UUID.randomUUID.toString)

  object Svn {
    def create(name: String) = {
      val dir = new File(subversion, "repository_" + System.currentTimeMillis)
      dir.mkdirs
      run("svnadmin create " + name, dir)
      "file://" + new File(dir, name).getAbsolutePath
    }

    def checkin(repository: File, file: String) {
      val revision = new File(repository, file)
      revision.createNewFile

      run("svn add " + revision.getAbsolutePath)
      run("svn ci . -m \"checkin\"", repository)
    }

    def checkout(url: String) = {
      val target = new File(subversion, "checkout_" + System.currentTimeMillis)
      run("svn co " + url + " " + target)
      target
    }

    def clean() = delete(subversion)
  }

  private def run(command: String) {
    run(command, subversion)
  }

  private def run(command: String, workingDir: File) {
    val cmd = Runtime.getRuntime().exec(command, null, workingDir)
    cmd.waitFor match {
      case 0 =>
      case _ => throw new Exception(Source.fromInputStream(cmd.getErrorStream).mkString)
    }
  }

}