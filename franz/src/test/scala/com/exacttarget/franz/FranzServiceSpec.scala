package com.exacttarget.franz

import org.jboss.netty.handler.codec.http._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._
import com.twitter.finagle.Service
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write => swrite}
import org.jboss.netty.buffer.ChannelBuffers.copiedBuffer
import org.jboss.netty.util.CharsetUtil.UTF_8
import kafka.utils._
import kafka.zk.EmbeddedZookeeper
import kafka.server._
import com.twitter.finagle.builder.ClientBuilder

import kafka.api.FetchRequest;
import kafka.javaapi.consumer.SimpleConsumer;



import kafka.utils.Utils;
import java.net.URLEncoder;
import java.io.File;

class FranzServiceSpec extends AbstractSpec {

  var kafkaServer: KafkaServer = null
  val kafkaPort = 9092
  val zkServer = new EmbeddedZookeeper(TestZKUtils.zookeeperConnect)
  val kafkaConfig = new KafkaConfig(TestUtils.createBrokerConfig(0, kafkaPort))
  var kafkaMessageCount:Int = 0

  doBeforeSpec {

    new File(franz.configuration.kestrelQueueFolder, franz.configuration.kestrelQueueName).delete
    kafkaServer = TestUtils.createServer(kafkaConfig)

    Thread.sleep(500)
    franz.start()

    client = ClientBuilder()
                .codec(Http.get)
                .hosts("localhost:8080")
                .hostConnectionLimit(1)
                .build()
  }

  doAfterSpec {
    franz.shutdown()

    if (null != kafkaServer) {
      kafkaServer.shutdown()
      Utils.rm(kafkaServer.config.logDir)
      Utils.rm(kafkaServer.config.logDir)
      Thread.sleep(500);
    }

    if (null != zkServer) zkServer.shutdown()
  }

  "FranzService" should {

    var respond: EventReceiver = null
    implicit val formats = DefaultFormats

    val consumer = new SimpleConsumer("127.0.0.1", 9092, 10000, 1024000);

    "Can SerDe http content container" in {

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
