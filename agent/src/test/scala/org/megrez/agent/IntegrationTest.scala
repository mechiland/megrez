package org.megrez.agent

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{Spec}
class IntegrationTest extends ServerIntegration with ShouldMatchers {
  describe("Version control intergration") {
    it("should handle subversion job assignment") {
      server.response(WebSocketHandshake, MegrezHandshake)
      server.start
      connect
      expect("CONNECTED", 1000)
    }
  }  
}