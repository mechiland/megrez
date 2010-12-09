package org.megrez.server.http.netty

import org.jboss.netty.handler.codec.http.{HttpRequestDecoder, HttpChunkAggregator, HttpResponseEncoder}
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import java.util.concurrent.Executors._
import javax.ws.rs.ext.RuntimeDelegate
import com.sun.jersey.api.core.PackagesResourceConfig
import org.jboss.netty.channel._
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class Server(val routes: Route*) {
  import scala.collection.JavaConversions._
  
  private def packageConfig = Map("com.sun.jersey.config.property.packages" -> packages)

  private def packages = routes.map(_ match {
    case ResourcePackage(packageName) => packageName
    case _ => ""
  }).filter(!_.isEmpty).mkString("", ",", ",org.megrez.server.http.netty")

  private val jerseyHandler = RuntimeDelegate.getInstance.createEndpoint(new PackagesResourceConfig(packageConfig), classOf[ChannelUpstreamHandler])

  private val bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(newCachedThreadPool(), newCachedThreadPool()))

  bootstrap.setPipelineFactory(new ChannelPipelineFactory {
    override def getPipeline() = {
      val pipeline = Channels.pipeline
      pipeline.addLast("decoder", new HttpRequestDecoder())
      pipeline.addLast("aggregator", new HttpChunkAggregator(65536))
      pipeline.addLast("encoder", new HttpResponseEncoder())
      pipeline.addLast("handler", Handler)
      pipeline
    }
  })

  object Handler extends SimpleChannelUpstreamHandler {
    override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
      jerseyHandler.handleUpstream(ctx, e)
    }
  }

  private var channel : Channel = _

  def start(port : Int) {
    channel = bootstrap.bind(new InetSocketAddress(port))
  }

  def shutdown {
    if (channel != null) channel.close.awaitUninterruptibly(30, TimeUnit.SECONDS)
    if (bootstrap != null) bootstrap.releaseExternalResources
  }
}