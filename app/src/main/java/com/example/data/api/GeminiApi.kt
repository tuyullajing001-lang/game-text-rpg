package com.example.data.api

import com.example.BuildConfig
import com.example.data.model.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateStoryResponse(
        prompt: String,
        gameState: GameState,
        customApiKey: String? = null,
        selectedModel: String = "gemini-3.5-flash"
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = customApiKey?.takeIf { it.isNotBlank() }
            ?: try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Throwable) {
                ""
            }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Return local narrative engine result seamlessly if key is not yet set
            return@withContext Result.success(
                LocalMasterEngine.generateResponse(prompt, gameState)
            )
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$selectedModel:generateContent?key=$apiKey"

        try {
            val root = JSONObject()
            
            // System instruction
            val systemInstruction = JSONObject()
            val sysParts = JSONArray()
            val sysPart = JSONObject().put("text", SystemPrompt.FULL_PROMPT + "\n\n" + buildGameStateContext(gameState))
            sysParts.put(sysPart)
            systemInstruction.put("parts", sysParts)
            root.put("systemInstruction", systemInstruction)

            // Contents history (last 6 messages for context)
            val contents = JSONArray()
            val recentMessages = gameState.chatMessages.takeLast(6)
            for (msg in recentMessages) {
                val role = if (msg.sender == "MASTER") "user" else "model"
                val contentObj = JSONObject()
                contentObj.put("role", role)
                val parts = JSONArray()
                parts.put(JSONObject().put("text", msg.text))
                contentObj.put("parts", parts)
                contents.put(contentObj)
            }
            
            // Current user turn
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", prompt))
            currentTurn.put("parts", currentParts)
            contents.put(currentTurn)
            
            root.put("contents", contents)

            // Generation config
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            generationConfig.put("topP", 0.95)
            root.put("generationConfig", generationConfig)

            val body = root.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: "Unknown error"
                // Fallback to local engine with a note
                val fallback = LocalMasterEngine.generateResponse(prompt, gameState)
                return@withContext Result.success(
                    "$fallback\n\n*(Catatan Sistem: Respon offline digunakan. Status API: ${response.code})*"
                )
            }

            val respBody = response.body?.string() ?: ""
            val jsonResp = JSONObject(respBody)
            val candidates = jsonResp.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else {
                Result.success(LocalMasterEngine.generateResponse(prompt, gameState))
            }
        } catch (e: Exception) {
            val fallback = LocalMasterEngine.generateResponse(prompt, gameState)
            Result.success("$fallback\n\n*(Catatan Sistem: Respon dialihkan ke offline engine: ${e.localizedMessage})*")
        }
    }

    private fun buildGameStateContext(gameState: GameState): String {
        val heroList = gameState.heroes.filter { it.isAlive }.joinToString("\n") { h ->
            "- ${h.name} [★${h.starGrade} Lv.${h.level}] Class: ${h.jobClass} (${h.jobTier}) | HP: ${h.currentHp}/${h.maxHp} | Fat: ${h.fatigue} | Strs: ${h.stress} | Tag: [${h.tag}]"
        }
        val deadList = gameState.graveyard.joinToString("\n") { h ->
            "- ${h.name} [★${h.starGrade} Lv.${h.level}] (Gugur)"
        }

        return """
=== KONTEKS STATUS GAME TERBARU ===
Master: ${gameState.masterName} | Peri Asisten: ${gameState.fairyName}
${gameState.formattedTime}
Tingkat Kesulitan: ${gameState.difficulty}
Wallet: ${gameState.gold} Gold | ${gameState.diamond} Diamond
Material: CM ${gameState.materials.cm}, UM ${gameState.materials.um}, RM ${gameState.materials.rm}, EM ${gameState.materials.em}
Menara Mobius: Lantai ${gameState.towerFloorCurrent} (Tertinggi: ${gameState.towerFloorHighest})
Pahlawan Hidup (${gameState.livingHeroesCount}):
$heroList
Kuburan (${gameState.graveyard.size}):
${if (deadList.isBlank()) "(Belum ada yang gugur)" else deadList}
===================================
"""
    }
}
