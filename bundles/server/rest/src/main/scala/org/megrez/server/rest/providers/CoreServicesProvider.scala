package org.megrez.server.rest.providers

import com.sun.jersey.spi.inject.{InjectableProvider, Injectable}
import javax.ws.rs.core.Context
import java.lang.reflect.Type
import com.sun.jersey.core.spi.component.{ComponentScope, ComponentContext}
import org.megrez.server.core.PipelineManager
import javax.ws.rs.ext.Provider

@Provider
class CoreServicesProvider extends InjectableProvider[Context, Type] {
  private val injectables = Map[Type, Injectable[_]](classOf[PipelineManager] -> new Injectable[PipelineManager] {
    def getValue = CoreServicesProvider.pipelineManager
  })

  def getInjectable(context: ComponentContext, annotation: Context, p: Type) = injectables.getOrElse(p, null)

  def getScope = ComponentScope.PerRequest
}

object CoreServicesProvider {
  var pipelineManager: PipelineManager = _
}