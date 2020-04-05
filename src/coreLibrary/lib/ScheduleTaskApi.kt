package coreLibrary.lib

import kotlinx.coroutines.*
import java.util.*

/**
 * use script.enabled to cancel to prevent leak
 */
object SharedTimer : Timer("ScriptAgentTimer", true)

/**
 * use script.enabled to cancel to prevent leak
 */
val SharedCoroutineScope = GlobalScope +
        SupervisorJob() + Dispatchers.Default + CoroutineName("ScriptAgentCoroutine")