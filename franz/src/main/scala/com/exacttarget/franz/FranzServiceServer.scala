package com.exacttarget.franz

import config.FranzServiceConfig
import collection.mutable.ListBuffer
import com.twitter.conversions.time._
import com.twitter.conversions.storage._
import com.twitter.finagle.builder.Server
import com.twitter.ostrich.admin.Service
import net.lag.kestrel._
import net.lag.kestrel.config._
import com.twitter.ostrich.stats.Stats
import java.util.concurrent.Executors

class FranzServiceServer(config: FranzServiceConfig) extends Service {
  val services = ListBuffer[Server]()
  var kestrelServer: Kestrel = null

  def start() {
    // http://robey.github.com/kestrel/doc/main/api/net/lag/kestrel/config/QueueBuilder.html
    var queueBuilders = List[QueueBuilder]()

    config.kafkaReadTopics.keySet.union(config.kafkaWriteTopics.keySet).foreach((topic: String) => {
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
      queuePath = config.kestrelQueueFolder
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

    if (config.kafkaReadTopics.size > 0) {
      val streamConsumer = new StreamConsumer(config, kestrelServer.queueCollection)
      streamConsumer.init()
      services += streamConsumer
    }

    if (config.kafkaWriteTopics.size > 0) {
      val streamProducer = new StreamProducer(kestrelServer.queueCollection, config.kafkaWriteTopics.keySet,
        config.kafkaConsumerProps, Executors.newFixedThreadPool(config.kafkaWriteTopics.size))
      streamProducer.init()
      services += streamProducer
    }
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
  }
}
