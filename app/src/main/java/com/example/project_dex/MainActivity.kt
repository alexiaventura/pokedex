
package com.example.project_dex
//.
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.project_dex.ui.theme.Project_dexTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Headers
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

// Data class for the list items from PokeAPI
@Serializable
data class ApiResource(
    val name: String,
    val url: String
)

@Serializable
data class ApiResponse(
    val results: List<ApiResource>
)

@Serializable
data class PokemonEncounter(
    val pokemon: ApiResource
)

@Serializable
data class EncounterMethodRate(
    val pokemon_encounters: List<PokemonEncounter>
)

@Serializable
data class LocationArea(
    val pokemon_encounters: List<PokemonEncounter>
)

@Serializable
data class LocationAreaResource(
    val name: String,
    val url: String
)

@Serializable
data class LocationDetails(
    val areas: List<LocationAreaResource>,
    val region: ApiResource?
)

@Serializable
data class PokemonSlot(
    val pokemon: ApiResource
)

@Serializable
data class PokemonTypeDetails(
    val name: String,
    val pokemon: List<PokemonSlot>
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project_dexTheme {
                var currentScreen by remember { mutableStateOf("menu") }
                var selectedPokemonUrl by remember { mutableStateOf<String?>(null) }
                var selectedLocationUrl by remember { mutableStateOf<String?>(null) }
                var selectedTypeUrl by remember { mutableStateOf<String?>(null) }

                // Navigation logic
                val navigateBack = {
                    when {
                        selectedPokemonUrl != null -> selectedPokemonUrl = null
                        selectedTypeUrl != null -> selectedTypeUrl = null
                        selectedLocationUrl != null -> selectedLocationUrl = null
                        currentScreen != "menu" -> currentScreen = "menu"
                    }
                }

                val topBarTitle = when {
                    selectedPokemonUrl != null -> "Pokemon Details"
                    selectedTypeUrl != null -> "Pokémon by Type"
                    selectedLocationUrl != null -> "Pokémon in Area"
                    currentScreen != "menu" -> currentScreen.replaceFirstChar { it.titlecase() } + " List"
                    else -> "PokéDex"
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        // Updated condition to show TopAppBar
                        if (currentScreen != "menu" || selectedPokemonUrl != null || selectedLocationUrl != null || selectedTypeUrl != null) {
                            TopAppBar(
                                title = { Text(topBarTitle) },
                                navigationIcon = {
                                    IconButton(onClick = navigateBack) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)

                    when {
                        selectedPokemonUrl != null -> {
                            PokemonDetailScreen(
                                pokemonUrl = selectedPokemonUrl!!,
                                modifier = screenModifier
                            )
                        }
                        selectedTypeUrl != null -> {
                            TypeDetailScreen(
                                typeUrl = selectedTypeUrl!!,
                                modifier = screenModifier,
                                onPokemonSelected = { url -> selectedPokemonUrl = url }
                            )
                        }
                        selectedLocationUrl != null -> {
                            LocationDetailScreen(
                                locationUrl = selectedLocationUrl!!,
                                modifier = screenModifier,
                                onPokemonSelected = { url -> selectedPokemonUrl = url }
                            )
                        }
                        else -> {
                            when (currentScreen) {
                                "menu" -> MainMenuScreen(
                                    modifier = screenModifier,
                                    onNavigate = { screen -> currentScreen = screen }
                                )
                                "pokemon" -> ListingScreen(
                                    resourceType = "pokemon",
                                    searchHint = "Search by name or Pokédex ID...",
                                    modifier = screenModifier,
                                    onResourceSelected = { url -> selectedPokemonUrl = url }
                                )
                                "location" -> ListingScreen(
                                    resourceType = currentScreen,
                                    searchHint = "Search for a location...",
                                    modifier = screenModifier,
                                    onResourceSelected = { url -> selectedLocationUrl = url }
                                )
                                "type" -> ListingScreen(
                                    resourceType = currentScreen,
                                    searchHint = "Search for a type...",
                                    modifier = screenModifier,
                                    onResourceSelected = { url -> selectedTypeUrl = url }
                                )
                                "ability", "item", "move" -> ListingScreen(
                                    resourceType = currentScreen,
                                    searchHint = "Search for a(n) $currentScreen...",
                                    modifier = screenModifier,
                                    onResourceSelected = { /* Do nothing for now */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// Main Menu Composable with all options restored
@Composable
fun MainMenuScreen(modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Pokedex Menu", modifier = Modifier.padding(bottom = 24.dp), fontSize = 32.sp)
        MenuCard(title = "Pokémon", onClick = { onNavigate("pokemon") })
        MenuCard(title = "Types", onClick = { onNavigate("type") })
        MenuCard(title = "Abilities", onClick = { onNavigate("ability") })
        MenuCard(title = "Locations", onClick = { onNavigate("location") })
        MenuCard(title = "Moves", onClick = { onNavigate("move") })
    }
}

@Composable
fun MenuCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.hsv(277f,1f, 0.5f))
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(16.dp),
            color = Color.White
        )
    }
}

// LOCATION data
@Composable
fun LocationInfoCard(locationName: String, regionName: String?, pokemonCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = locationName.replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (regionName != null) {
                Text(
                    text = "Region: ${regionName.replaceFirstChar { it.titlecase() }}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = "Pokémon Species: $pokemonCount",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun LocationDetailScreen(locationUrl: String, modifier: Modifier = Modifier, onPokemonSelected: (String) -> Unit) {
    var pokemonList by remember { mutableStateOf<List<ApiResource>>(emptyList()) }
    var locationDetails by remember { mutableStateOf<LocationDetails?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val json = Json { ignoreUnknownKeys = true }

    LaunchedEffect(locationUrl) {
        isLoading = true
        val client = AsyncHttpClient()

        // Step 1: Fetch the Location to get the Location-Area URL
        client.get(locationUrl, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, jsonResponse: JSON) {
                locationDetails = json.decodeFromString<LocationDetails>(jsonResponse.jsonObject.toString())
                val areaUrl = locationDetails?.areas?.firstOrNull()?.url

                if (areaUrl != null) {
                    // Step 2: Fetch the Location-Area using the URL from Step 1
                    client.get(areaUrl, object : JsonHttpResponseHandler() {
                        override fun onSuccess(statusCode: Int, headers: Headers, jsonResponse: JSON) {
                            try {
                                val locationArea = json.decodeFromString<LocationArea>(jsonResponse.jsonObject.toString())
                                val uniquePokemon = locationArea.pokemon_encounters.map { it.pokemon }.toSet().toList()
                                pokemonList = uniquePokemon
                            } catch (e: Exception) {
                                // Handle parsing error for the second call
                            } finally {
                                isLoading = false
                            }
                        }

                        override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                            isLoading = false
                        }
                    })
                } else {
                    isLoading = false // No areas found for this location
                }
            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                isLoading = false // Failed to fetch initial location details
            }
        })
    }


    if (isLoading) {
        Text("Loading Pokémon...", modifier = modifier.padding(16.dp))
    } else if (pokemonList.isEmpty()) {
        Text("No Pokémon found in this area.", modifier = modifier.padding(16.dp))
    } else {
        LazyColumn(modifier = modifier) {
            item {
                locationDetails?.let {
                    LocationInfoCard(
                        locationName = it.areas.first().name, // Use area name for more specific title
                        regionName = it.region?.name,
                        pokemonCount = pokemonList.size
                    )
                }
            }
            items(pokemonList) { pokemon ->
                // Replace the old ListItem with this Button
                Button(
                    onClick = { onPokemonSelected(pokemon.url) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    // Use transparent background to mimic a list item
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    // This Row will arrange the sprite and the name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pokemonId = pokemon.url.split("/").dropLast(1).last()
                        val spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"

                        // Sprite
                        AsyncImage(
                            model = spriteUrl,
                            contentDescription = "${pokemon.name} sprite",
                            modifier = Modifier.size(56.dp) // Adjust size as needed
                        )

                        // Pokémon Name
                        Text(
                            text = pokemon.name.replaceFirstChar { it.titlecase() },
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypeDetailScreen(typeUrl: String, modifier: Modifier = Modifier, onPokemonSelected: (String) -> Unit) {
    var pokemonList by remember { mutableStateOf<List<ApiResource>>(emptyList()) }
    var typeName by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val json = Json { ignoreUnknownKeys = true }

    LaunchedEffect(typeUrl) {
        isLoading = true
        val client = AsyncHttpClient()

        client.get(typeUrl, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, jsonResponse: JSON) {
                try {
                    val typeDetails = json.decodeFromString<PokemonTypeDetails>(jsonResponse.jsonObject.toString())
                    pokemonList = typeDetails.pokemon.map { it.pokemon }
                    typeName = typeDetails.name
                } catch (e: Exception) {
                    // Handle parsing error
                } finally {
                    isLoading = false
                }
            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                isLoading = false
            }
        })
    }

    if (isLoading) {
        Text("Loading Pokémon...", modifier = modifier.padding(16.dp))
    } else if (pokemonList.isEmpty()) {
        Text("No Pokémon found for this type.", modifier = modifier.padding(16.dp))
    } else {
        LazyColumn(modifier = modifier) {
            item {
                Text(
                    text = "Pokémon of type: ${typeName?.replaceFirstChar { it.titlecase() }}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(pokemonList) { pokemon ->
                Button(
                    onClick = { onPokemonSelected(pokemon.url) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pokemonId = pokemon.url.split("/").dropLast(1).last()
                        val spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"

                        AsyncImage(
                            model = spriteUrl,
                            contentDescription = "${pokemon.name} sprite",
                            modifier = Modifier.size(56.dp)
                        )

                        Text(
                            text = pokemon.name.replaceFirstChar { it.titlecase() },
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}



// Reusable Listing Screen for any resource type
@Composable
fun ListingScreen(
    resourceType: String,
    searchHint: String,
    modifier: Modifier = Modifier,
    onResourceSelected: (String) -> Unit
) {
    var allItems by remember { mutableStateOf<List<ApiResource>>(emptyList()) }
    var filteredItems by remember { mutableStateOf<List<ApiResource>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    val json = Json { ignoreUnknownKeys = true }

    // Fetch data from the API
    LaunchedEffect(resourceType) {
        val client = AsyncHttpClient()
        // Using a high limit to get most/all entries for a given type
        val url = "https://pokeapi.co/api/v2/$resourceType?limit=2000"

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, jsonResponse: JSON) {
                try {
                    val response = json.decodeFromString<ApiResponse>(jsonResponse.jsonObject.toString())
                    allItems = response.results
                    filteredItems = response.results
                } catch (e: Exception) {
                    // Handle parsing error if needed
                }
            }
            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                // Handle fetch error if needed
            }
        })
    }

    // Filter logic that works for all resource types
    LaunchedEffect(searchQuery, allItems) {
        filteredItems = if (searchQuery.isBlank()) {
            allItems
        } else {
            allItems.filter { item ->
                val nameMatch = item.name.contains(searchQuery, ignoreCase = true)
                // Only search by ID if the resource is pokemon
                if (resourceType == "pokemon") {
                    val id = item.url.split("/").dropLast(1).last()
                    val idMatch = id.contains(searchQuery)
                    nameMatch || idMatch
                } else {
                    nameMatch
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(searchHint) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredItems) { item ->
                val id = item.url.split("/").dropLast(1).last()
                // Replace ListItem with a styled Button
                Button(
                    onClick = { onResourceSelected(item.url) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant, // Use the same gray
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant  // And same content color
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.name.replaceFirstChar { it.titlecase() },
                            modifier = Modifier.weight(1f) // Text takes up available space
                        )
                        // Show the ID only for Pokémon
                        if (resourceType == "pokemon") {
                            Text(text = "ID: $id")
                        }
                    }
                }
            }
        }
    }
}
