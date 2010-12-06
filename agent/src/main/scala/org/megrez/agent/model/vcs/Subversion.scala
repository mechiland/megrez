package org.megrez.agent.model.vcs

class Subversion(val url: String) extends org.megrez.model.vcs.Subversion with org.megrez.ChangeSource {
  override def toString = "Subversion[url=" + url + "]"

  override def equals(x: Any): Boolean = x match {
    case that: Subversion => this.url == that.url
    case _ => false
  }
}