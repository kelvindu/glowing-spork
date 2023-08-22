package du.kelvin.minigames.persistor.dto

import kotlinx.serialization.Serializable

@Serializable
data class TestDto(
  val code: String,
  val name: String,
  val createdAt: String,
  val description: String
)

