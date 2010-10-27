package org.megrez.server.http

import Method._
import util.parsing.json.{JSONObject, JSON}
import org.megrez.server._

case class Request(val method : Method, val uri: String, val content: String){
	def resource = {
		JSON.parseFull(content) match {
	      case Some(content: Map[String, Any]) =>
	        val pipeline = content("pipeline") match {
	          case pipeline: Map[String, Any] => pipeline
	          case _ => null
	        }
	        val vcs = pipeline("vcs") match {
	          case vcs: Map[String, Any] => vcs
	          case _ => null
	        }

			val _type = vcs("type").asInstanceOf[String]
	        val material = Material.find(_type)
	        new PipelineConfig(pipeline("name").asInstanceOf[String], material.parse(vcs))
	      case None => null
		}
	}
}
case class WebSocket()