import com.exacttarget.franz.config.FranzServiceConfig
import com.twitter.logging.config._
import com.twitter.ostrich.admin.config._
import java.util.Properties

// production mode.
new FranzServiceConfig {
  // Ostrich http admin port.  Curl this for stats, etc
  admin.httpPort = 9900

  // End user configuration

  // Expert-only: Ostrich stats and logger configuration.

  admin.statsNodes = new StatsConfig {
    reporters = new TimeSeriesCollectorConfig
  }

  kafkaProducerProps = new Properties() {
    put("zk.connect", "localhost:2182")
    put("serializer.class", "com.exacttarget.franz.ByteEncoder")
  }

  kestrelQueueFolder = "/tmp"
  threadPoolSize = 1

  loggers =
    new LoggerConfig {
      level = Level.INFO
      handlers = new FileHandlerConfig {
        filename = "/tmp/franz.log"
        roll = Policy.SigHup
      }
    } :: new LoggerConfig {
      node = "stats"
      level = Level.INFO
      useParents = false
      handlers = new FileHandlerConfig {
        filename = "/tmp/stats.log"
        formatter = BareFormatterConfig
      }
    }
}
