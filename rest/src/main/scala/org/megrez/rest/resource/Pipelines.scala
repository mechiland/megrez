package org.megrez.rest.resource

import org.restlet.resource.{Post, ServerResource}

class Pipelines extends ServerResource {

  @Post
  def create() : String ={
     return "Create a pipeline";
  }

}