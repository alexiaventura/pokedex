package com.example.project_dex
//..
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.Headers

@Composable
fun PokemonDetailScreen(pokemonUrl: String, modifier: Modifier = Modifier) {
    var pokemonDetails by remember { mutableStateOf<PokemonDetails?>(null) }
    var locations by remember { mutableStateOf<List<LocationAreaEncounter>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val json = Json { ignoreUnknownKeys = true }

    LaunchedEffect(pokemonUrl) {
        val client = AsyncHttpClient()

        client.get(pokemonUrl, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, jsonResponse: JSON) {
                try {
                    val details = json.decodeFromString<PokemonDetails>(jsonResponse.jsonObject.toString())
                    pokemonDetails = details

                    client.get(details.location_area_encounters, object : JsonHttpResponseHandler() {
                        override fun onSuccess(statusCode: Int, headers: Headers, jsonResponse: JSON) {
                            try {
                                val locationList = json.decodeFromString(
                                    ListSerializer(LocationAreaEncounter.serializer()),
                                    jsonResponse.jsonArray.toString()
                                )
                                locations = locationList
                            } catch (e: Exception) {
                                error = "Failed to parse location data."
                            } finally {
                                isLoading = false
                            }
                        }

                        override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                            isLoading = false
                        }
                    })
                } catch (e: Exception) {
                    error = "Failed to parse Pokémon details."
                    isLoading = false
                }
            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                error = "Failed to fetch Pokémon data. Status: $statusCode"
                isLoading = false
            }
        })
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }
            error != null -> {
                Text(text = error!!)
            }
            pokemonDetails != null -> {
                val details = pokemonDetails!!
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()), // Make column scrollable
                    horizontalAlignment = Alignment.CenterHorizontally // Center content
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(details.sprites.front_default)
                            .crossfade(true)
                            .build(),
                        contentDescription = "${details.name} sprite",
                        modifier = Modifier.size(128.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = details.name.replaceFirstChar { it.titlecase() }, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        details.types.forEach {
                            PokemonTypeView(type = it.type.name)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Abilities: ${details.abilities.joinToString { it.ability.name.replaceFirstChar { c -> c.titlecase() } }}")
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Base Stats:", fontSize = 20.sp)
                    details.stats.forEach { statInfo ->
                        Text("${statInfo.stat.name.replace("-", " ").replaceFirstChar { it.titlecase() }}: ${statInfo.base_stat}")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Game Locations:", fontSize = 20.sp)
                    if (locations.isNotEmpty()) {
                        val locationNames = locations.joinToString(separator = "\n") {
                            "- " + it.location_area.name
                                .replace("-", " ")
                                .split(" ")
                                .joinToString(" ") { word -> word.replaceFirstChar { char -> char.titlecase() } }
                        }
                        Text(locationNames)
                    } else {
                        Text("No location data found for this Pokémon in the games.")
                    }
                }
            }
        }
    }
}
