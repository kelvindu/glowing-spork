package du.kelvin.minigames.persistor

import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.AsyncResult
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject


fun main() {
  val vertx = Vertx.vertx()
  val azServiceBusVerticle = AzServiceBusVerticle()
  val persistorVerticle = PersistorVerticle()
  // Create the config retriever
  val retriever = ConfigRetriever.create(
    vertx, ConfigRetrieverOptions()
      .addStore(ConfigStoreOptions().setType("file").setConfig(JsonObject().put("path", "vertx.json")))
  )
  // Retrieve the configuration
  retriever.config.onComplete { json: AsyncResult<JsonObject> ->
    val result = json.result()
    // Close the vert.x instance, we don't need it anymore.
    vertx.close()
    // Create a new Vert.x instance using the retrieve configuration
    val options = VertxOptions(result)
    val newVertx = Vertx.vertx(options)

    // Service bus consumer
    newVertx.deployVerticle(
      azServiceBusVerticle,
      DeploymentOptions().setConfig(result.getJsonObject("az_servicebus"))
    )

    // Postgres Persistor
    newVertx.deployVerticle(
      persistorVerticle,
      DeploymentOptions().setConfig(result.getJsonObject("postgres"))
    )
  }
}
