package org.megrez.server.trigger

import java.lang.String
import java.io.File
import collection.mutable._
import java.util.Calendar
import main.scala.org.megrez.server.trigger.VersionControl
import org.megrez.server.{PipelineConfig, TriggerMessage, Pipeline}

class Git(val pipeline: PipelineConfig) extends VersionControl {
  private val MonthConverter = HashMap("Jan" -> 1, "Feb" -> 2, "Mar" -> 3, "Apr" -> 4, "May" -> 5, "Jun" -> 6, "Jul" -> 7, "Aug" -> 8, "Sep" -> 9, "Oct" -> 10, "Nov" -> 11, "Dec" -> 12)

  private val repositoryUrl: String = pipeline.material.url
  var commitVersion: String = null
  var currentDate: Calendar = null


  def checkWorkDirectory(file: File, localRepository: String): Boolean = {
    if (!file.exists || !new File(localRepository + "/.git").exists) {
      val runtime: Runtime = Runtime.getRuntime()
      val process: Process = runtime.exec("git clone" + repositoryUrl, null, file)
      println("Check out code to Local repository")
      process.waitFor()
      checkWorkDirectory(file, localRepository)
    } else
      {
        println("Local repository exist")
        return true
      }

  }

  def getChange(): TriggerMessage = {
    if (needTriggerScheduler == true)
      return new TriggerMessage(pipeline.name, commitVersion)
    return null
  }

  def checkChange() = {
    val localRepository: String = pipeline.workingDir() + repositoryUrl.substring(repositoryUrl.lastIndexOf("/")).split(".git")(0)
    val file: File = new File(localRepository)

    checkWorkDirectory(file, localRepository)

    val runtime: Runtime = Runtime.getRuntime()
    runtime.exec("git pull", null, file)
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

    if (commitVersion != latestCommit) {
      currentDate = latestDate
      commitVersion = latestCommit
      needTriggerScheduler = true
    }
    process.waitFor()
  }
}