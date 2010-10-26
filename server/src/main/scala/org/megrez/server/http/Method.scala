package org.megrez.server.http

class Method
object GET extends Method{
	override def toString = {
		"GET"
	}
}
object PUT extends Method{
	override def toString = {
		"PUT"
	}
}
object DELETE extends Method{
	override def toString = {
		"DELETE"
	}
}
object POST extends Method{
	override def toString = {
		"POST"
	}
}
