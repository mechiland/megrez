package org.megrez.server.http

import org.scalatest.matchers.ShouldMatchers

import org.scalatest.{BeforeAndAfterEach, Spec}
import org.scalatest.mock.MockitoSugar
import org.jboss.netty.channel.{MessageEvent, ChannelHandlerContext, ChannelPipeline, Channel}
import org.mockito.Mockito._
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpVersion, HttpRequest, DefaultHttpRequest}
import scala.actors._
import scala.actors.Actor._
import Method._
import Route._

class RequestTest extends Spec with ShouldMatchers with BeforeAndAfterEach  {
	describe("httprequest should be converted to request"){
		// implicit def httpToRequest(request: HttpRequest) = new Request2(request)
		// val req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/agent")
		// req.method should be === GET
		// req.path should be === "/agent"
	}
}