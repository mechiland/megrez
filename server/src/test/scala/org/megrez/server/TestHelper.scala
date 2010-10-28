package org.megrez.server

trait TestHelper {
  def megrezParentFolder(): String = {
    System.getProperty("user.dir").split("/megrez")(0)
  }

}