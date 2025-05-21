package com.example.frontend_happygreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.frontend_happygreen.api.JotformQuestion
import com.example.frontend_happygreen.models.FormSubmissionState
import com.example.frontend_happygreen.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Form question component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormQuestion(
    question: JotformQuestion,
    formState: FormSubmissionState,
    onAnswerChanged: (String, String) -> Unit,
    isFormCompleted: Boolean = false
) {
    val answer = formState.answers[question.name] ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Question text
        Text(
            text = question.text + if (question.required) " *" else "",
            fontWeight = FontWeight.Bold,
            color = Green800
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Different input types based on question type
        when (question.type) {
            "control_textbox" -> {
                OutlinedTextField(
                    value = answer,
                    onValueChange = { onAnswerChanged(question.name, it) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isFormCompleted,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Green300,
                        unfocusedBorderColor = Green100
                    ),
                    placeholder = { Text("La tua risposta") }
                )
            }

            "control_textarea" -> {
                OutlinedTextField(
                    value = answer,
                    onValueChange = { onAnswerChanged(question.name, it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    enabled = !isFormCompleted,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Green300,
                        unfocusedBorderColor = Green100
                    ),
                    placeholder = { Text("La tua risposta") },
                    maxLines = 5
                )
            }

            "control_dropdown" -> {
                val options = question.options?.get("options") as? Map<*, *> ?: emptyMap<String, String>()

                var expanded by remember { mutableStateOf(false) }
                val selectedOption = if (answer.isNotEmpty()) answer else "Seleziona un'opzione"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .border(
                            width = 1.dp,
                            color = Green100,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(enabled = !isFormCompleted) { expanded = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedOption,
                            color = if (answer.isEmpty()) Color.Gray else Color.Black
                        )

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand dropdown",
                            tint = Green600
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        options.entries.forEach { (key, value) ->
                            val optionValue = value.toString()
                            DropdownMenuItem(
                                text = { Text(optionValue) },
                                onClick = {
                                    onAnswerChanged(question.name, optionValue)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            "control_radio" -> {
                val options = question.options?.get("options") as? Map<*, *> ?: emptyMap<String, String>()

                Column(modifier = Modifier.fillMaxWidth()) {
                    options.entries.forEach { (key, value) ->
                        val optionValue = value.toString()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !isFormCompleted) {
                                    onAnswerChanged(question.name, optionValue)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = answer == optionValue,
                                onClick = {
                                    if (!isFormCompleted) {
                                        onAnswerChanged(question.name, optionValue)
                                    }
                                },
                                enabled = !isFormCompleted,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Green600,
                                    unselectedColor = Green300
                                )
                            )

                            Text(
                                text = optionValue,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            "control_checkbox" -> {
                val options = question.options?.get("options") as? Map<*, *> ?: emptyMap<String, String>()
                val selectedOptions = answer.split(",").filter { it.isNotEmpty() }.toMutableStateList()

                Column(modifier = Modifier.fillMaxWidth()) {
                    options.entries.forEach { (key, value) ->
                        val optionValue = value.toString()
                        val isSelected = selectedOptions.contains(optionValue)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !isFormCompleted) {
                                    if (isSelected) {
                                        selectedOptions.remove(optionValue)
                                    } else {
                                        selectedOptions.add(optionValue)
                                    }
                                    onAnswerChanged(question.name, selectedOptions.joinToString(","))
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = {
                                    if (!isFormCompleted) {
                                        if (isSelected) {
                                            selectedOptions.remove(optionValue)
                                        } else {
                                            selectedOptions.add(optionValue)
                                        }
                                        onAnswerChanged(question.name, selectedOptions.joinToString(","))
                                    }
                                },
                                enabled = !isFormCompleted,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Green600,
                                    uncheckedColor = Green300
                                )
                            )

                            Text(
                                text = optionValue,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            "control_number" -> {
                OutlinedTextField(
                    value = answer,
                    onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) onAnswerChanged(question.name, it) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isFormCompleted,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Green300,
                        unfocusedBorderColor = Green100
                    ),
                    placeholder = { Text("Inserisci un numero") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            "control_email" -> {
                OutlinedTextField(
                    value = answer,
                    onValueChange = { onAnswerChanged(question.name, it) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isFormCompleted,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Green300,
                        unfocusedBorderColor = Green100
                    ),
                    placeholder = { Text("Inserisci l'email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }

            "control_phone" -> {
                OutlinedTextField(
                    value = answer,
                    onValueChange = { if (it.isEmpty() || it.all { char -> char.isDigit() || char == '+' || char == ' ' || char == '(' || char == ')' || char == '-' }) onAnswerChanged(question.name, it) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isFormCompleted,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Green300,
                        unfocusedBorderColor = Green100
                    ),
                    placeholder = { Text("Inserisci il numero di telefono") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
            }

            "control_datetime" -> {
                // Date picker would go here, using simplified version for now
                OutlinedTextField(
                    value = answer,
                    onValueChange = { onAnswerChanged(question.name, it) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isFormCompleted,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Green300,
                        unfocusedBorderColor = Green100
                    ),
                    placeholder = { Text("GG/MM/AAAA") }
                )
            }

            "control_address" -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = answer,
                        onValueChange = { onAnswerChanged(question.name, it) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isFormCompleted,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Green300,
                            unfocusedBorderColor = Green100
                        ),
                        placeholder = { Text("Inserisci l'indirizzo") }
                    )
                }
            }

            else -> {
                // Default to text input for any other type
                OutlinedTextField(
                    value = answer,
                    onValueChange = { onAnswerChanged(question.name, it) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isFormCompleted,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Green300,
                        unfocusedBorderColor = Green100
                    ),
                    placeholder = { Text("La tua risposta") }
                )
            }
        }
    }
}

/**
 * Form display component
 */
@Composable
fun JotformDisplay(
    formId: String,
    formTitle: String,
    questions: List<JotformQuestion>,
    formState: FormSubmissionState,
    onAnswerChanged: (String, String) -> Unit,
    onSubmit: () -> Unit,
    isFormCompleted: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Green300, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        // Form header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Green900
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Compila questo modulo per continuare",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            if (isFormCompleted) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Green600, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White
                    )
                }
            }
        }

        Divider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = Color.LightGray
        )

        // Form questions
        questions.forEach { question ->
            FormQuestion(
                question = question,
                formState = formState,
                onAnswerChanged = onAnswerChanged,
                isFormCompleted = isFormCompleted
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Submit button
        if (!isFormCompleted) {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = !formState.isSubmitting && validateForm(questions, formState),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green600,
                    disabledContainerColor = Green200
                )
            ) {
                if (formState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Invia")
                }
            }
        } else {
            Text(
                text = "Modulo inviato il ${formatDate(Date())}",
                style = MaterialTheme.typography.bodySmall,
                color = Green600,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * Validate form answers against required fields
 */
private fun validateForm(questions: List<JotformQuestion>, formState: FormSubmissionState): Boolean {
    // Check if all required questions are answered
    val requiredQuestions = questions.filter { it.required }

    return requiredQuestions.all { question ->
        val answer = formState.answers[question.name] ?: ""
        answer.isNotBlank()
    }
}

/**
 * Format date for display
 */
private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}