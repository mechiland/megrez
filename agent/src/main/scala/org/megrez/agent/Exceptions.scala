package org.megrez.agent

import java.net.URI

class NotMegrezServerException(val uri: URI) extends Exception