package org.megrez.server.core.osgi

import org.megrez.server.core.PipelineManager
import java.util.Hashtable
import org.megrez.server.core.actors._
import org.osgi.framework.{ServiceRegistration, BundleContext, BundleActivator}

class Activator extends BundleActivator {
  private var pipelineManager: ServiceRegistration = _

  def start(context: BundleContext) {
    pipelineManager = context.registerService(classOf[PipelineManager].getName, new PipelineManagerActor, new Hashtable[String, String])
  }

  def stop(context: BundleContext) {
    pipelineManager.unregister
  }
}