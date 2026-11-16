package com.example.myapp.ui.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.ui.common.AvatarImage
import java.io.File

/**
 * Экран редактирования профиля.
 *
 * При входе запрашиваются разрешения (камера + доступ к изображениям).
 * Если пользователь отказал — выходим с экрана. По нажатию на аватарку
 * предлагается выбрать источник: галерея или камера.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: ProfileEditViewModel = viewModel(factory = ProfileEditViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val requiredPermissions = remember { requiredPermissions() }
    var permissionDenied by remember { mutableStateOf(false) }
    var showSourceDialog by remember { mutableStateOf(false) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        fun isGranted(permission: String): Boolean = result[permission]
            ?: (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)

        val cameraGranted = isGranted(Manifest.permission.CAMERA)
        val mediaGranted = hasMediaAccess(::isGranted)
        if (!cameraGranted || !mediaGranted) permissionDenied = true
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.onAvatarSelected(uri.toString())
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) viewModel.onAvatarSelected(uri.toString())
        pendingCameraUri = null
    }

    // Запрос разрешений при входе на экран.
    LaunchedEffect(Unit) {
        fun isGranted(permission: String): Boolean =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        val cameraGranted = isGranted(Manifest.permission.CAMERA)
        val mediaGranted = hasMediaAccess(::isGranted)
        if (!cameraGranted || !mediaGranted) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        }
    }

    // Отказ в доступе — покидаем экран (требование практики).
    LaunchedEffect(permissionDenied) {
        if (permissionDenied) {
            Toast.makeText(
                context,
                "Без разрешений нельзя изменить аватарку",
                Toast.LENGTH_LONG,
            ).show()
            onBack()
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onDone()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование профиля") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AvatarImage(
                            uri = uiState.avatarUri,
                            size = 140.dp,
                            modifier = Modifier.clickable { showSourceDialog = true },
                        )
                        if (uiState.isAvatarProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }

                    Text(
                        text = "Нажмите на фото, чтобы выбрать галерею или камеру",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )

                    OutlinedTextField(
                        value = uiState.fullName,
                        onValueChange = viewModel::updateFullName,
                        label = { Text("ФИО") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = uiState.position,
                        onValueChange = viewModel::updatePosition,
                        label = { Text("Должность") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = uiState.resumeUrl,
                        onValueChange = viewModel::updateResumeUrl,
                        label = { Text("Ссылка на резюме (PDF)") },
                        placeholder = { Text("https://example.com/resume.pdf") },
                        supportingText = { Text("Документ скачается по кнопке «Резюме»") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Button(
                        onClick = viewModel::save,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text("Готово")
                        }
                    }
                }
            }
        }
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Новая аватарка") },
            text = { Text("Откуда взять изображение?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSourceDialog = false
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                ) {
                    Text("Галерея")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSourceDialog = false
                        val uri = createCameraOutputUri(context)
                        pendingCameraUri = uri
                        runCatching { cameraLauncher.launch(uri) }
                            .onFailure {
                                Toast.makeText(context, "Камера недоступна", Toast.LENGTH_LONG).show()
                            }
                    },
                ) {
                    Text("Камера")
                }
            },
        )
    }
}

/** Разрешения, которые запрашиваем при входе на экран редактирования. */
private fun requiredPermissions(): List<String> = buildList {
    add(Manifest.permission.CAMERA)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.READ_MEDIA_IMAGES)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        }
    } else {
        add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

/** Доступ к изображениям: полный или частичный (Android 14+). */
private fun hasMediaAccess(isGranted: (String) -> Boolean): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        isGranted(Manifest.permission.READ_MEDIA_IMAGES) ||
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                isGranted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED))
    } else {
        isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

/** Файл во внутреннем хранилище + content:// URI для камеры (через FileProvider). */
private fun createCameraOutputUri(context: Context): Uri {
    val directory = File(context.filesDir, "avatars").apply { mkdirs() }
    val file = File(directory, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
