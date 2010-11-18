package org.megrez.server

object Daemon {

  def main(args: Array[String]) {
    Main.main(args)
    while(true) {}
  }

}