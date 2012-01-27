# Franz

Franz is a [Kestrel](http://github.com/robey/kestrel) interface to [Kafka](http://incubator.apache.org/kafka/).  Franz is designed to be a bridge for non-JVM languages that do not have a full Kafka client implementation.  Since there is a solid memcached client for almost every language, Kestrel is used to read and write data to Kafka.

Franz is also useful if you are looking for local persistent of messages in the case of network failures, etc.  Franz will buffer data to local disk until Kafka becomes available.

# Building

Franz requires SBT 0.7.x. 

```bash
sbt update compile
```

# Running

Open an SBT shell by running "sbt".  Then do:

```bash
run -f config/development.scala
```

to start an instance of Franz.

# Configuration

Here is a sample /etc/franz/franz.properties file:

```
zk.connect=localhost:2181
kafkaReadTopics=testRead1,testRead2,testRead3
kafkaWriteTopics=testWrite1,testWrite2,testWrite3
groupid=groupid
```

The properties for read and write topics define the names of the topics in Kafka to read or write to.  The kestrel queue names will match thet Kafka topic names directly.  If you are trying to write to a Kafka topic, you would place an item on the kestrel queue matching the topic name.  If you want to read a Kafka topic, then you would retreive from the kestrel queue matching the topic name.

The groupid property defines the consumer group for Kafka.

# Demo

Assuming you have downloaded Kafka and are in the root of the distribution, run:

```bash
sbt update compile
./bin/zookeeper-server-start.sh config/zookeeper.properties
./bin/kafka-server-start.sh config/server.properties
```

This will have Kafka up and running.  Next startup Franz using the steps listed above.  Then use the Kafka producer shell to put some text messages into Kafka:

```
./bin/kafka-producer-shell.sh --server kafka://localhost:9092 --topic testRead1
```

Once this is done, you can look at /var/spool/kestrel to see that the queues are created and have data.  A sample script to consume Kestrel from Ruby is located in src/main/scripts.

```ruby
require 'rubygems'
require 'kestrel'

queue = Kestrel::Client.new('localhost:22133')
queue.set 'testWrite1', 'testing123'
print queue.get('testRead1')
```
