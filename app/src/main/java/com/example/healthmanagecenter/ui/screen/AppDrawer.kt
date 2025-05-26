package com.example.healthmanagecenter.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawer(
    userName: String,
    userRole: String,
    userId: Long,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Person, contentDescription = "User", modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(8.dp))
            Text(userName, style = MaterialTheme.typography.titleLarge)
            Text(userRole, style = MaterialTheme.typography.bodyMedium)
            Text("ID: $userId", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onLogout()
                    onClose()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
} 