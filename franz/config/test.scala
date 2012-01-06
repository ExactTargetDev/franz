import com.exacttarget.franz.config.FranzServiceConfig
import com.exacttarget.ostrich._
import com.twitter.logging.config._
import com.twitter.ostrich.admin.config._
import java.util.Properties
import java.io.FileInputStream

// development mode.
var graphiteProperties: Properties = new Properties() {
  load(new FileInputStream("/etc/exacttarget/graphite.properties"))
}

new FranzServiceConfig {
  // Ostrich http admin port.  Curl this for stats, etc
  admin.httpPort = 9900

  // End user configuration

  // Expert-only: Ostrich stats and logger configuration.
  admin.statsNodes = List(new StatsConfig {
    reporters = List(new TimeSeriesCollectorConfig,
      new GraphiteStatsLoggerConfig {
        host = graphiteProperties.getProperty("graphite")
        serviceName = Option("franz")
        prefix = "franz"
      }
    )
  })

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

  threadPoolSize = kafkaReadTopics.size * 20

  kafkaConsumerProps = new Properties() {
    putAll(franzProps)
    put("serializer.class", "com.exacttarget.franz.ByteEncoder")
  }

  kestrelQueueFolder = "/var/spool/kestrel"

  loggers =
    new LoggerConfig {
      level = Level.INFO
      handlers = new FileHandlerConfig {
        filename = "/var/log/social-franz/franz.log"
        roll = Policy.SigHup
      }
    } :: new LoggerConfig {
      node = "history"
      level = Level.INFO
      useParents = false
      handlers = new FileHandlerConfig {
       filename = "/var/log/social-franz/history.log"
       roll = Policy.Daily
      }
    }  :: new LoggerConfig {
      node = "stats"
      level = Level.INFO
      useParents = false
      handlers = new FileHandlerConfig {
        filename = "/var/log/social-franz/stats.log"
        formatter = BareFormatterConfig
      }
    }
}
