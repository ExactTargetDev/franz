package com.exacttarget.franz

import org.specs.Specification
import com.twitter.ostrich.admin._

abstract class AbstractSpec extends Specification {
  val env = RuntimeEnvironment(this, Array("-f", "config/unit.scala"))
  lazy val franz = {
    val out = env.loadRuntimeConfig[FranzServiceServer]

    // You don't really want the thrift server active, particularly if you
    // are running repetitively via ~test
    ServiceTracker.shutdown // all services
    out
  }
}
