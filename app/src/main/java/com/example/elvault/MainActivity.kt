package com.example.elvault

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.elvault.data.enums.VaultCategory
import com.example.elvault.data.models.VaultItem
import com.example.elvault.ui.theme.ElVaultTheme
import kotlinx.coroutines.delay

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
fun VaultHomeScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<VaultCategory?>(null) }

    var showPreserveDialog by remember { mutableStateOf(false) }

    // Sample data with poetic descriptions
    val vaultItems = remember {
        listOf(
            VaultItem(
                1,
                "Google Account",
                "example@gmail.com",
                Icons.Default.Lock,
                VaultCategory.PASSWORD
            ),
            VaultItem(2, "Bank Card", "•••• 4532", Icons.Default.DateRange, VaultCategory.IMAGE),
            VaultItem(
                3,
                "Secure Notes",
                "Personal thoughts",
                Icons.Default.Edit,
                VaultCategory.NOTE
            ),
            VaultItem(4, "ID Document", "National ID", Icons.Default.Face, VaultCategory.DOCUMENT),
            VaultItem(
                5,
                "Facebook",
                "user@facebook.com",
                Icons.Default.Lock,
                VaultCategory.PASSWORD
            ),
            VaultItem(
                6,
                "Amazon",
                "shopping@email.com",
                Icons.Default.Lock,
                VaultCategory.PASSWORD
            ),
        )
    }

    val filteredItems = vaultItems.filter { item ->
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                item.subtitle.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == null || item.category == selectedCategory
        matchesSearch && matchesCategory
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
                    showPreserveDialog= true
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

            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it }
            )

            Spacer(Modifier.height(16.dp))

            StatsCard(totalItems = vaultItems.size)

            Spacer(Modifier.height(20.dp))

            // Items List Header
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

            if (filteredItems.isEmpty()) {
                EmptyState(searchQuery = searchQuery)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems) { item ->
                        VaultItemCard(item = item)
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
            onConfirm = { newItem ->
                // Handle saving the new item
                showPreserveDialog = false
                // TODO: Add newItem to your vault list
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
fun CategoryFilterRow(
    selectedCategory: VaultCategory?,
    onCategorySelected: (VaultCategory?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text("All Realms") },
            leadingIcon = {
                Icon(Icons.Default.Face, null, Modifier.size(18.dp))
            }
        )

        FilterChip(
            selected = selectedCategory == VaultCategory.PASSWORD,
            onClick = { onCategorySelected(VaultCategory.PASSWORD) },
            label = { Text("Whispers") },
            leadingIcon = {
                Icon(Icons.Default.Lock, null, Modifier.size(18.dp))
            }
        )

        FilterChip(
            selected = selectedCategory == VaultCategory.IMAGE,
            onClick = { onCategorySelected(VaultCategory.IMAGE) },
            label = { Text("Treasures") },
            leadingIcon = {
                Icon(Icons.Default.Lock, null, Modifier.size(18.dp))
            }
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
fun VaultItemCard(item: VaultItem) {
    Card(
        onClick = { /* Open item details */ },
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when (item.category) {
                            VaultCategory.PASSWORD -> MaterialTheme.colorScheme.primaryContainer
                            VaultCategory.IMAGE -> MaterialTheme.colorScheme.secondaryContainer
                            VaultCategory.NOTE -> MaterialTheme.colorScheme.tertiaryContainer
                            VaultCategory.DOCUMENT -> MaterialTheme.colorScheme.errorContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = when (item.category) {
                        VaultCategory.PASSWORD -> MaterialTheme.colorScheme.onPrimaryContainer
                        VaultCategory.IMAGE -> MaterialTheme.colorScheme.onSecondaryContainer
                        VaultCategory.NOTE -> MaterialTheme.colorScheme.onTertiaryContainer
                        VaultCategory.DOCUMENT -> MaterialTheme.colorScheme.onErrorContainer
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.category.poeticName,
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = { /* Show options */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
                text = "waiting to guard",
                fontSize = 17.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "what you hold dear.",
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

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Perhaps it's elsewhere,",
                fontSize = 17.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "or perhaps it never was.",
                fontSize = 17.sp,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Try seeking with different words",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun PreserveDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (VaultItem) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(VaultCategory.PASSWORD) }
    var title by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }

    // Category-specific fields
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    var noteContent by remember { mutableStateOf("") }

    var documentType by remember { mutableStateOf("") }
    var documentNumber by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column {
                Text(
                    "Preserve a Memory",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
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
                // Category Selection
                Text(
                    "Type of Memory",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        category = VaultCategory.PASSWORD,
                        selectedCategory = selectedCategory,
                        onClick = { selectedCategory = VaultCategory.PASSWORD }
                    )
                    CategoryChip(
                        category = VaultCategory.IMAGE,
                        selectedCategory = selectedCategory,
                        onClick = { selectedCategory = VaultCategory.IMAGE }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        category = VaultCategory.NOTE,
                        selectedCategory = selectedCategory,
                        onClick = { selectedCategory = VaultCategory.NOTE }
                    )
                    CategoryChip(
                        category = VaultCategory.DOCUMENT,
                        selectedCategory = selectedCategory,
                        onClick = { selectedCategory = VaultCategory.DOCUMENT }
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Common Fields
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("Give it a name...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category-specific fields
                when (selectedCategory) {
                    VaultCategory.PASSWORD -> {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username/Email") },
                            placeholder = { Text("user@example.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            placeholder = { Text("Your secret key...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.Default.Face
                                        else Icons.Default.Person,
                                        "Toggle password visibility"
                                    )
                                }
                            }
                        )

                        OutlinedTextField(
                            value = website,
                            onValueChange = { website = it },
                            label = { Text("Website (Optional)") },
                            placeholder = { Text("https://example.com") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Lock, null) }
                        )
                    }

                    VaultCategory.IMAGE -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Select an Image",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )

                            if (imageUri == null) {
                                // Empty state — pick image
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
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Pick Image",
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "Tap to choose from gallery",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                // Image preview
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

                            OutlinedTextField(
                                value = subtitle,
                                onValueChange = { subtitle = it },
                                label = { Text("Description (Optional)") },
                                placeholder = { Text("Describe this image…") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2
                            )
                        }
                    }


                    VaultCategory.NOTE -> {
                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            label = { Text("Note Content") },
                            placeholder = { Text("Write your thoughts here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            maxLines = 6,
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                    }

                    VaultCategory.DOCUMENT -> {
                        OutlinedTextField(
                            value = documentType,
                            onValueChange = { documentType = it },
                            label = { Text("Document Type") },
                            placeholder = { Text("Passport, ID, License...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Lock, null) }
                        )

                        OutlinedTextField(
                            value = documentNumber,
                            onValueChange = { documentNumber = it },
                            label = { Text("Document Number") },
                            placeholder = { Text("A12345678") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Lock, null) }
                        )
                    }
                }

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Additional details...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    leadingIcon = { Icon(Icons.Default.Lock, null) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val icon = when (selectedCategory) {
                        VaultCategory.PASSWORD -> Icons.Default.Lock
                        VaultCategory.IMAGE -> Icons.Default.Lock
                        VaultCategory.NOTE -> Icons.Default.Edit
                        VaultCategory.DOCUMENT -> Icons.Default.Face
                    }

                    val displaySubtitle = when (selectedCategory) {
                        VaultCategory.PASSWORD -> username.ifEmpty { subtitle }
                        VaultCategory.IMAGE -> "•••• ${cardNumber.takeLast(4)}"
                        VaultCategory.NOTE -> noteContent.take(30) + "..."
                        VaultCategory.DOCUMENT -> documentNumber.ifEmpty { subtitle }
                    }

                    val newItem = VaultItem(
                        id = System.currentTimeMillis().toInt(),
                        title = title,
                        subtitle = displaySubtitle,
                        icon = icon,
                        category = selectedCategory
                    )
                    onConfirm(newItem)
                },
                enabled = title.isNotEmpty()
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

@Composable
fun CategoryChip(
    category: VaultCategory,
    selectedCategory: VaultCategory,
    onClick: () -> Unit
) {
    val isSelected = category == selectedCategory

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = when (category) {
                    VaultCategory.PASSWORD -> "Password"
                    VaultCategory.IMAGE -> "Image"
                    VaultCategory.NOTE -> "Note"
                    VaultCategory.DOCUMENT -> "Document"
                },
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = when (category) {
                    VaultCategory.PASSWORD -> Icons.Default.Lock
                    VaultCategory.IMAGE -> Icons.Default.Lock
                    VaultCategory.NOTE -> Icons.Default.Edit
                    VaultCategory.DOCUMENT -> Icons.Default.Face
                },
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}
