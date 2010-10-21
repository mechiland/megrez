package org.megrez.server

import java.net.InetSocketAddress
import java.util.concurrent.Executors

import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.channel._

object Main {  
  def main(args: Array[String]) {
    val bootstrap = new ServerBootstrap(
      new NioServerSocketChannelFactory(
        Executors.newCachedThreadPool(),
        Executors.newCachedThreadPool()));

    bootstrap.setPipelineFactory(PipelineFactory);
    bootstrap.bind(new InetSocketAddress(8080));
  }

  object PipelineFactory extends ChannelPipelineFactory {
    override def getPipeline() = {
      val pipeline = Channels.pipeline();
      pipeline.addLast("decoder", new HttpRequestDecoder());
      pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
      pipeline.addLast("encoder", new HttpResponseEncoder());
      pipeline.addLast("handler", Handler);
      pipeline
    }
  }

  object Handler extends SimpleChannelUpstreamHandler {
    override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
      event.getMessage match {
        case http: HttpRequest => handleHttpRequest(context, http)
      }
    }

    override def exceptionCaught(context: ChannelHandlerContext, event: ExceptionEvent) {
    }

    private def handleHttpRequest(context: ChannelHandlerContext, request: HttpRequest) {
      println(request.getMethod)
    }
  }
}
