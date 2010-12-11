package org.megrez.server.rest.osgi

import org.osgi.service.http.HttpService
import org.osgi.util.tracker.{ServiceTrackerCustomizer, ServiceTracker}
import org.osgi.framework.{ServiceReference, BundleContext, BundleActivator}
import com.sun.jersey.spi.container.servlet.ServletContainer
import java.util.Hashtable
import com.sun.jersey.api.core.PackagesResourceConfig
import org.megrez.server.rest.providers.CoreServicesProvider
import org.megrez.server.core.PipelineManager
import collection.mutable.HashSet

class Activator extends BundleActivator {
  private val RESOURCES_PACKAGE = "org.megrez.server.rest.resources"
  private val PROVIDERS_PACKAGE = "org.megrez.server.rest.providers"
  private val PATH = "/megrez"

  private var tracker: ServiceTracker = _
  private val references = HashSet[ServiceReference]()

  def start(context: BundleContext) {
    tracker = new ServiceTracker(context, classOf[HttpService].getName, new ServiceTrackerCustomizer {
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

    CoreServicesProvider.pipelineManager = getService[PipelineManager](context)
  }

  private def getService[T](context: BundleContext)(implicit m : Manifest[T]) = {
    val reference = context.getServiceReference(m.erasure.getName)
    references += reference
    context.getService(reference).asInstanceOf[T]
  }

  def stop(context: BundleContext) {
    tracker.close
    references.foreach(context.ungetService(_))
  }

  private def jerseyServlet = new ServletContainer(new PackagesResourceConfig(RESOURCES_PACKAGE, PROVIDERS_PACKAGE))


  private def jerseyParameters = new Hashtable[String, String]()
}