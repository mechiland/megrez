package org.megrez.server.trigger

trait SvnChecker {
  val repositoryUrl: String

  def getLatestVersionForSvn(): Int = {
    val process: Process = Runtime.getRuntime().exec("svn info " + repositoryUrl)
    val answers: Iterator[String] = scala.io.Source.fromInputStream(process.getInputStream).getLines()
    answers.foreach {
      item =>
        if (item.contains("Revision"))
          return (Integer.parseInt(item.split(":")(1).trim))
    }
    process.waitFor()
  }
}