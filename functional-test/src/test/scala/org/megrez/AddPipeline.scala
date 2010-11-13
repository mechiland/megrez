package org.megrez

import java.io.{DataOutputStream, InputStream}
import java.net.{URL, URLEncoder}

object AddPipeline {
  def main(args: Array[String]) {
    val json = """{"name":"megrez","materials":[{"type":"git","url":"git@github.com:vincentx/megrez.git","dest":"$main"}],"stages":[{"name":"test","jobs":[{"name":"test","resources":[],"tasks":[{"type":"cmd","command":"buildr clean test"}]}]}]}"""

    val url = new URL("http://localhost:8051/pipelines")
    val connection = url.openConnection
    connection.setDoInput(true)
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    val out = new DataOutputStream(connection.getOutputStream)
    out.writeBytes("pipeline=" + URLEncoder.encode(json, "UTF-8"))
    out.flush
    out.close

    val inputStream: InputStream = connection.getInputStream
  }
}
