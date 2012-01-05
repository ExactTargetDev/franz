package com.exacttarget.franz

import kafka.message.Message
import kafka.serializer.Encoder

class ByteEncoder extends Encoder[Array[Byte]] {
  override def toMessage(event: Array[Byte]): Message = new Message(event)
}
