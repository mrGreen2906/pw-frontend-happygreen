package com.example.frontend_happygreen.models

import android.net.Uri
import com.example.frontend_happygreen.api.JotformQuestion
import java.util.*

/**
 * Expanded ChatMessage type to include form messages
 */
sealed class ChatContent {
    data class TextContent(val text: String) : ChatContent()
    data class ImageContent(val imageUri: Uri) : ChatContent()
    data class FormContent(
        val formId: String,
        val formTitle: String,
        val questions: List<JotformQuestion>,
        val isCompleted: Boolean = false
    ) : ChatContent()
}

/**
 * Enhanced ChatMessage with support for different content types
 */
data class EnhancedChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: ChatContent,
    val isFromUser: Boolean,
    val timestamp: Date = Date()
)

/**
 * Form submission state
 */
data class FormSubmissionState(
    val formId: String,
    val messageId: String,
    val answers: MutableMap<String, String> = mutableMapOf(),
    val isSubmitting: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null
)

/**
 * Jotform bot triggers
 */
object JotformBotTriggers {
    // Keywords that should trigger specific forms
    val wasteSurveyTriggers = listOf(
        "sondaggio", "survey", "feedback", "questionario", "opinione",
        "esperienza", "riciclo", "migliorare", "valutazione"
    )

    val wasteReportTriggers = listOf(
        "segnalare", "report", "problema", "discarica", "abusivo",
        "segnalazione", "illegale", "rifiuti abbandonati"
    )

    val pickupRequestTriggers = listOf(
        "ritiro", "pickup", "raccolta", "ingombranti", "speciali",
        "prenotare", "richiesta", "prenotazione"
    )

    // Map of form types to form IDs - these would be your actual Jotform form IDs
    val formIdMap = mapOf(
        "waste_survey" to "230051968463055",
        "waste_report" to "230051960834050",
        "pickup_request" to "230051952559051"
    )
}