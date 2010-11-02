package org.megrez.runtime

import io.Source

trait ShellCommand {
  def check(command: String) = {
    val process = Runtime.getRuntime().exec(command)
    process.waitFor match {
      case 0 => true
      case _ => false
    }
  }

  def run(command : String) = {
    val process = Runtime.getRuntime().exec(command)
    process.waitFor match {
      case 0 => process
      case exitCode: Int => throw new ShellException("exit code " + exitCode + "\n" + Source.fromInputStream(process.getErrorStream).mkString)
    }    
  }
}

class ShellException(message : String) extends Exception(message)