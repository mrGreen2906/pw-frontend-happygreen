package com.example.frontend_happygreen.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

/**
 * Jotform API interface for Retrofit
 */
interface JotformApiService {
    @GET("user/forms")
    suspend fun getForms(
        @Query("apiKey") apiKey: String
    ): Response<JotformFormsResponse>

    @GET("form/{formId}/questions")
    suspend fun getFormQuestions(
        @Query("apiKey") apiKey: String,
        @Path("formId") formId: String
    ): Response<JotformQuestionsResponse>

    @FormUrlEncoded
    @POST("form/{formId}/submissions")
    suspend fun submitForm(
        @Path("formId") formId: String,
        @Query("apiKey") apiKey: String,
        @FieldMap formData: Map<String, String>
    ): Response<JotformSubmissionResponse>
}

/**
 * API response models
 */
data class JotformFormsResponse(
    @SerializedName("responseCode") val responseCode: Int,
    @SerializedName("message") val message: String,
    @SerializedName("content") val content: List<JotformForm>
)

data class JotformForm(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("status") val status: String
)

data class JotformQuestionsResponse(
    @SerializedName("responseCode") val responseCode: Int,
    @SerializedName("message") val message: String,
    @SerializedName("content") val content: Map<String, JotformQuestion>
)

data class JotformQuestion(
    @SerializedName("type") val type: String,
    @SerializedName("text") val text: String,
    @SerializedName("name") val name: String,
    @SerializedName("order") val order: Int,
    @SerializedName("options") val options: Map<String, Any>? = null,
    @SerializedName("required") val required: Boolean = false
)

data class JotformSubmissionResponse(
    @SerializedName("responseCode") val responseCode: Int,
    @SerializedName("message") val message: String,
    @SerializedName("content") val content: JotformSubmission
)

data class JotformSubmission(
    @SerializedName("submissionID") val submissionID: String
)

/**
 * Jotform API Client
 */
class JotformApiClient(private val apiKey: String) {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.jotform.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(JotformApiService::class.java)

    /**
     * Get available forms
     */
    suspend fun getForms(): Result<List<JotformForm>> = withContext(Dispatchers.IO) {
        try {
            val response = service.getForms(apiKey)
            if (response.isSuccessful) {
                Result.success(response.body()?.content ?: emptyList())
            } else {
                Result.failure(Exception("Error fetching forms: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get questions for a specific form
     */
    suspend fun getFormQuestions(formId: String): Result<Map<String, JotformQuestion>> = withContext(Dispatchers.IO) {
        try {
            val response = service.getFormQuestions(apiKey, formId)
            if (response.isSuccessful) {
                Result.success(response.body()?.content ?: emptyMap())
            } else {
                Result.failure(Exception("Error fetching questions: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Submit form data
     */
    suspend fun submitForm(formId: String, formData: Map<String, String>): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = service.submitForm(formId, apiKey, formData)
            if (response.isSuccessful) {
                val submissionId = response.body()?.content?.submissionID ?: ""
                Result.success("Form submitted successfully! Submission ID: $submissionId")
            } else {
                Result.failure(Exception("Error submitting form: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}