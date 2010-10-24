package org.megrez.rest


import org.restlet.data.Protocol
import org.restlet.{Component}

object Main {
  def main(args: Array[String]) {
    var component = new Component();
    component.getServers().add(Protocol.HTTP, 8080);

    component.getDefaultHost().attach(new ManagerApplication());

    component.start();
  }
}