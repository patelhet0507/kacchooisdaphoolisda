package com.example.auth

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.net.ConnectException
import java.net.UnknownHostException

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class AuthResultHandler(
    private val authManager: AuthManager,
    private val onError: (String) -> Unit,
    private val onStatus: (String) -> Unit,
    private val onLoading: (Boolean) -> Unit
) {

    suspend fun verifyPhoneNumber(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (String, PhoneAuthProvider.ForceResendingToken) -> Unit,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit
    ) {
        onLoading(true)
        onStatus("Initiating verification & reCAPTCHA...")

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                onLoading(false)
                onStatus("Auto-verification successful!")
                onVerificationCompleted(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onLoading(false)
                val friendlyMessage = mapException(e)
                onError(friendlyMessage)
                Log.e("AuthResultHandler", "Verification failed", e)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onLoading(false)
                onStatus("Verification code sent via SMS!")
                onCodeSent(verificationId, token)
            }
        }

        try {
            authManager.verifyPhoneNumber(activity, phoneNumber, callbacks)
        } catch (e: Exception) {
            onLoading(false)
            onError("Failed to start verification: ${e.localizedMessage ?: "Unknown error"}")
            Log.e("AuthResultHandler", "Exception in verifyPhoneNumber", e)
        }
    }

    suspend fun signInWithPhone(
        verificationId: String,
        smsCode: String,
        onSuccess: (AuthUserState) -> Unit
    ) {
        onLoading(true)
        onStatus("Verifying code...")
        try {
            val result = authManager.signInWithPhoneCredential(verificationId, smsCode)
            onLoading(false)
            when (result) {
                is AuthResult.Success -> onSuccess(result.user)
                is AuthResult.Error -> onError(result.message)
            }
        } catch (e: Exception) {
            onLoading(false)
            onError(mapException(e))
            Log.e("AuthResultHandler", "Exception in signInWithPhone", e)
        }
    }

    suspend fun signInWithCredential(
        credential: PhoneAuthCredential,
        onSuccess: (AuthUserState) -> Unit
    ) {
        onLoading(true)
        onStatus("Signing in...")
        try {
            val result = authManager.signInWithPhoneAuthCredential(credential)
            onLoading(false)
            when (result) {
                is AuthResult.Success -> onSuccess(result.user)
                is AuthResult.Error -> onError(result.message)
            }
        } catch (e: Exception) {
            onLoading(false)
            onError(mapException(e))
            Log.e("AuthResultHandler", "Exception in signInWithCredential", e)
        }
    }

    fun mapException(e: Exception): String {
        return when (e) {
            is FirebaseAuthInvalidCredentialsException ->
                "The verification code entered is incorrect or has expired. Please check and try again."
            is FirebaseTooManyRequestsException ->
                "Too many attempts. We have blocked all requests from this device due to unusual activity. Try again later."
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_SESSION_EXPIRED" -> "Verification session expired. Please request a new code."
                    "ERROR_QUOTA_EXCEEDED" -> "SMS quota exceeded for today. Please try another method or wait 24 hours."
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your internet connection and try again."
                    "ERROR_INVALID_PHONE_NUMBER" -> "The phone number entered is invalid. Please check the format and country code."
                    else -> e.localizedMessage ?: "Authentication failed. Please retry."
                }
            }
            is UnknownHostException, is ConnectException ->
                "No internet connection detected. Please check your data or Wi-Fi."
            else -> e.localizedMessage ?: "An unexpected error occurred. Please try again."
        }
    }
}
