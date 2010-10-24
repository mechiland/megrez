package org.megrez.rest


import org.restlet.resource.{Post, Get, ServerResource}

class PipelineResource extends ServerResource {

  @Get
  def show() : String = {
    return "I'm a pipeline";
  }

  @Post
  def create() : String ={
     return "Create a pipeline";
  }

}