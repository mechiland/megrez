package org.megrez.server.vcs

trait VersionControl {
  def changes : Option[Any]
}