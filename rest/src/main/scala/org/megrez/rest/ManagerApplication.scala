package org.megrez.rest

import org.restlet.routing.Router
import org.restlet.{Restlet, Application}
import org.megrez.rest.resource._

class ManagerApplication extends Application {
  override def createInboundRoot(): Restlet = {
    var router = new Router(getContext());
    router.attach("/pipelines", classOf[Pipelines]);
    router.attach("/pipelines/{pipeline}", classOf[Pipeline]);
    return router;
  }
}