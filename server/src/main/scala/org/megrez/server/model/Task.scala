package org.megrez.server.model

import data.{Pluggable, Entity}

abstract class Task extends Entity with org.megrez.model.Task

object Task extends Pluggable[Task]