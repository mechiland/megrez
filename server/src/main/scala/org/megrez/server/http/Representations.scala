package org.megrez.server.http

import collection.mutable.HashMap
import java.lang.Class
import java.io.File
object Representations {
  private val map = new HashMap[Class[_],File]()

  def register[T](dir : File)(implicit manifest: Manifest[T]) {
      map.put(manifest.erasure, dir)
  }

  def getTemplateFolder(resourceClass: Class[_]) = map(resourceClass)
}