package du.kelvin.minigames.persistor

import du.kelvin.minigames.persistor.dto.TestDto
import io.vertx.core.eventbus.Message
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.pgclient.PgConnectOptions
import io.vertx.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import kotlinx.serialization.json.Json
import java.time.LocalDateTime


class PersistorVerticle: CoroutineVerticle() {
  private var client: SqlClient? = null

  override suspend fun start() {
    super.start()
    println("Setting up postgres client pool")
    val connectOptions = PgConnectOptions()
      .setPort(config.getInteger("port"))
      .setHost(config.getString("host"))
      .setDatabase(config.getString("db"))
      .setUser(config.getString("username"))
      .setPassword(config.getString("password"))

    // Pool options
    val poolOptions = PoolOptions()
      .setMaxSize(5)

    // Create the pooled client
    client = PgPool.client(vertx, connectOptions, poolOptions)
    println("postgres connected inserting values")
    vertx.eventBus().consumer<String>("event.test.persistor").handler { message ->
      handlePayload(message)
    }
    println("Persistor created!")
  }

  private fun handlePayload(message: Message<String>) {
    val payload = Json.decodeFromString<TestDto>(message.body())
    println("deserialized payload ${payload}")

    client!!
      .preparedQuery("INSERT INTO item_stub (code, name, description, \"createdAt\") VALUES ($1, $2, $3, $4)")
      .execute(Tuple.of(payload.code, payload.name,payload.description, LocalDateTime.parse(payload.createdAt)))
      .onComplete { ar ->
        if (ar.succeeded()) {
          println("Success::${ar.succeeded()}")
        } else {
          println("Failure ${ar.cause().message}")
        }
    }
  }

  override suspend fun stop() {
    super.stop()
    client!!.close()
  }
}
