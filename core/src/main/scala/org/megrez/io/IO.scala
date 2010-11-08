package org.megrez.io

trait Reader[Resource, Format] {
  def read(representation: Format): Resource
}

trait Write[Resource, Format] {
  def write(resource : Resource):String
}
