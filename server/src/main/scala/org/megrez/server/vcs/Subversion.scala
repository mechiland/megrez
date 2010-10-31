package org.megrez.server.vcs

import xml.XML

class Subversion(val url: String) extends VersionControl {
  private var revision: Int = -1

  def changes: Option[Any] = {
    val process = Runtime.getRuntime().exec("svn info " + url + " --xml")
    process.waitFor match {
      case 0 =>
        val svnInfo = XML.load(process.getInputStream)
       val commit = Integer.parseInt(((svnInfo \\ "entry")(0) \ "@revision").text)
        if (commit > revision) {
          revision = commit
          Some(revision)
        } else None
      case _ => None
    }
  }
}