package com.exacttarget.franz

import config.FranzServiceConfig
import collection.mutable.ListBuffer
import com.twitter.conversions.time._
import com.twitter.conversions.storage._
import com.twitter.finagle.builder.Server
import com.twitter.ostrich.admin.Service
import com.twitter.util.{FuturePool, JavaTimer}
import java.util.concurrent.Executors
import net.lag.kestrel._
import net.lag.kestrel.config._
import com.twitter.ostrich.stats.Stats

class FranzServiceServer(config: FranzServiceConfig) extends Service {
  val services = ListBuffer[Server]()
  val queueWriterExecutor = Executors.newFixedThreadPool(2)
  var queueWriterPool = FuturePool(queueWriterExecutor)
  var kestrelServer: Kestrel = null

  def start() {
    // http://robey.github.com/kestrel/doc/main/api/net/lag/kestrel/config/QueueBuilder.html
    var queueBuilders = List[QueueBuilder]()
    config.kafkaTopics.keySet.foreach((topic: String) => {
      queueBuilders = queueBuilders ::: List(new QueueBuilder {
        syncJournal = 1.seconds
        name = topic
      })
    })

    val kestrelConfig = new KestrelConfig {
      queues = queueBuilders
      listenAddress = "0.0.0.0"
      memcacheListenPort = 22133
      textListenPort = 2222
      queuePath = "/var/spool/kestrel"
      clientTimeout = 30.seconds
      expirationTimerFrequency = 1.second
      maxOpenTransactions = 100

      // default queue settings:
      default.defaultJournalSize = 16.megabytes
      default.maxMemorySize = 128.megabytes
      default.maxJournalSize = 1.gigabyte

    }

    kestrelServer = new Kestrel(new QueueBuilder().apply(), kestrelConfig.queues, kestrelConfig.listenAddress,
      kestrelConfig.memcacheListenPort, kestrelConfig.textListenPort, kestrelConfig.queuePath,
      kestrelConfig.protocol, kestrelConfig.expirationTimerFrequency, kestrelConfig.clientTimeout,
      kestrelConfig.maxOpenTransactions)
    kestrelServer.start()
    Stats.addGauge("connections") { Kestrel.sessions.get().toDouble }

    Option(config.kafkaConsumerProps.get("hosts")).map ( hosts =>  {
      val chain = hosts.toString
      if (!chain.isEmpty)
        config.kafkaConsumerProps.put("zk.connect", chain.split(",").toList.map((host: String) =>
        host + ":2181").mkString(","))
      }
    )

    val streamConsumer = new StreamConsumer(config, kestrelServer.queueCollection)
    streamConsumer.init()

    services += streamConsumer
  }

  def shutdown() {
    val close = (server: Server) => {
      synchronized {
        if (server != null) {
          server.close(0.seconds)
        }
      }
    }

    kestrelServer.shutdown()

    services.foreach(close(_))
    queueWriterExecutor.shutdown()
  }
}
