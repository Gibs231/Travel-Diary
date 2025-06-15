package com.gibraltar0123.traveldiary.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.gibraltar0123.traveldiary.BuildConfig
import com.gibraltar0123.traveldiary.R
import com.gibraltar0123.traveldiary.model.Travel
import com.gibraltar0123.traveldiary.model.User
import com.gibraltar0123.traveldiary.network.ApiStatus
import com.gibraltar0123.traveldiary.network.UserDataStore
import com.gibraltar0123.traveldiary.ui.theme.TravelDiaryTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())

    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage

    var showDialogHapus by remember { mutableStateOf(false) }
    var travelToDelete by remember { mutableStateOf<Travel?>(null) }

    var showDialog by remember { mutableStateOf(false) }
    var showTravelDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showPreviewDialog by remember { mutableStateOf(false) }

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    var travelToEdit by remember { mutableStateOf<Travel?>(null) }
    var travelToPreview by remember { mutableStateOf<Travel?>(null) }
    var isEditMode by remember { mutableStateOf(false) }


    fun resetEditState() {
        bitmap = null
        travelToEdit = null
        isEditMode = false
        showEditDialog = false
        showImageSourceDialog = false
    }


    fun resetAddState() {
        bitmap = null
        showTravelDialog = false
        showImageSourceDialog = false
    }


    val cameraLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        val croppedBitmap = getCroppedImage(context.contentResolver, result)
        if (croppedBitmap != null) {
            bitmap = croppedBitmap
            if (isEditMode && travelToEdit != null) {
                showEditDialog = true
            } else {
                showTravelDialog = true
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        val croppedBitmap = getCroppedImage(context.contentResolver, result)
        if (croppedBitmap != null) {
            bitmap = croppedBitmap
            if (isEditMode && travelToEdit != null) {
                showEditDialog = true
            } else {
                showTravelDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = stringResource(id = R.string.app_name),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (user.email.isNotEmpty()) {
                                Text(
                                    text = "Welcome back, ${user.name}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                actions = {
                    if (user.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch {
                                signIn(context, dataStore)
                            }
                        } else {
                            showDialog = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.account_circle),
                            contentDescription = stringResource(R.string.profil),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    isEditMode = false
                    showImageSourceDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.shadow(8.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.tambah_travel)
                )
            }
        }
    ) { innerPadding ->
        ScreenContent(
            viewModel = viewModel,
            userId = user.email,
            modifier = Modifier.padding(innerPadding),
            onDeleteClick = { travel ->
                travelToDelete = travel
                showDialogHapus = true
            },
            onEditClick = { travel ->
                travelToEdit = travel
                isEditMode = true
                showImageSourceDialog = true
            },
            onCardClick = { travel ->
                travelToPreview = travel
                showPreviewDialog = true
            }
        )

        if (showDialog) {
            ProfilDialog(
                user = user,
                onDismissRequest = { showDialog = false }
            ) {
                CoroutineScope(Dispatchers.IO).launch {
                    signOut(context, dataStore)
                }
                showDialog = false
            }
        }

        if (showImageSourceDialog) {
            ImageSourceDialog(
                onDismissRequest = {
                    if (isEditMode) {
                        resetEditState()
                    } else {
                        resetAddState()
                    }
                },
                onCameraSelected = {
                    showImageSourceDialog = false
                    val options = CropImageContractOptions(
                        null,
                        CropImageOptions(
                            imageSourceIncludeGallery = false,
                            imageSourceIncludeCamera = true,
                            fixAspectRatio = true
                        )
                    )
                    cameraLauncher.launch(options)
                },
                onGallerySelected = {
                    showImageSourceDialog = false
                    val options = CropImageContractOptions(
                        null,
                        CropImageOptions(
                            imageSourceIncludeGallery = true,
                            imageSourceIncludeCamera = false,
                            fixAspectRatio = true
                        )
                    )
                    galleryLauncher.launch(options)
                }
            )
        }

        if (showTravelDialog) {
            TravelDialog(
                bitmap = bitmap,
                onDismissRequest = {
                    resetAddState()
                }
            ) { title, description ->
                bitmap?.let {
                    viewModel.saveData(user.email, title, description, it)
                }
                resetAddState()
            }
        }


        if (showEditDialog && travelToEdit != null) {
            EditTravelDialog(
                travel = travelToEdit!!,
                bitmap = bitmap,
                onDismissRequest = {
                    resetEditState()
                }
            ) { title, description ->
                bitmap?.let {
                    viewModel.updateData(user.email, travelToEdit!!.id, title, description, it)
                }
                resetEditState()
            }
        }

        if (showDialogHapus) {
            DialogHapus(
                onDismissRequest = {
                    showDialogHapus = false
                    travelToDelete = null
                },
                onConfirm = {
                    travelToDelete?.let { travel ->
                        viewModel.deleteData(user.email, travel.id)
                    }
                    showDialogHapus = false
                    travelToDelete = null
                }
            )
        }

        // Travel Preview Dialog
        if (showPreviewDialog && travelToPreview != null) {
            TravelPreviewDialog(
                travel = travelToPreview!!,
                onDismissRequest = {
                    showPreviewDialog = false
                    travelToPreview = null
                }
            )
        }

        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
}


@Composable
fun ScreenContent(
    viewModel: MainViewModel,
    userId: String,
    modifier: Modifier = Modifier,
    onDeleteClick: (Travel) -> Unit,
    onEditClick: (Travel) -> Unit,
    onCardClick: (Travel) -> Unit
) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.retrieveData(userId)
        }
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        )

        when {
            userId.isEmpty() -> {
                EmptyLoginState()
            }

            status == ApiStatus.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading your adventures...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            status == ApiStatus.SUCCESS -> {
                if (data.isEmpty()) {
                    EmptyTravelState()
                } else {
                    LazyVerticalStaggeredGrid(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        items(data) { travel ->
                            TravelCard(
                                travel = travel,
                                onDeleteClick = onDeleteClick,
                                onEditClick = onEditClick,
                                onCardClick = onCardClick
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            status == ApiStatus.FAILED -> {
                ErrorState { viewModel.retrieveData(userId) }
            }
        }
    }
}

@Composable
fun EmptyLoginState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Start Your Travel Journey",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.login_required),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.tap_profile_to_login),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun EmptyTravelState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.baseline_camera_alt_24), // Make sure you have this icon
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Adventures Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Capture your first travel memory\nby tapping the + button below",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun ErrorState(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Oops! Connection Error",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.koneksi_error),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(text = stringResource(id = R.string.coba_lagi))
        }
    }
}

