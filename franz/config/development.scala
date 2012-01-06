import com.exacttarget.franz.config.FranzServiceConfig
import com.twitter.logging.config._
import com.twitter.ostrich.admin.config._
import java.util.Properties
import java.io.FileInputStream
import collection.JavaConversions._

// development mode.
new FranzServiceConfig {
  // Ostrich http admin port.  Curl this for stats, etc
  admin.httpPort = 9900

  // End user configuration

  // Expert-only: Ostrich stats and logger configuration.

  admin.statsNodes = new StatsConfig {
    reporters = new TimeSeriesCollectorConfig
  }

  val franzProps = new Properties() {
    load(new FileInputStream("/etc/franz/franz.properties"))
    //put("kafkaTopics", "test1,test2,test3")
  }

  kafkaReadTopics = franzProps.get("kafkaReadTopics")
    .asInstanceOf[String]
    .split(",").foldLeft(Map[String,Int]()){ (m, topic) =>m + (topic -> 1)
  }

  kafkaWriteTopics = franzProps.get("kafkaWriteTopics")
    .asInstanceOf[String]
    .split(",").foldLeft(Map[String,Int]()){ (m, topic) =>m + (topic -> 1)
  }

  threadPoolSize = (kafkaReadTopics.size + kafkaWriteTopics.size) * 20

  kafkaConsumerProps = new Properties() {
    load(new FileInputStream("/etc/exacttarget/kafka_zookeeper-c1.properties"))
    putAll(franzProps)
    put("serializer.class", "com.exacttarget.franz.ByteEncoder")
    //put("zk.connect", "nvqa2s1hc1aggr01.np.local:2181")
    //put("groupid", "groupid")
  }

  kestrelQueueFolder = "/var/spool/kestrel"

  loggers =
    new LoggerConfig {
      level = Level.DEBUG
      handlers = new FileHandlerConfig {
        filename = "franz.log"
        roll = Policy.SigHup
      }
    } :: new LoggerConfig {
      node = "history"
      level = Level.INFO
      useParents = false
      handlers = new FileHandlerConfig {
       filename = "history.log"
       roll = Policy.Daily
      }
    } :: new LoggerConfig {
      node = "stats"
      level = Level.INFO
      useParents = false
      handlers = new FileHandlerConfig {
        filename = "stats.log"
        formatter = BareFormatterConfig
      }
    }
}
