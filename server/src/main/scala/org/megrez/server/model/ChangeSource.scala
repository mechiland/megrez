package org.megrez.server.model

import data.{Pluggable, Entity}

abstract class ChangeSource extends Entity with org.megrez.model.ChangeSource

object ChangeSource extends Pluggable[ChangeSource]