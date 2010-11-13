package org.megrez.task

class AntTask(val target: String, val buildfile: String) extends CommandLineTask("") {
  override def commandLine() = {
    var cmd = "ant"
    if (buildfile != null) cmd += " -f " + buildfile
    if (target != null) cmd += " " + target
    cmd
  }
  override def toString() = "AntTask[target=" + target + ", buildfile=" + buildfile + "]"

  override def equals(other: Any) = other match {
      case that: AntTask => this.target == that.target && this.buildfile == that.buildfile
      case _ => false
    }

}