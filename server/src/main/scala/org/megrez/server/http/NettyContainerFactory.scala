package org.megrez.server.http

import org.jboss.netty.channel.ChannelUpstreamHandler
import com.sun.jersey.spi.container.{WebApplication, ContainerProvider}
import com.sun.jersey.api.core.ResourceConfig
import java.lang.Class

class NettyContainerFactory extends ContainerProvider[ChannelUpstreamHandler] {
  def createContainer(containerType: Class[ChannelUpstreamHandler], config: ResourceConfig, application: WebApplication) =
    if (containerType == classOf[ChannelUpstreamHandler]) new NettyContainer(application) else null
}