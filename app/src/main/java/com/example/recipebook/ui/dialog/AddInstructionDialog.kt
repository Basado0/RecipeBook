package com.example.recipebook.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddInstructionDialog(
    instructionName: String,
    steps: List<String>,
    onNameChange: (String) -> Unit,
    onStepChange: (Int, String) -> Unit,
    onAddStep: () -> Unit,
    onRemoveStep: (Int) -> Unit,
    onDismiss: () -> Unit,
    onAdd: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Instruction") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = instructionName,
                    onValueChange = onNameChange,
                    label = { Text("Section name (optional)") },
                    placeholder = { Text("e.g., For the sauce") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Steps",
                    style = MaterialTheme.typography.titleSmall
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(steps) { index, step ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${index + 1}.",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            OutlinedTextField(
                                value = step,
                                onValueChange = { onStepChange(index, it) },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Step ${index + 1}") }
                            )
                            if (steps.size > 1) {
                                IconButton(onClick = { onRemoveStep(index) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Remove step"
                                    )
                                }
                            }
                        }
                    }

                    item {
                        TextButton(onClick = onAddStep) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Text("Add step")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onAdd,
                enabled = steps.any { it.isNotBlank() }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}