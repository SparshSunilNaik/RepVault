package com.example.workouttracker.ui.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workouttracker.RepVaultApp
import com.example.workouttracker.ui.theme.Accent
import com.example.workouttracker.ui.theme.Divider
import com.example.workouttracker.ui.theme.SecondaryText
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val app = context.applicationContext as RepVaultApp
    val backupManager = app.backupManager

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = backupManager.exportToUri(context, uri)
                val message = if (success) "Data exported successfully" else "Export failed"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val success = backupManager.importFromUri(context, uri)
                val message = if (success) "Data imported successfully" else "Import failed"
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 56.dp, bottom = 24.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer
                        )
                        .padding(24.dp)
                ) {
                    Text(
                        text = uiState.userName.take(2).ifEmpty { "RV" }.uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = uiState.userName.ifEmpty { "RepVault Athlete" },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Lifter since 2025",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileStatRow(label = "Total Workouts", value = "${uiState.totalWorkouts}")
                HorizontalDivider(
                    color = Divider,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                ProfileStatRow(label = "Total Sets", value = "${uiState.totalSets}")
                HorizontalDivider(
                    color = Divider,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                ProfileStatRow(
                    label = "Best Lift",
                    value = if (uiState.bestLiftName.isNotEmpty()) {
                        "${uiState.bestLiftName} / ${uiState.bestLiftWeight}"
                    } else {
                        "\u2014"
                    }
                )
                HorizontalDivider(
                    color = Divider,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                ProfileStatRow(
                    label = "Current Streak",
                    value = "${uiState.currentStreak} day${if (uiState.currentStreak != 1) "s" else ""}"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Body Metrics",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileStatRow(label = "Weight", value = "\u2014")
                HorizontalDivider(
                    color = Divider,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                ProfileStatRow(label = "Height", value = "\u2014")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = {
                        exportLauncher.launch("repvault_backup.json")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Export Data",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Accent
                    )
                }

                HorizontalDivider(
                    color = Divider,
                    thickness = 1.dp
                )

                TextButton(
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Import Data",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Accent
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = SecondaryText
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}
