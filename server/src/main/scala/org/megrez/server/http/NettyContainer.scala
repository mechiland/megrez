package org.megrez.server.http

import com.sun.jersey.api.core.ResourceConfig
import org.jboss.netty.channel._
import com.sun.jersey.spi.container.{ContainerRequest, ContainerResponse, ContainerResponseWriter, WebApplication}
import org.jboss.netty.handler.codec.http._
import java.net.URI
import com.sun.jersey.core.header.InBoundHeaders
import org.jboss.netty.buffer.{ChannelBufferInputStream, ChannelBufferOutputStream, ChannelBuffers}

class NettyContainer(val application: WebApplication) extends SimpleChannelUpstreamHandler {
  import scala.collection.JavaConversions._
  
  override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
    event.getMessage match {
      case request: HttpRequest =>        
        val base = "http://" + request.getHeader(HttpHeaders.Names.HOST) + "/"
        val path = request.getUri.substring(request.getUri.indexOf("/", base.length))

        val headers = new InBoundHeaders()
        request.getHeaderNames.foreach(name => headers.put(name, request.getHeaders(name)))
        val containerRequest = new ContainerRequest(application, request.getMethod.getName,
          new URI(base), new URI(path), headers, new ChannelBufferInputStream(request.getContent))
        application.handleRequest(containerRequest, new NettyContainer.Writer(event.getChannel))
      case _ =>
    }
  }
}

object NettyContainer {
  class Writer(val channel: Channel) extends ContainerResponseWriter {
    import scala.collection.JavaConversions._

    private var httpResponse: org.jboss.netty.handler.codec.http.HttpResponse = null

    def finish {
      channel.write(httpResponse).addListener(ChannelFutureListener.CLOSE)
    }

    def writeStatusAndHeaders(length: Long, response: ContainerResponse) = {
      httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(response.getStatus))
      response.getHttpHeaders.foreach(header => httpResponse.setHeader(header._1, header._2.map(ContainerResponse.getHeaderValue(_))))
      val buffer = ChannelBuffers.dynamicBuffer
      httpResponse.setContent(buffer)
      new ChannelBufferOutputStream(buffer)
    }
  }
}