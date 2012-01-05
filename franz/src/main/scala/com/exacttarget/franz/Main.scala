package com.exacttarget.franz

import com.twitter.ostrich.admin.RuntimeEnvironment

object Main {
  def main(args: Array[String]) {
    val env = RuntimeEnvironment(this, args)
    val service = env.loadRuntimeConfig[FranzServiceServer]
    service.start()
  }
}

