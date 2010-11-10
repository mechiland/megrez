package org.megrez.util

trait Serializer[Resource, Format] {
  def read(representation : Format) : Resource
  def write(resource : Resource) : Format
}
