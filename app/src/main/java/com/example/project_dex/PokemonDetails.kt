package com.example.project_dex
import kotlinx.serialization.Serializable

@Serializable
data class PokemonDetails(
    val id: Int,
    val name: String,
    val types: List<TypeInfo>,
    val abilities: List<AbilityInfo>,
    val stats: List<StatInfo>,
    val sprites: PokemonSprites,
    val location_area_encounters: String // URL for location details
)

@Serializable
data class TypeInfo(
    val type: NamedApiResource
)

@Serializable
data class AbilityInfo(
    val ability: NamedApiResource
)

@Serializable
data class StatInfo(
    val stat: NamedApiResource,
    val base_stat: Int
)

@Serializable
data class PokemonSprites(
    val front_default: String
)

@Serializable
data class NamedApiResource(
    val name: String,
    val url: String
)

// New data class for location information
@Serializable
data class LocationAreaEncounter(
    val location_area: NamedApiResource
)
