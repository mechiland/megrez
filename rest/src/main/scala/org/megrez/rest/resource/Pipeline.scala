package org.megrez.rest.resource

import org.restlet.resource.{Get, ServerResource}

class Pipeline extends ServerResource {
  var pipelineId: String = "";

  override def doInit() {
    this.pipelineId = getRequestAttributes().get("pipeline").asInstanceOf[String];
  }

  @Get
  def show(): String = {
    return "I'm the pipeline with id:" + pipelineId;
  }

}