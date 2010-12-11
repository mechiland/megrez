package org.megrez.server.rest

import org.osgi.service.http.HttpService
import org.osgi.util.tracker.{ServiceTrackerCustomizer, ServiceTracker}
import org.osgi.framework.{ServiceReference, BundleContext, BundleActivator}
import com.sun.jersey.spi.container.servlet.ServletContainer
import java.util.Hashtable
import com.sun.jersey.api.core.PackagesResourceConfig
import java.lang.String
import resources.PipelineResource

class Activator extends BundleActivator {
  private val RESOURCES_PACKAGE = "org.megrez.server.rest.resources"
  private val PATH = "/megrez"

  private var tracker: ServiceTracker = _

  def start(context: BundleContext) {
    tracker = new ServiceTracker(context, classOf[HttpService].getName, new ServiceTrackerCustomizer() {
      def addingService(reference: ServiceReference) = {
        val httpService = context.getService(reference).asInstanceOf[HttpService]
        httpService.registerServlet(PATH, jerseyServlet, jerseyParameters, null)
        httpService
      }

      def modifiedService(reference: ServiceReference, service: AnyRef) {}

      def removedService(reference: ServiceReference, service: AnyRef) {
        service.asInstanceOf[HttpService].unregister(PATH)
      }
    })
    tracker.open
  }

  def stop(context: BundleContext) {
    tracker.close
  }

//  private def jerseyServlet = new ServletContainer(new javax.ws.rs.core.Application() {
//
//    import scala.collection.JavaConversions._
//
//    override def getClasses = asSet(Set[Class[_]](classOf[PipelineResource]))
//  })

  private def jerseyServlet = new ServletContainer(new PackagesResourceConfig(RESOURCES_PACKAGE))


  private def jerseyParameters = new Hashtable[String, String]()
}