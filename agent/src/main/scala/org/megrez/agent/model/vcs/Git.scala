package org.megrez.agent.model.vcs

class Git(val url: String) extends org.megrez.model.vcs.Git with org.megrez.ChangeSource {
  override def toString = "Git[url=" + url + "]"

  override def equals(x: Any): Boolean = x match {
    case that: Git => this.url == that.url
    case _ => false
  }
}