package org.megrez.server.http

import org.scalatest.matchers.ShouldMatchers

import Route._
import org.scalatest.{BeforeAndAfterEach, Spec}
import org.scalatest.mock.MockitoSugar
import org.jboss.netty.channel.{MessageEvent, ChannelHandlerContext, ChannelPipeline, Channel}
import org.mockito._
import org.mockito.Mockito._
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpVersion, DefaultHttpRequest}
import scala.actors._
import scala.actors.Actor._

class RouteTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
	describe("routing"){
		it("should match path and GET method"){
			val route = get("/agent") -> this
			val getRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/agent")
			route isMatch(getRequest) should be === true 
		}
		
		it("should match path and PUT method"){
			val route = put("/agent") -> this
			val getRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT, "/agent")
			route isMatch(getRequest) should be === true 
		}
		
		it("should match path and POST method"){
			val route = post("/agent") -> this
			val getRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/agent")
			route isMatch(getRequest) should be === true 
		}
		
		it("should match path and DELETE method"){
			val route = delete("/agent") -> this
			val getRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, "/agent")
			route isMatch(getRequest) should be === true 
		}

		it("should knows which method is matches"){
			val route = get("/agent") -> this
			val getRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/agent")
			route.matchedMethod(getRequest) should be === GET
		}
	}
}