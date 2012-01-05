import com.exacttarget.franz.config.FranzServiceConfig
import com.exacttarget.ostrich._
import com.twitter.logging.config._
import com.twitter.ostrich.admin.config._
import java.util.Properties
import java.io.FileInputStream

// production mode.
var graphiteProperties: Properties = new Properties() {
  load(new FileInputStream("/etc/exacttarget/graphite.properties"))
}

new FranzServiceConfig {

  // Add your own config here

  // Where your service will be exposed.
  thriftPort = 9999

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

  kafkaProducerProps = new Properties() {
    //put("zk.connect", "TBD:2181")
    load(new FileInputStream("/etc/exacttarget/kafka_zookeeper-c1.properties"))
    put("serializer.class", "com.exacttarget.franz.ByteEncoder")
  }

  kestrelQueueFolder = "/var/kestrel/journal"
  threadPoolSize = 1

  loggers =
    new LoggerConfig {
      level = Level.INFO
      handlers = new FileHandlerConfig {
        filename = "/var/log/franz/franz.log"
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
    } :: new LoggerConfig {
      node = "stats"
      level = Level.INFO
      useParents = false
      handlers = new FileHandlerConfig {
        filename = "/var/log/franz/stats.log"
        formatter = BareFormatterConfig
      }
    }
}
