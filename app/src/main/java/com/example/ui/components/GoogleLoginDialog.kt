package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GoogleLoginDialog(
    currentName: String,
    currentEmail: String,
    isLoggedIn: Boolean,
    onDismiss: () -> Unit,
    onLogin: (name: String, email: String) -> Unit,
    onLogout: () -> Unit
) {
    var authMode by remember { mutableStateOf(0) } // 0: Google, 1: Email & Password
    var nameInput by remember { mutableStateOf(if (currentName == "Player 1" || currentName.isBlank()) "" else currentName) }
    var emailInput by remember { mutableStateOf(currentEmail.ifBlank { "player@gmail.com" }) }
    var passwordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = GoldLight,
        textContentColor = TextLight,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (authMode == 0) Color.White else GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    if (authMode == 0) {
                        Text(
                            text = "G",
                            color = Color(0xFF4285F4),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = if (isLoggedIn) "Account Connected" else (if (authMode == 0) "Sign in with Google" else "Email & Password"),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                text = currentName,
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = currentEmail,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "Your account is synced for multiplayer rooms, achievements, and unlocked avatars.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                } else {
                    // Auth mode switcher tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { authMode = 0; errorMessage = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (authMode == 0) GoldPrimary else DarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Google",
                                color = if (authMode == 0) Color.Black else TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = { authMode = 1; errorMessage = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (authMode == 1) GoldPrimary else DarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Email / Password",
                                color = if (authMode == 1) Color.Black else TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (authMode == 0) {
                        Text(
                            text = "Sign in quickly with your Google account to sync your profile and unlocked rewards.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Display Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = EmeraldBorder,
                                focusedLabelColor = GoldLight,
                                unfocusedLabelColor = TextMuted,
                                cursorColor = GoldPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Google Gmail") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = EmeraldBorder,
                                focusedLabelColor = GoldLight,
                                unfocusedLabelColor = TextMuted,
                                cursorColor = GoldPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Text(
                            text = "Enter your email and password to sign up or log in to your Kaachu Phool account.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Display Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = EmeraldBorder,
                                focusedLabelColor = GoldLight,
                                unfocusedLabelColor = TextMuted,
                                cursorColor = GoldPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = EmeraldBorder,
                                focusedLabelColor = GoldLight,
                                unfocusedLabelColor = TextMuted,
                                cursorColor = GoldPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = EmeraldBorder,
                                focusedLabelColor = GoldLight,
                                unfocusedLabelColor = TextMuted,
                                cursorColor = GoldPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isLoggedIn) {
                Button(
                    onClick = {
                        onLogout()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        if (authMode == 1) {
                            if (emailInput.isBlank() || !emailInput.contains("@") || passwordInput.length < 4) {
                                errorMessage = "Please enter a valid email and password (min 4 chars)."
                                return@Button
                            }
                        }
                        val name = nameInput.ifBlank { if (authMode == 0) "Google User" else "Player" }
                        val email = emailInput.ifBlank { "user@example.com" }
                        onLogin(name, email)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (authMode == 0) "Connect Google Account" else "Sign In / Sign Up",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
