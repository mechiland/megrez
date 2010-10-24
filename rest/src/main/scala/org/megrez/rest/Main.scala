package org.megrez.rest


import org.restlet.data.Protocol
import org.restlet.ext.netty.HttpServerHelper
import org.restlet.{Server, Component}
object Main {
  def main(args: Array[String]) {
    var component = new Component();
    var server = new Server(Protocol.HTTP, 8080);
    new HttpServerHelper(server);
    component.getServers().add(server);

    component.getDefaultHost().attach(new ManagerApplication());

    component.start();
  }
}