package org.megrez.server.trigger

import java.lang.String
import java.io.File
import collection.mutable._
import java.util.{Date, Calendar}

trait GitChecker {
  val repositoryUrl: String
  private val repositoryRoot: String = "/home/nana/workspace" //need specific it
  private val MonthConverter = HashMap("Jan" -> 1, "Feb" -> 2, "Mar" -> 3, "Apr" -> 4, "May" -> 5, "Jun" -> 6, "Jul" -> 7, "Aug" -> 8, "Sep" -> 9, "Oct" -> 10, "Nov" -> 11, "Dec" -> 12)
  var commitVersion: String = ""
  var currentDate: Calendar = Calendar.getInstance
  currentDate.setTime(new Date(2009))
  var needTriggerGit: Boolean = false

  def getLatestVersionForGit() = {
    val localRepository: String = repositoryRoot + repositoryUrl.substring(repositoryUrl.lastIndexOf("/")).split(".git")(0)
    val file: File = new File(localRepository)

    val runtime: Runtime = Runtime.getRuntime()
    runtime.exec("git pull",null, file)
    val process: Process = runtime.exec("git show", null, file)
    val answers: Iterator[String] = scala.io.Source.fromInputStream(process.getInputStream).getLines()

    var latestCommit: String = ""
    val latestDate: Calendar = Calendar.getInstance()

    answers.foreach {
      item =>
        if (item.contains("commit"))
          {
            latestCommit = item.substring(7).trim
          }
        else if (item.contains("Date:")) {
          val dateInfo: Array[String] = item.split("Date:")(1).trim.split(" ")
          val hourInfo: Array[String] = dateInfo(3).trim.split(":")
          latestDate.set(dateInfo(4).toInt, MonthConverter(dateInfo(1)), dateInfo(2).toInt, hourInfo(0).toInt, hourInfo(1).toInt, hourInfo(2).toInt)
        }
    }

    if (latestDate.after(currentDate) && commitVersion != latestCommit) {
      currentDate = latestDate
      commitVersion = latestCommit
      needTriggerGit = true
    }
    process.waitFor()
  }
}