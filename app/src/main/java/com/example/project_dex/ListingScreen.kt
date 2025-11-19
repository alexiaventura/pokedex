package com.example.project_dex
//.
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.example.project_dex.network.ApiResource
import com.example.project_dex.network.PagedResponse
import kotlinx.serialization.json.Json
import okhttp3.Headers
import java.util.Locale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

// --- VIEWMODEL and UI STATE ---
// Placed inside the same file as the UI that uses it.

sealed interface ListUiState {
    data class Success(val items: List<ApiResource>) : ListUiState
    object Error : ListUiState
    object Loading : ListUiState
}

class ListingViewModel : ViewModel() {
    var uiState: ListUiState by mutableStateOf(ListUiState.Loading)
        private set

    // This is the JSON parser from the kotlinx.serialization library
    private val jsonParser = Json { ignoreUnknownKeys = true }

    fun fetchResourceList(resourceType: String) {
        val client = AsyncHttpClient()
        val url = "https://pokeapi.co/api/v2/${resourceType}?limit=1000"

        uiState = ListUiState.Loading

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                try {
                    // Get the raw JSON as a string
                    val jsonString = json.jsonObject.toString()

                    // Use the kotlinx.serialization parser to decode the string
                    val pagedResponse = jsonParser.decodeFromString<PagedResponse>(jsonString)

                    // Update the UI state with the successfully parsed data
                    uiState = ListUiState.Success(pagedResponse.results)

                } catch (e: Exception) {
                    // If parsing fails, set the UI state to Error
                    uiState = ListUiState.Error
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                // If the network request itself fails, set the error state
                uiState = ListUiState.Error
            }
        })
    }
}


// --- UI COMPOSABLE ---
// This is the actual screen.
@Composable
fun ListingScreen(
    resourceType: String, // e.g., "pokemon", "move"
    searchHint: String,
    modifier: Modifier = Modifier,
    onResourceSelected: (String) -> Unit,
    listingViewModel: ListingViewModel = viewModel()
) {
    // Trigger the data fetch when the screen is first composed.
    // `LaunchedEffect` ensures it only runs once when `resourceType` is first seen.
    LaunchedEffect(resourceType) {
        listingViewModel.fetchResourceList(resourceType)
    }

    var searchQuery by remember { mutableStateOf("") }
    val uiState = listingViewModel.uiState

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(searchHint) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // 2. HANDLE UI STATES (Loading, Error, Success)
        when (uiState) {
            is ListUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ListUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Failed to load data. Please try again.")
                }
            }
            is ListUiState.Success -> {
                // 3. DEFINE filteredItems inside the Success state
                val filteredItems = uiState.items.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredItems) { item ->
                        val id = item.url.split("/").dropLast(1).last()
                        Button(
                            onClick = { onResourceSelected(item.url) },
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
    }
}