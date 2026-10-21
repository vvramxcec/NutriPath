package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.StatusRejectedRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard

@Composable
fun DeleteAccountDialog(
  isProcessing: Boolean,
  onDismiss: () -> Unit,
  onConfirmDelete: () -> Unit
) {
  AlertDialog(
    onDismissRequest = { if (!isProcessing) onDismiss() },
    containerColor = WarmWhiteCard,
    title = {
      Text(
        text = "Delete Account & Purge Data?",
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = DarkCharcoalText
      )
    },
    text = {
      Column {
        Text(
          text = "This will permanently delete your cloud account and all associated backups. Furthermore, all local health profiles, meal plans, and history logs on this device will be immediately wiped.",
          color = TextMuted,
          fontSize = 13.sp,
          lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "This action is irreversible.",
          color = StatusRejectedRed,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirmDelete,
        enabled = !isProcessing,
        colors = ButtonDefaults.buttonColors(containerColor = StatusRejectedRed),
        modifier = Modifier.testTag("confirm_delete_account_button")
      ) {
        Text(
          text = if (isProcessing) "Deleting..." else "Delete Account & Wipe",
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        enabled = !isProcessing,
        modifier = Modifier.testTag("cancel_delete_account_button")
      ) {
        Text("Cancel", color = TextMuted)
      }
    }
  )
}
