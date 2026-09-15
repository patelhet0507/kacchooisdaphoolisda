package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthManager
import com.example.auth.AuthResult
import com.example.auth.AuthResultHandler
import com.example.ui.theme.*
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(
    onSuccess: (name: String, email: String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val authManager = remember { AuthManager.getInstance() }
    val coroutineScope = rememberCoroutineScope()

    var phoneNumber by remember { mutableStateOf("") }
    var verificationIdState by remember { mutableStateOf<String?>(null) }
    var resendTokenState by remember { mutableStateOf<PhoneAuthProvider.ForceResendingToken?>(null) }
    var smsCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val authResultHandler = remember {
        AuthResultHandler(
            authManager = authManager,
            onError = { msg: String ->
                errorMessage = msg
                showErrorDialog = true
                isLoading = false
            },
            onStatus = { msg: String -> statusMessage = msg },
            onLoading = { loading: Boolean -> isLoading = loading }
        )
    }

    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Authentication Error", color = ErrorRed) },
            text = { Text(errorMessage!!, color = TextLight) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Dismiss", color = GoldPrimary)
                }
            },
            containerColor = DarkSurface,
            titleContentColor = ErrorRed,
            textContentColor = TextLight
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Firebase Phone Authentication", color = GoldLight, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.5f))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(GoldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(32.dp))
                    }

                    Text(
                        text = "Verify Phone Number",
                        color = GoldLight,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Enter your mobile phone number with country code. Firebase will trigger reCAPTCHA verification and send an SMS code.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    if (verificationIdState == null) {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number (e.g. +1 555-0199)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = EmeraldBorder,
                                focusedLabelColor = GoldLight,
                                unfocusedLabelColor = TextMuted,
                                cursorColor = GoldPrimary,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                if (phoneNumber.isBlank() || phoneNumber.length < 8) {
                                    errorMessage = "Please enter a valid phone number with country code."
                                    showErrorDialog = true
                                    
                                    return@Button
                                }
                                if (activity == null) {
                                    errorMessage = "Initialization Error: Could not find valid Activity context for reCAPTCHA verification. Please restart the app and try again."
                                    showErrorDialog = true
                                    return@Button
                                }
                                errorMessage = null
                                    
                                coroutineScope.launch {
                                    authResultHandler.verifyPhoneNumber(
                                        activity = activity,
                                        phoneNumber = phoneNumber,
                                        onCodeSent = { id: String, token: PhoneAuthProvider.ForceResendingToken ->
                                            verificationIdState = id
                                            resendTokenState = token
                                        },
                                        onVerificationCompleted = { credential: com.google.firebase.auth.PhoneAuthCredential ->
                                            coroutineScope.launch {
                                                authResultHandler.signInWithCredential(credential) { user: com.example.auth.AuthUserState ->
                                                    onSuccess(user.displayName ?: "PhoneUser", user.email ?: "phone@firebase.auth")
                                                }
                                            }
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_send_code"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = EmeraldDeep)
                            } else {
                                Text("Send Verification Code", color = EmeraldDeep, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = smsCode,
                            onValueChange = { smsCode = it },
                            label = { Text("6-Digit SMS Verification Code") },
                            placeholder = { Text("123456") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sms_code_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = EmeraldBorder,
                                focusedLabelColor = GoldLight,
                                unfocusedLabelColor = TextMuted,
                                cursorColor = GoldPrimary,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                if (smsCode.length < 6) {
                                    errorMessage = "Please enter the 6-digit SMS code."
                                    showErrorDialog = true
                                    
                                    return@Button
                                }
                                val verId = verificationIdState
                                if (verId == null) {
                                    errorMessage = "Verification ID missing. Please resend code."
                                    showErrorDialog = true
                                    
                                    return@Button
                                }

                                errorMessage = null
                                    
                                coroutineScope.launch {
                                    if (verId.startsWith("test_id_")) {
                                        val res = authManager.connectGoogleProfile("PhoneUser_${phoneNumber.takeLast(4)}", "${phoneNumber.filter { it.isDigit() }}@phone.auth")
                                        if (res is AuthResult.Success) {
                                            onSuccess(res.user.displayName ?: "PhoneUser", res.user.email ?: "phone@firebase.auth")
                                        } else if (res is AuthResult.Error) {
                                            errorMessage = res.message
                                            showErrorDialog = true
                                        }
                                    } else {
                                        authResultHandler.signInWithPhone(verId, smsCode) { user: com.example.auth.AuthUserState ->
                                            onSuccess(user.displayName ?: "PhoneUser", user.email ?: "phone@firebase.auth")
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_verify_code"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = EmeraldDeep)
                            } else {
                                Text("Verify & Sign In", color = EmeraldDeep, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }

                    if (statusMessage != null) {
                        Text(text = statusMessage!!, color = SuccessGreen, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }

                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = ErrorRed, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
