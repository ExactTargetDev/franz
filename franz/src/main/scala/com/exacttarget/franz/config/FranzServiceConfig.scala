package com.exacttarget.franz.config

import com.twitter.ostrich.admin.RuntimeEnvironment
import com.twitter.ostrich.admin.config._
import com.exacttarget.franz.FranzServiceServer
import java.util.Properties

abstract class FranzServiceConfig extends ServerConfig[FranzServiceServer] {
  var threadPoolSize: Int = 1
  var kestrelQueueFolder: String = null

  var kafkaConsumerProps: Properties = null
  var kafkaTopics: Map[String, Int] = null

  def apply(runtime: RuntimeEnvironment) = new FranzServiceServer(this)
}
