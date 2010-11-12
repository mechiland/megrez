package org.megrez.util

import io.Source
import java.io.File

trait ShellCommand {
  def check(command: String, workingDir:File = null) = {
    val process = Runtime.getRuntime().exec(command, null, workingDir)
    process.waitFor match {
      case 0 => true
      case _ => false
    }
  }


  def run(command: String, workingDir: File = null):Process = {
    val process = Runtime.getRuntime().exec(command, null, workingDir)
    process.waitFor match {
      case 0 => process
      case exitCode: Int => throw new ShellException("exit code " + exitCode + "\n" + Source.fromInputStream(process.getErrorStream).mkString)
    }
  }

}

class ShellException(message: String) extends Exception(message)

