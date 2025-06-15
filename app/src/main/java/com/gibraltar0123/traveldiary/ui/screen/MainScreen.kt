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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    var travelToEdit by remember { mutableStateOf<Travel?>(null) }
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

    // Launcher untuk galeri dengan crop
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
                    Text(text = stringResource(id = R.string.app_name))
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
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
            FloatingActionButton(onClick = {
                isEditMode = false
                showImageSourceDialog = true
            }) {
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

        // Dialog untuk edit travel
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
    onEditClick: (Travel) -> Unit
) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            viewModel.retrieveData(userId)
        }
    }

    when {
        userId.isEmpty() -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.login_required),
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = stringResource(id = R.string.tap_profile_to_login),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        status == ApiStatus.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        status == ApiStatus.SUCCESS -> {
            LazyVerticalGrid(
                modifier = modifier
                    .fillMaxSize()
                    .padding(4.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(data) { travel ->
                    ListItem(
                        travel = travel,
                        onDeleteClick = onDeleteClick,
                        onEditClick = onEditClick
                    )
                }
            }
        }

        status == ApiStatus.FAILED -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.koneksi_error))
                Button(
                    onClick = { viewModel.retrieveData(userId) },
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.coba_lagi))
                }
            }
        }
    }
}

@Composable
fun ListItem(
    travel: Travel,
    onDeleteClick: (Travel) -> Unit,
    onEditClick: (Travel) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(travel.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.gambar, travel.title),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            placeholder = painterResource(R.drawable.loading_img),
            error = painterResource(R.drawable.baseline_broken_image_24)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = travel.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!travel.description.isNullOrBlank()) {
                    Text(
                        text = travel.description,
                        fontStyle = FontStyle.Italic,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (travel.completed) {
                        Text(
                            text = stringResource(R.string.completed),
                            fontSize = 10.sp,
                            color = Color.Green,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }


                    Row {

                        IconButton(
                            onClick = {
                                onEditClick(travel)
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_edit_24),
                                contentDescription = "Edit",
                                tint = Color.White
                            )
                        }

                        // Delete button
                        IconButton(
                            onClick = {
                                onDeleteClick(travel)
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_delete_24),
                                contentDescription = "Hapus",
                                tint = Color.White
                            )
                        }
                    }
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