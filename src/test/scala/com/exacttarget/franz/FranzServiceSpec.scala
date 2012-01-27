package com.exacttarget.franz

import kafka.utils._
import kafka.zk.EmbeddedZookeeper
import kafka.server._
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.utils.Utils;

class FranzServiceSpec extends AbstractSpec {

  var kafkaServer: KafkaServer = null
  val kafkaPort = 9092
  val zkServer = new EmbeddedZookeeper(TestZKUtils.zookeeperConnect)
  val kafkaConfig = new KafkaConfig(TestUtils.createBrokerConfig(0, kafkaPort))
  var kafkaMessageCount:Int = 0

  doBeforeSpec {

    //new File(franz.configuration.kestrelQueueFolder, franz.configuration.kestrelQueueName).delete
    kafkaServer = TestUtils.createServer(kafkaConfig)

    Thread.sleep(500)
    //franz.start()
  }

  doAfterSpec {
    //franz.shutdown()

    if (null != kafkaServer) {
      kafkaServer.shutdown()
      Utils.rm(kafkaServer.config.logDir)
      Utils.rm(kafkaServer.config.logDir)
      Thread.sleep(500);
    }

    if (null != zkServer) zkServer.shutdown()
  }

  "FranzService" should {

    //var respond: EventReceiver = null
    val consumer = new SimpleConsumer("127.0.0.1", 9092, 10000, 1024000);

    "should drop messages from topic onto queue" in {

      //val instance = HttpEvent(EventContext(1234,2345, SocialPageEventType.socialPageClick.toString),
      //        Map("tabid" -> "1"))
      //    val json = swrite(instance)

      //  val event = parse(json).extract[HttpEvent]
      //event.context.mid mustEqual 2345
      //SocialPageEventType.withName(event.context.eventType) mustEqual SocialPageEventType.socialPageClick

      //read[HttpEvent](json) mustEqual instance
    }

  }

}
