package org.megrez.agent

case class JobAssignment(val pipelineId : String, val versionControl : VersionControl, val workSet : Any, val job : Job)
case class JobCompleted(val pipelineId : String, val workSet : Any)