@Composable
fun TravelCard(
    travel: Travel,
    onDeleteClick: (Travel) -> Unit,
    onEditClick: (Travel) -> Unit,
    onCardClick: (Travel) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick(travel) }
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(travel.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.gambar, travel.title),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                placeholder = painterResource(R.drawable.loading_img),
                error = painterResource(R.drawable.baseline_broken_image_24)
            )


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 100f
                        ),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { onEditClick(travel) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_24),
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { onDeleteClick(travel) },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                if (travel.completed) {
                    Text(
                        text = stringResource(R.string.completed),
                        fontSize = 10.sp,
                        color = Color.Green,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                Color.Green.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = travel.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }


        if (!travel.description.isNullOrBlank()) {
            Text(
                text = travel.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun TravelPreviewDialog(
    travel: Travel,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {

                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }


                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(travel.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = travel.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    placeholder = painterResource(R.drawable.loading_img),
                    error = painterResource(R.drawable.baseline_broken_image_24)
                )


                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Status badge
                    if (travel.completed) {
                        Text(
                            text = stringResource(R.string.completed),
                            fontSize = 12.sp,
                            color = Color.Green,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(
                                    Color.Green.copy(alpha = 0.2f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Title
                    Text(
                        text = travel.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    if (!travel.description.isNullOrBlank()) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = travel.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        )
                    } else {
                        Text(
                            text = "No description available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

private suspend fun signIn(context: Context, dataStore: UserDataStore) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(result: GetCredentialResponse, dataStore: UserDataStore) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(User(nama, email, photoUrl))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    } else {
        Log.e("SIGN-IN", "Unrecognized credential type.")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    } catch (e: ClearCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }

    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenPreview() {
    TravelDiaryTheme {
        MainScreen()
    }
}