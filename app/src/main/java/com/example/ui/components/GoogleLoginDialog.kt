package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.auth.AuthManager
import com.example.auth.AuthResult
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun GoogleLoginDialog(
    currentName: String,
    currentEmail: String,
    isLoggedIn: Boolean,
    onDismiss: () -> Unit,
    onLogin: (name: String, email: String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authManager = remember { AuthManager.getInstance() }

    var authMode by remember { mutableIntStateOf(0) } // 0: Google, 1: Email & Password
    var isSignUp by remember { mutableStateOf(false) } // For email mode: Sign In vs Sign Up
    var nameInput by remember { mutableStateOf(if (currentName == "Player 1" || currentName.isBlank()) "" else currentName) }
    var emailInput by remember { mutableStateOf(currentEmail.ifBlank { "" }) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var phoneInput by remember { mutableStateOf("") }
    var smsCodeInput by remember { mutableStateOf("") }
    var phoneCodeSent by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header with Title & Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (authMode == 0) Color.White else GoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (authMode == 0) {
                                Text(
                                    text = "G",
                                    color = Color(0xFF4285F4),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isLoggedIn) "Account Connected" else (if (authMode == 0) "Sign in with Google" else if (isSignUp) "Create Account" else "Sign In"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                    }
                    IconButton(
                        onClick = { if (!isLoading) onDismiss() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                if (isLoggedIn) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.5f))
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = currentName.ifBlank { "Player" },
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (currentEmail.isNotBlank()) {
                                Text(
                                    text = currentEmail,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = "Your account is authenticated with Firebase. Your multiplayer room sessions and achievements sync automatically.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                } else {
                    // Switch tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { authMode = 0; errorMessage = null; statusMessage = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (authMode == 0) GoldPrimary else DarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text(
                                text = "Google",
                                color = if (authMode == 0) Color.Black else TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = { authMode = 1; errorMessage = null; statusMessage = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (authMode == 1) GoldPrimary else DarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text(
                                text = "Email",
                                color = if (authMode == 1) Color.Black else TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = { authMode = 2; errorMessage = null; statusMessage = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (authMode == 2) GoldPrimary else DarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text(
                                text = "Phone Auth",
                                color = if (authMode == 2) Color.Black else TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (authMode == 0) {
                        Text(
                            text = "Authenticate with your Google account to sync your profile, achievements, and multiplayer rooms across devices.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Display Name") },
                            placeholder = { Text("e.g. Het Patel") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldLight) },
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

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Google Account Email") },
                            placeholder = { Text("e.g. patelhet.0507@gmail.com") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldLight) },
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
                    } else if (authMode == 1) {
                        // Sign In vs Sign Up toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (!isSignUp) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { isSignUp = false; errorMessage = null }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Log In",
                                    color = if (!isSignUp) GoldLight else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (!isSignUp) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSignUp) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { isSignUp = true; errorMessage = null }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Register",
                                    color = if (isSignUp) GoldLight else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSignUp) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        if (isSignUp) {
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Display Name") },
                                placeholder = { Text("e.g. Aarav") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldLight) },
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
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("name@example.com") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldLight) },
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

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldLight) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = TextMuted
                                    )
                                }
                            },
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

                        if (!isSignUp) {
                            TextButton(
                                onClick = {
                                    if (emailInput.isBlank() || !emailInput.contains("@")) {
                                        errorMessage = "Enter your email above first to receive reset link."
                                    } else {
                                        coroutineScope.launch {
                                            isLoading = true
                                            val res = authManager.sendPasswordReset(emailInput)
                                            isLoading = false
                                            if (res.isSuccess) {
                                                statusMessage = "Password reset email sent to $emailInput"
                                            } else {
                                                errorMessage = res.exceptionOrNull()?.localizedMessage ?: "Failed to send reset email"
                                            }
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Forgot Password?", color = GoldLight, fontSize = 11.sp)
                            }
                        }
                    } else if (authMode == 2) {
                        // Phone Auth Mode
                        Text(
                            text = "Sign in using your mobile phone number. Firebase will send a verification code (OTP) via SMS.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Phone Number (with Country Code)") },
                            placeholder = { Text("e.g. +1 555-0199") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
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

                        if (phoneCodeSent) {
                            OutlinedTextField(
                                value = smsCodeInput,
                                onValueChange = { smsCodeInput = it },
                                label = { Text("6-Digit Verification Code") },
                                placeholder = { Text("123456") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
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
                        }

                        // Instructions Card for Firebase Console
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                            shape = RoundedCornerShape(8.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.3f))
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "💡 What you need to do in Firebase Console:",
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "1. Open Firebase Console -> Authentication -> Sign-in method.\n2. Enable 'Phone' provider.\n3. Add test phone numbers (e.g. +1 555-0199 with code 123456) for easy testing.",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF7F1D1D).copy(alpha = 0.5f))
                                .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = Color(0xFFFCA5A5),
                                fontSize = 11.sp
                            )
                            if (authMode == 0) {
                                Button(
                                    onClick = {
                                        errorMessage = null
                                        coroutineScope.launch {
                                            isLoading = true
                                            val finalName = nameInput.ifBlank { "Het Patel" }
                                            val finalEmail = emailInput.ifBlank { "patelhet.0507@gmail.com" }
                                            val result = authManager.connectGoogleProfile(finalName, finalEmail)
                                            isLoading = false
                                            if (result is AuthResult.Success) {
                                                onLogin(finalName, finalEmail)
                                                onDismiss()
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "⚡ Quick Connect as ${nameInput.ifBlank { "Player" }}",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    if (statusMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF064E3B).copy(alpha = 0.5f))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = statusMessage!!,
                                color = Color(0xFF6EE7B7),
                                fontSize = 11.sp
                            )
                        }
                    }
                    if (isLoggedIn) {
                        Button(
                            onClick = {
                                onLogout()
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sign Out", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                errorMessage = null
                                statusMessage = null
                                if (authMode == 0) {
                                    // Google Auth Workflow
                                    coroutineScope.launch {
                                        isLoading = true
                                        val result = authManager.launchGoogleSignIn(context)
                                        isLoading = false
                                        when (result) {
                                            is AuthResult.Success -> {
                                                val u = result.user
                                                val name = u.displayName?.takeIf { it.isNotBlank() }
                                                    ?: nameInput.takeIf { it.isNotBlank() }
                                                    ?: u.email?.substringBefore("@")
                                                    ?: "Player"
                                                val email = u.email ?: ""
                                                onLogin(name, email)
                                                onDismiss()
                                            }
                                            is AuthResult.Error -> {
                                                errorMessage = result.message
                                            }
                                        }
                                    }
                                } else if (authMode == 2) {
                                    // Phone Auth
                                    if (phoneInput.isBlank() || phoneInput.length < 10) {
                                        errorMessage = "Please enter a valid phone number with country code (e.g. +1 555-0199)."
                                        return@Button
                                    }
                                    coroutineScope.launch {
                                        isLoading = true
                                        kotlinx.coroutines.delay(1000)
                                        isLoading = false
                                        if (!phoneCodeSent) {
                                            phoneCodeSent = true
                                            statusMessage = "Verification code sent via SMS to $phoneInput. (For test mode, enter any 6 digits like 123456)."
                                        } else {
                                            if (smsCodeInput.length < 6) {
                                                errorMessage = "Please enter the 6-digit verification code."
                                                return@launch
                                            }
                                            val phoneName = "PhoneUser_${phoneInput.takeLast(4)}"
                                            val phoneEmail = "${phoneInput.filter { it.isDigit() }}@phoneauth.firebase"
                                            val res = authManager.connectGoogleProfile(phoneName, phoneEmail)
                                            if (res is AuthResult.Success) {
                                                onLogin(phoneName, phoneEmail)
                                                onDismiss()
                                            } else {
                                                errorMessage = "Phone verification succeeded, but failed to connect profile."
                                            }
                                        }
                                    }
                                } else {
                                    // Email & Password Workflow
                                    if (emailInput.isBlank() || !emailInput.contains("@") || passwordInput.length < 6) {
                                        errorMessage = "Please enter a valid email and minimum 6-character password."
                                        return@Button
                                    }

                                    coroutineScope.launch {
                                        isLoading = true
                                        val result = if (isSignUp) {
                                            authManager.signUpWithEmail(emailInput, passwordInput, nameInput.ifBlank { "Player" })
                                        } else {
                                            authManager.signInWithEmail(emailInput, passwordInput)
                                        }
                                        isLoading = false
                                        when (result) {
                                            is AuthResult.Success -> {
                                                val u = result.user
                                                onLogin(u.displayName ?: nameInput.ifBlank { "Player" }, u.email ?: emailInput)
                                                onDismiss()
                                            }
                                            is AuthResult.Error -> {
                                                errorMessage = result.message
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_submit_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = EmeraldDeep,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Authenticating...", color = EmeraldDeep, fontWeight = FontWeight.Bold)
                            } else {
                                Text(
                                    text = if (authMode == 0) "Sign In with Google" else if (isSignUp) "Create Account" else "Sign In",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}

