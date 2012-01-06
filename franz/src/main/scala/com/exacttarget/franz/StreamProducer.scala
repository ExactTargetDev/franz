package com.exacttarget.franz

import scala.collection.{Set => SSet}
import com.twitter.conversions.time._
import com.twitter.finagle.builder.Server
import com.twitter.logging.Logger
import com.twitter.util.Duration
import kafka.producer.{ProducerData, ProducerConfig, Producer}
import java.util.Properties
import java.util.concurrent.{ExecutorService, TimeUnit}
import net.lag.kestrel.{QueueCollection, QItem, PersistentQueue}

class StreamProducer(val queues: QueueCollection, val topics: SSet[String],
                     val producerProperties: Properties, val pool: ExecutorService) extends Server {

  val log = Logger.get(getClass.getName)

  def init() {
    topics.foreach((topic: String) => {
      log.debug("Creating stream producer for `" + topic + "'")
      val producer : Producer[String, Array[Byte]] = new Producer(new ProducerConfig(producerProperties))
      pool.submit(new KafkaPump(producer, queues.queue(topic).get))
    })
  }

  def close(timeout: Duration) {
    pool.shutdown()
    pool.awaitTermination(timeout.inMilliseconds, TimeUnit.MILLISECONDS)
  }

  class KafkaPump(val producer: Producer[String, Array[Byte]], val queue: PersistentQueue) extends Runnable {
    def enqueue(item:QItem) {
      producer.send(new ProducerData[String, Array[Byte]](queue.name, item.data))
      queue.confirmRemove(item.xid)
    }

    def run() {
      while(true) {
        queue.waitRemove(Option(2.seconds.fromNow), true)() match {
          case None => ()
          case Some(item) => enqueue(item)
        }
      }
    }
  }

}