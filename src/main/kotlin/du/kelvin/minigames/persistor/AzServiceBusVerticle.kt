package du.kelvin.minigames.persistor

import com.azure.messaging.servicebus.ServiceBusClientBuilder
import com.azure.messaging.servicebus.ServiceBusErrorContext
import com.azure.messaging.servicebus.ServiceBusException
import com.azure.messaging.servicebus.ServiceBusProcessorClient
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext
import du.kelvin.minigames.persistor.dto.TestDto
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.serialization.json.Json

class AzServiceBusVerticle : CoroutineVerticle() {

  private var sbClient: ServiceBusProcessorClient? = null

  override suspend fun start() {
    super.start()
    val connectionString = config.getString("connection_string")
    val topic = config.getString("topic")
    val subscription = config.getString("subscription")

    sbClient = ServiceBusClientBuilder()
      .connectionString(connectionString)
      .processor()
      .topicName(topic)
      .subscriptionName(subscription)
      .processMessage { receivedMessageContext -> onMessage(receivedMessageContext) }
      .processError { errorContext -> onError(errorContext) }
      .buildProcessorClient()

    println("Starting client processor")
    sbClient!!.start()
    println("Consumer started!")
  }

  override suspend fun stop() {
    super.stop()
    sbClient!!.stop()
  }

  private fun onMessage(context: ServiceBusReceivedMessageContext) {
    println("Processing message. Sequence #: ${context.message.sequenceNumber}")
    vertx.eventBus().send("event.test.persistor", context.message.body.toString())
  }

  private fun onError(context: ServiceBusErrorContext) {
    if (context.exception is ServiceBusException) {
      val exception = context.exception as ServiceBusException
      println("Error source: ${context.errorSource}, reason ${exception.reason}")
    } else {
      println("Error occurred: ${context.exception}")
    }
  }

//  @Throws(Exception::class)
//  override fun start(startPromise: Promise<Void?>?) {
//    val client: ServiceBusProcessorClient = ServiceBusClientBuilder().connectionString(CONNECTION_STRING)
//      .processor()
//      .topicName(TOPIC)
//      .subscriptionName(SUBSCRIPTION)
//      .processMessage(MainVerticle::onMessage)
//      .processError(MainVerticle::onError)
//      .buildProcessorClient()
//    println("Starting client processor")
//    client.start()
//    println("Consumer started!")
//    super.start()
//  }
//
//  private fun onMessage(context: ServiceBusReceivedMessageContext) {
//    val subject: String = java.lang.String.valueOf(context.getMessage().getApplicationProperties().get("label"))
//    val `object`: SomeStuff = context.getMessage().getBody().toObject(SomeStuff::class.java)
//    val log = java.lang.String.format(
//      "subject:%s id:%s name:%s description:%s",
//      subject,
//      `object`.getId(),
//      `object`.getName(),
//      `object`.getDescription()
//    )
//    println(log)
//  }
//
//  private fun onError(context: ServiceBusErrorContext) {
//    System.out.printf("Error exception: %s\n", context.getException().getMessage())
//  }

}
