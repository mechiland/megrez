package org.megrez.server.util

import io.Source
import java.io.{File, IOException}

object Utils {
  def aRandomName = {
    String.valueOf(System.currentTimeMillis)
  }
}

object CommandUtil {
  def run(command: String): Iterator[String] = {
    run(command, new File(System.getProperty("user.dir")))
  }

  def run(command: String, workingDir: File) = {
    val process: Process = Runtime.getRuntime().exec(command, null, workingDir)
    val exitCode: Int = process.waitFor
    if (exitCode != 0) {
      val error = Source.fromInputStream(process.getErrorStream).mkString
      throw new IOException(String.format("Got error when running command: [%s]\n%s", command, error))
    }
    Source.fromInputStream(process.getInputStream).getLines()
  }
}

object EnvUtil {
  def megrezParentFolder(): String = {
    System.getProperty("user.dir").split("/megrez")(0)
  }

  def tempDir(): File = {
    new File(System.getProperty("user.dir"), "target")
  }

  def isWindows() = {
    System.getProperty("os.name").contains("Win")
  }
}