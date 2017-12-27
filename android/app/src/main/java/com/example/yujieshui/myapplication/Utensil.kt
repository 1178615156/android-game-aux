package com.example.yujieshui.myapplication

import android.util.Log
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import okhttp3.internal.http.CallServerInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Created by yujieshui on 2017/12/26.
 */
object Utensil {
  private val okHttp = OkHttpClient().newBuilder().writeTimeout(50, TimeUnit.SECONDS).build()
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
        .header("Content-Encoding", "gzip")
//        .header("Connection", "close")
//        .header("Content-Type","image/png")
        .post(multipartBody)
        .build()
    Log.w("headers",request.headers().toString())
    val response = okHttp.newCall(request).execute().body()!!.string()
    return response as String
  }

}