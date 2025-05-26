package com.example.healthmanagecenter.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.healthmanagecenter.data.entity.UserEntity
import com.example.healthmanagecenter.viewmodel.ElderHomeViewModel
import com.example.healthmanagecenter.viewmodel.LoginRegisterViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickDoctorScreen(
    userId: Long,
    onDoctorPicked: (Long) -> Unit,
    elderHomeViewModel: ElderHomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    loginRegisterViewModel: LoginRegisterViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val doctors by elderHomeViewModel.getAllDoctors().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select Your Doctor",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(doctors) { doctor ->
                DoctorCard(
                    doctor = doctor,
                    onClick = {
                        scope.launch {
                            if (loginRegisterViewModel.updateUserDoctor(userId, doctor.userId)) {
                                onDoctorPicked(userId)
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoctorCard(
    doctor: UserEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Doctor", modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(doctor.name, style = MaterialTheme.typography.titleMedium)
                Text("Phone: ${doctor.phone}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
} 