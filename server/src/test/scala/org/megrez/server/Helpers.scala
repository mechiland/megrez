package org.megrez.server

import java.io.File
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.EmbeddedGraphDatabase
import io.Source
import java.util.UUID
import java.util.concurrent.Executors
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
import org.jboss.netty.bootstrap.ClientBootstrap
import org.jboss.netty.channel._
import java.net.{URI, InetSocketAddress}
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.util.CharsetUtil

trait IoSupport {
  def delete(file: File) {
    file.listFiles.foreach {
      file =>
        if (file.isDirectory) delete(file) else file.delete
    }
    file.delete
  }
}

trait Neo4JSupport {
  self: IoSupport =>
  private val database = new File(System.getProperty("user.dir"), "database_" + UUID.randomUUID.toString)

  protected var neo: GraphDatabaseService = null

  object Neo4J {
    def start() {
      database.mkdirs
      neo = new EmbeddedGraphDatabase(database.getAbsolutePath)
    }

    def shutdown() {
      neo.shutdown
      delete(database)
    }
  }
}

trait SvnSupport {
  self: IoSupport =>

  private val subversion: File = new File(System.getProperty("user.dir"), "subversion_" + UUID.randomUUID.toString)

  object Svn {
    def create(name: String) = {
      val dir = new File(subversion, "repository_" + System.currentTimeMillis)
      dir.mkdirs
      run("svnadmin create " + name, dir)
      "file://" + new File(dir, name).getAbsolutePath
    }

    def checkin(repository: File, file: String) {
      val revision = new File(repository, file)
      revision.createNewFile

      run("svn add " + revision.getAbsolutePath)
      run("svn ci . -m \"checkin\"", repository)
    }

    def checkout(url: String) = {
      val target = new File(subversion, "checkout_" + System.currentTimeMillis)
      run("svn co " + url + " " + target)
      target
    }

    def clean() = delete(subversion)
  }

  private def run(command: String) {
    run(command, subversion)
  }

  private def run(command: String, workingDir: File) {
    val cmd = Runtime.getRuntime().exec(command, null, workingDir)
    cmd.waitFor match {
      case 0 =>
      case _ => throw new Exception(Source.fromInputStream(cmd.getErrorStream).mkString)
    }
  }
}

trait HttpClientSupport {
  object HttpClient {
    import scala.collection.JavaConversions._

    def bootstrap(handler: SimpleChannelUpstreamHandler) = {
      val httpClientBootstrap = new ClientBootstrap(
        new NioClientSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()))

      httpClientBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
        def getPipeline = {
          val pipeline = Channels.pipeline
          pipeline.addLast("codec", new HttpClientCodec())
          pipeline.addLast("inflater", new HttpContentDecompressor())
          pipeline.addLast("aggregator", new HttpChunkAggregator(1048576))
          pipeline.addLast("handler", handler)
          pipeline
        }
      })
      httpClientBootstrap
    }

    def get(uri: URI) = {
      var status  = 200
      var headers : Map[String, List[String]] = Map()
      var content = ""
      val httpClientBootstrap = bootstrap(new SimpleChannelUpstreamHandler() {
        override def messageReceived(context: ChannelHandlerContext, event: MessageEvent) {
          event.getMessage match {
            case response : HttpResponse =>
              headers = response.getHeaderNames.map(name => name -> response.getHeaders(name).toList).toMap
              val buffer = response.getContent()
              content = if (buffer.readable) buffer.toString(CharsetUtil.UTF_8) else ""
              status = response.getStatus.getCode
            case _ =>
          }
        }
      })
      val future = httpClientBootstrap.connect(new InetSocketAddress(uri.getHost, uri.getPort))
      val channel = future.awaitUninterruptibly.getChannel
      val request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toASCIIString())
      request.setHeader(HttpHeaders.Names.HOST, uri.getHost)
      request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE)
      request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP)
      channel.write(request)
      channel.getCloseFuture.awaitUninterruptibly
      httpClientBootstrap.releaseExternalResources
      (status, headers, content)
    }
  }
}