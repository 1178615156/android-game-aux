package com.example.yujieshui.myapplication

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.File

/**
 * Created by yujieshui on 2017/12/26.
 */
object Utensil {
  private val okHttp = OkHttpClient()
  private val mapper = ObjectMapper().registerModule(KotlinModule())

  fun readJson(s: String): List<Action> {
    val result: List<Action?> = mapper.readValue<List<JsonNode>>(s).map { json ->
      when (json.get("action").asText()) {
        "tap"   -> mapper.readValue<TapAction>(json.traverse())
        "delay" -> mapper.readValue<DelayAction>(json.traverse())
        else    -> null
      }
    }

    return result.filter { it != null }.map { it as Action }
  }

  fun postFile(url: String, file: File): String {
    val body = RequestBody.create(MediaType.parse("image/png"), file);
    val multipartBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("screen", "screen.png", body)
        .build()

    val request = Request.Builder()
        .url(url)
        .header("Connection", "close")
        .post(multipartBody)
        .build()
    val response = okHttp.newCall(request).execute().body()!!.string()
    return response as String
  }

}