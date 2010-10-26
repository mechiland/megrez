package org.megrez.server

import java.util.Calendar

class Pipeline(val name:String, val repositoryUrl:String, val buildRevision:String,val buildDate:Calendar=null,val workDir:String ="")

class Job(val name : String, val resources : Set[String], val tasks : List[Task])

class Task