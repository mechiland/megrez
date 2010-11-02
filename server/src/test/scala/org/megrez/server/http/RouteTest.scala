package org.megrez.server.http

import org.scalatest.matchers.ShouldMatchers

import Route._
import org.scalatest.{BeforeAndAfterEach, Spec}
import org.scalatest.mock.MockitoSugar
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpVersion, DefaultHttpRequest}
import scala.actors.Actor._

import Method._

class RouteTest extends Spec with ShouldMatchers with BeforeAndAfterEach with MockitoSugar {
	describe("routing"){
		it("should match path"){
			val route = get("/agent") -> self
			val getRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/agent")
			route matches(getRequest) should be === true
		}
		
		it("should match path and PUT method"){
			val route = put("/pipelines") -> self
			val getRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT, "/pipelines?name=pipeline1&vcs=svn&url=url")
			route matches(getRequest) should be === true
		}
		
		it("should match path and POST method"){
			val route = post("/pipelines") -> self
			val getRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/pipelines")
			route matches(getRequest) should be === true
		}
		
		it("should match path and DELETE method"){
			val route = delete("/agent") -> self
			val getRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.DELETE, "/agent")
			route matches(getRequest) should be === true
		}

		it("should knows which method is matches"){
			val route = get("/agent") -> self
			val getRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/agent")
			route.matchedMethod(getRequest) should be === GET
		}
	}
}