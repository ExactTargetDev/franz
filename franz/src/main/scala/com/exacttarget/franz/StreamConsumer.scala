package com.exacttarget.franz

import kafka.consumer.{KafkaMessageStream, Consumer, ConsumerConnector, ConsumerConfig}
import kafka.message.Message
import com.twitter.finagle.builder.Server
import com.twitter.util.Duration
import java.util.concurrent.TimeUnit
import com.twitter.ostrich.stats.Stats
import java.util.concurrent.Executors
import com.twitter.logging._
import config.FranzServiceConfig
import net.lag.kestrel.QueueCollection

class StreamConsumer(val config: FranzServiceConfig, val queues: QueueCollection) extends Server {

  val log = Logger.get(getClass.getName)
  val pool = Executors.newFixedThreadPool(config.threadPoolSize)

  def init() {
    val consumerConfig = new ConsumerConfig(config.kafkaConsumerProps)
    val consumerConnector: ConsumerConnector = Consumer.create(consumerConfig)
    val topicMessageStreams = consumerConnector.createMessageStreams(config.kafkaReadTopics)

    for (streams <- topicMessageStreams) {
      streams._2.foreach(s =>  {
        log.info("Creating stream processor for topic '%s'".format(streams._1))
        pool.submit(new StreamProcessor(streams._1, queues, s))
      })
    }
  }

  def close(timeout: Duration) {
    pool.shutdown()
    pool.awaitTermination(timeout.inMilliseconds, TimeUnit.MILLISECONDS)
  }

  class StreamProcessor(val topic: String, val queues: QueueCollection,
                        val stream: KafkaMessageStream[Message]) extends Runnable {
    def run() {
      stream.foreach(m  => {
        Stats.time("enqueue_message_to_kestrel") {
          log.debug("Receiving message from Kafka, enqueuing into kestrel topic: " + topic)
          val buffer = m.payload
          val payload = new Array[Byte](buffer.remaining)
          buffer.get(payload)
          queues.add(topic, payload)
        }
        Stats.incr("total_messages_queued")
      })
    }
  }

}
