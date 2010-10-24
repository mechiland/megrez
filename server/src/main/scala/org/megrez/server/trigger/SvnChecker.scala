package org.megrez.server.trigger

trait SvnChecker {
  val repositoryUrl: String
  var revision:Int = 0
  var needTriggerSVN:Boolean=false

  def getLatestVersionForSvn(): Int = {
    val process: Process = Runtime.getRuntime().exec("svn info " + repositoryUrl)
    val answers: Iterator[String] = scala.io.Source.fromInputStream(process.getInputStream).getLines()
    answers.foreach {
      item =>
        if (item.contains("Revision"))
          {val latestVersion:Int = Integer.parseInt(item.split(":")(1).trim)
           if(latestVersion>revision){
             revision = latestVersion
             needTriggerSVN = true
           }
          }
    }
    process.waitFor()
  }
}