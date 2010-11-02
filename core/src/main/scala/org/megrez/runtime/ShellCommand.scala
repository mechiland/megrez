package org.megrez.runtime

import io.Source
import java.io.File

trait ShellCommand {
  def check(command: String) = {
    val process = Runtime.getRuntime().exec(command)
    process.waitFor match {
      case 0 => true
      case _ => false
    }
  }

  def run(command: String, file: File = null) = {
    var process: Process = null
    if (file == null) process = Runtime.getRuntime().exec(command)
    else process = Runtime.getRuntime().exec(command, null, file)
    process.waitFor match {
      case 0 => process
      case exitCode: Int => throw new ShellException("exit code " + exitCode + "\n" + Source.fromInputStream(process.getErrorStream).mkString)
    }
  }
}

class ShellException(message: String) extends Exception(message)