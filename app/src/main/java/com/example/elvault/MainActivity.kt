package com.example.elvault

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.elvault.data.local.entity.VaultEntity
import com.example.elvault.data.enums.VaultMediaType
import com.example.elvault.ui.theme.ElVaultTheme
import com.example.elvault.ui.viewmodel.VaultViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ElVaultTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onTimeout = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            VaultHomeScreen()
                        }
                    }
                }
            }
        }
    }
}

// Helper function to extract video thumbnail
suspend fun getVideoThumbnail(context: Context, uri: Uri): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val bitmap = retriever.getFrameAtTime(0)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var scale by remember { mutableStateOf(0.5f) }
    var textAlpha by remember { mutableStateOf(0f) }

    val scaleAnim by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    val alphaAnim by animateFloatAsState(
        targetValue = textAlpha,
        animationSpec = tween(durationMillis = 800, delayMillis = 400),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        scale = 1f
        delay(300)
        textAlpha = 1f
        delay(1700)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.splash),
                contentDescription = "ElVault Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnim)
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "ElVault",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.scale(scaleAnim)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "A sanctuary for fragments",
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic,
                color = Color.White.copy(alpha = 0.9f * alphaAnim),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "of your soul",
                fontSize = 15.sp,
                fontStyle = FontStyle.Italic,
                color = Color.White.copy(alpha = 0.9f * alphaAnim),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultHomeScreen(viewModel: VaultViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMediaType by remember { mutableStateOf<VaultMediaType?>(null) }
    var showPreserveDialog by remember { mutableStateOf(false) }

    val allItems by viewModel.allVaultItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            showPreserveDialog = true
        }
    }

    val filteredItems = remember(allItems, searchQuery, selectedMediaType) {
        allItems.filter { item ->
            val matchesSearch = item.title?.contains(searchQuery, ignoreCase = true) == true ||
                    item.description?.contains(searchQuery, ignoreCase = true) == true
            val matchesType = selectedMediaType == null || item.mediaType == selectedMediaType
            matchesSearch && matchesType
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "ElVault",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Text(
                            "The Keeper of Silent Things",
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.READ_MEDIA_IMAGES,
                                android.Manifest.permission.READ_MEDIA_VIDEO
                            )
                        )
                    } else {
                        permissionLauncher.launch(
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        )
                    }
                },
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("Preserve a Memory") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "What are you seeking?"
            )

            Spacer(Modifier.height(16.dp))

            MediaTypeFilterRow(
                selectedMediaType = selectedMediaType,
                onMediaTypeSelected = { selectedMediaType = it }
            )

            Spacer(Modifier.height(16.dp))

            StatsCard(totalItems = allItems.size)

            Spacer(Modifier.height(20.dp))

            Text(
                text = if (filteredItems.isEmpty()) "The Silence" else "Your Collection",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (filteredItems.isNotEmpty()) {
                Text(
                    text = "${filteredItems.size} ${if (filteredItems.size == 1) "treasure" else "treasures"} await",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filteredItems.isEmpty()) {
                EmptyState(searchQuery = searchQuery)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems) { item ->
                        VaultEntityCard(
                            item = item,
                            onDelete = { viewModel.deleteVaultItem(item) }
                        )
                    }

                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showPreserveDialog) {
        PreserveDialog(
            onDismissRequest = { showPreserveDialog = false },
            onConfirm = { newEntity ->
                viewModel.insertVaultItem(newEntity)
                showPreserveDialog = false
            }
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search..."
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = {
            Text(
                text = placeholder,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaTypeFilterRow(
    selectedMediaType: VaultMediaType?,
    onMediaTypeSelected: (VaultMediaType?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedMediaType == null,
            onClick = { onMediaTypeSelected(null) },
            label = { Text("All") },
            leadingIcon = { Icon(Icons.Default.Face, null, Modifier.size(18.dp)) }
        )

        FilterChip(
            selected = selectedMediaType == VaultMediaType.IMAGE,
            onClick = { onMediaTypeSelected(VaultMediaType.IMAGE) },
            label = { Text("Images") },
            leadingIcon = { Icon(Icons.Default.Lock, null, Modifier.size(18.dp)) }
        )

        FilterChip(
            selected = selectedMediaType == VaultMediaType.VIDEO,
            onClick = { onMediaTypeSelected(VaultMediaType.VIDEO) },
            label = { Text("Videos") },
            leadingIcon = { Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp)) }
        )
    }
}

@Composable
fun StatsCard(totalItems: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Secrets Held",
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = totalItems.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (totalItems == 1) "fragment preserved" else "fragments preserved",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultEntityCard(item: VaultEntity, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var videoThumbnail by remember { mutableStateOf<Bitmap?>(null) }

    // Load video thumbnail if it's a video
    LaunchedEffect(item.uri, item.mediaType) {
        if (item.mediaType == VaultMediaType.VIDEO) {
            try {
                val uri = Uri.parse(item.uri)
                videoThumbnail = getVideoThumbnail(context, uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Card(
        onClick = { },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Preview based on media type
            when (item.mediaType) {
                VaultMediaType.IMAGE -> {
                    Box {
                        Image(
                            painter = rememberAsyncImagePainter(Uri.parse(item.uri)),
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                VaultMediaType.VIDEO -> {
                    Box {
                        if (videoThumbnail != null) {
                            Image(
                                bitmap = videoThumbnail!!.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        }
                        // Play icon overlay
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Video",
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.3f),
                                    CircleShape
                                )
                                .padding(12.dp),
                            tint = Color.White
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                when (item.mediaType) {
                                    VaultMediaType.AUDIO -> MaterialTheme.colorScheme.tertiaryContainer
                                    VaultMediaType.DOCUMENT -> MaterialTheme.colorScheme.errorContainer
                                    VaultMediaType.PASSWORD -> MaterialTheme.colorScheme.primaryContainer
                                    VaultMediaType.NOTE -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (item.mediaType) {
                                VaultMediaType.AUDIO -> Icons.Default.Refresh
                                VaultMediaType.DOCUMENT -> Icons.Default.Face
                                VaultMediaType.PASSWORD -> Icons.Default.Lock
                                VaultMediaType.NOTE -> Icons.Default.Edit
                                else -> Icons.Default.Lock
                            },
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title ?: "Untitled",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.description ?: "No description",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.mediaType.poeticName,
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Memory?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun EmptyState(searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (searchQuery.isEmpty()) Icons.Default.Lock else Icons.Default.ThumbUp,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )

        Spacer(Modifier.height(20.dp))

        if (searchQuery.isEmpty()) {
            Text(
                text = "The vault breathes in silence,",
                fontSize = 17.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "waiting to guard what you hold dear.",
                fontSize = 17.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Tap below to preserve your first memory",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "Nothing found in the silence.",
                fontSize = 17.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PreserveDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (VaultEntity) -> Unit
) {
    var selectedMediaType by remember { mutableStateOf(VaultMediaType.IMAGE) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var videoThumbnail by remember { mutableStateOf<Bitmap?>(null) }
    var audioUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            imageUri = it
        }
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            videoUri = it
        }
    }

    val audioPicker= rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ){uri ->
        uri?.let{
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION

                )
            }catch (e:Exception){
                e.printStackTrace()
            }
            audioUri = it
        }

    }

    LaunchedEffect(videoUri) {
        videoUri?.let { uri ->
            try {
                videoThumbnail = getVideoThumbnail(context, uri)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column {
                Text("Preserve a Memory", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text(
                    "What secrets shall we keep?",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Type", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VaultMediaType.entries.take(3).forEach { type ->
                        FilterChip(
                            selected = selectedMediaType == type,
                            onClick = { selectedMediaType = type },
                            label = { Text(type.name) }
                        )
                    }
                }

                if (selectedMediaType == VaultMediaType.IMAGE) {
                    if (imageUri == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text("Tap to choose image", fontSize = 12.sp)
                            }
                        }
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentScale = ContentScale.Crop
                        )
                    }
                } else if (selectedMediaType == VaultMediaType.VIDEO) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { videoPicker.launch("video/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (videoUri == null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    null,
                                    Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text("Tap to choose video", fontSize = 12.sp)
                            }
                        } else {
                            if (videoThumbnail != null) {
                                Image(
                                    bitmap = videoThumbnail!!.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                CircularProgressIndicator()
                            }
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Video",
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                    .padding(12.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val uri = when (selectedMediaType) {
                        VaultMediaType.IMAGE -> imageUri?.toString()
                        VaultMediaType.VIDEO -> videoUri?.toString()
                        else -> null
                    } ?: ""

                    if (uri.isNotEmpty() && title.isNotEmpty()) {
                        onConfirm(
                            VaultEntity(
                                uri = uri,
                                mediaType = selectedMediaType,
                                title = title,
                                description = description.ifEmpty { null }
                            )
                        )
                    }
                },
                enabled = title.isNotEmpty() &&
                        ((selectedMediaType == VaultMediaType.IMAGE && imageUri != null) ||
                                (selectedMediaType == VaultMediaType.VIDEO && videoUri != null))
            ) {
                Text("Preserve")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}