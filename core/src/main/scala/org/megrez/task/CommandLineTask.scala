package org.megrez.task

import actors.Actor._
import java.io.{InputStream, InputStreamReader, BufferedReader, File}
import org.megrez.Task
import actors.{TIMEOUT, Actor}

class CommandLineTask(val command: String) extends Task {
  private var process: Process = _
  private val stdout = new StringBuffer

  private var onConsoleOutput : String => Unit = (_) => {}

  def execute(workingDir: File):String = {
    process = Runtime.getRuntime.exec(command)
    if (stdout.length != 0) stdout.delete(0, stdout.length)
    pipe(process.getInputStream, stdout, self, onConsoleOutput)
    
    process.waitFor
    
    receiveWithin(5000) {
      case "DONE" =>
      case TIMEOUT =>
    }
    return stdout.toString
  }

  def onConsoleOutputChanges(listener : String => Unit) {
    onConsoleOutput = listener
  }

  private def pipe(in : InputStream, result : StringBuffer, executor : Actor, listener : String => Unit) {
    actor {
      val reader = new BufferedReader(new InputStreamReader(in))
      var line = reader.readLine
      while(line != null) {
        listener(line)
        result.append(line)
        line = reader.readLine
      }
      executor ! "DONE"
    }
  }

  protected def commandLine() = command
  
  def cancel() {
    process.destroy
  }
}