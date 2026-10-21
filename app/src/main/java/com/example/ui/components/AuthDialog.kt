package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowBackground
import com.example.ui.theme.StatusRejectedBg
import com.example.ui.theme.StatusRejectedRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard

@Composable
fun AuthDialog(
  isAuthenticating: Boolean,
  onDismiss: () -> Unit,
  onGoogleSignIn: (Context) -> Unit,
  onEmailSignIn: (String, String) -> Unit,
  onEmailSignUp: (String, String) -> Unit
) {
  val context = LocalContext.current
  var isSignUpMode by remember { mutableStateOf(false) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = { if (!isAuthenticating) onDismiss() }) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = WarmWhiteCard,
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("auth_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Top Row: Title + Close Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isSignUpMode) "Create Account" else "Sign In",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = DeepTealDark
          )
          IconButton(
            onClick = onDismiss,
            enabled = !isAuthenticating,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = TextMuted
            )
          }
        }

        Text(
          text = if (isSignUpMode)
            "Sync your personalized health profile and meal plans to the cloud."
          else
            "Sign in to restore your cloud meal history and personalized targets.",
          fontSize = 12.sp,
          color = TextMuted,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 18.dp)
        )

        // Error message banner
        AnimatedVisibility(visible = errorMessage != null) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(StatusRejectedBg)
              .border(1.dp, StatusRejectedRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
              .padding(10.dp)
          ) {
            Text(
              text = errorMessage ?: "",
              fontSize = 12.sp,
              color = StatusRejectedRed,
              fontWeight = FontWeight.Medium
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
        }

        // 1. Google Sign-In Button
        OutlinedButton(
          onClick = {
            errorMessage = null
            onGoogleSignIn(context)
          },
          enabled = !isAuthenticating,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("google_signin_button"),
          colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
          border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)
          )
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            // Google G badge indicator
            Box(
              modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(DeepTealPrimary),
              contentAlignment = Alignment.Center
            ) {
              Text("G", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Sign in with Google",
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold,
              color = DarkCharcoalText
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Divider: or continue with email
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
          Text(
            text = "  or email  ",
            fontSize = 11.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium
          )
          HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSubtle)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Email Field
        OutlinedTextField(
          value = email,
          onValueChange = {
            email = it
            errorMessage = null
          },
          label = { Text("Email Address") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          leadingIcon = {
            Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted)
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("auth_email_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = DarkCharcoalText,
            unfocusedTextColor = DarkCharcoalText,
            focusedBorderColor = DeepTealPrimary,
            unfocusedBorderColor = BorderSubtle
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Password Field
        OutlinedTextField(
          value = password,
          onValueChange = {
            password = it
            errorMessage = null
          },
          label = { Text("Password") },
          singleLine = true,
          visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted)
          },
          trailingIcon = {
            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
              Icon(
                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = "Toggle password visibility",
                tint = TextMuted
              )
            }
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("auth_password_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = DarkCharcoalText,
            unfocusedTextColor = DarkCharcoalText,
            focusedBorderColor = DeepTealPrimary,
            unfocusedBorderColor = BorderSubtle
          )
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Submit Button (Sign In / Register)
        Button(
          onClick = {
            if (email.isBlank() || !email.contains("@")) {
              errorMessage = "Please enter a valid email address."
              return@Button
            }
            if (password.length < 6) {
              errorMessage = "Password must be at least 6 characters."
              return@Button
            }
            errorMessage = null
            if (isSignUpMode) {
              onEmailSignUp(email.trim(), password)
            } else {
              onEmailSignIn(email.trim(), password)
            }
          },
          enabled = !isAuthenticating,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("auth_submit_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary)
        ) {
          if (isAuthenticating) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
          } else {
            Text(
              text = if (isSignUpMode) "Create Account" else "Sign In",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Toggle between Sign In and Create Account
        TextButton(
          onClick = {
            isSignUpMode = !isSignUpMode
            errorMessage = null
          },
          enabled = !isAuthenticating,
          modifier = Modifier.testTag("auth_toggle_mode_button")
        ) {
          Text(
            text = if (isSignUpMode) "Already have an account? Sign In" else "Don't have an account? Create one",
            fontSize = 12.sp,
            color = DeepTealPrimary,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}
