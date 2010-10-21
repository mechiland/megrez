package org.megrez.server

class Pipeline

class Job(val name : String, val resources : Set[String], val tasks : List[Task])

class Task