package org.megrez.server.json

import org.megrez.util.JSON.JsonSerializer
import org.megrez.server.Build.JobStage
import org.megrez.Pipeline
import org.megrez.server.Build

object JSON {
  private val JsonParser = org.megrez.util.JSON

  def write[T](resource: T)(implicit jsObject: JsonSerializer[T]): String = JsonParser.write(resource)

  import org.megrez.util.JSON._

  implicit object JobStageSerializer extends JsonSerializer[JobStage] {
    def read(json: Map[String, Any]) = null

    def write(stageInstance: JobStage) = Map("name" -> stageInstance.stage.name,
      "status" -> stageInstance.status.toString.toLowerCase,
      "jobs" -> stageInstance.jobs.map(job => Map("name" -> job.name, "status" -> stageInstance.jobStatus(job).toString.toLowerCase)).toList)
  }

  implicit object BuildSerializer extends JsonSerializer[Build] {
    def read(json: Map[String, Any]) = null

    def write(build: Build) = Map("name" -> build.pipeline.name, "stages" -> build.pipeline.stages.map(writeStage(build, _)))

    private def writeStage(build: Build, stage: Pipeline.Stage): Map[String, Any] = {
      implicit def pipeline = build.pipeline;

      if (build.current.status == Build.Stage.Status.Completed) {
        completedStage(stage)
      }
      else {
        val currentStage = build.current.asInstanceOf[JobStage]
        if (stage == currentStage.stage) {
          JobStageSerializer.write(currentStage)
        }
        else if (stage.before(currentStage.stage)) {
          completedStage(stage)
        }
        else {
          Map("name" -> stage.name, "status" -> "unknown", "jobs" -> stage.jobs.map(job => Map("name" -> job.name, "status" -> "unknown")).toList)
        }
      }
    }

    private def completedStage(stage: Pipeline.Stage): Map[String, Any] = {
      Map("name" -> stage.name, "status" -> "completed", "jobs" -> stage.jobs.map(job => Map("name" -> job.name, "status" -> "completed")).toList)
    }
  }
}