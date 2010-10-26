package org.megrez.agent

import java.util.concurrent.Executors

import org.jboss.netty.bootstrap._
import org.jboss.netty.channel._
import org.jboss.netty.channel.socket.nio._
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpHeaders._
import org.jboss.netty.handler.codec.http.HttpHeaders.Names._
import org.jboss.netty.handler.codec.http.HttpHeaders.Values._
import org.jboss.netty.handler.codec.http.HttpVersion._
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.jboss.netty.handler.codec.http.websocket._
import java.net._

import actors.Actor
import java.io.File

object Main {
  def main(args: Array[String]) {
    val worker = new Worker(new FileWorkspace(new File(args.last)))
    worker.start
    val serverConnection = new Server(new URI(args.head), 5000, worker)
    serverConnection.connect
  }
}



