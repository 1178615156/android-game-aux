package com.example.yujieshui.myapplication

import android.content.Context
import android.os.StrictMode
import android.support.v4.content.ContextCompat
import android.util.Log
import com.example.yujieshui.myapplication.HelpFunc.screencap
import com.example.yujieshui.myapplication.HelpFunc.tap
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.*
import java.io.File


class ServiceThread(val ip: String, val port: String) : Thread() {


  override fun run() {
//    test_screencap();return;
//    StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build())
    while (!this.isInterrupted) {
      try {
        val fileName = "/sdcard/tmp/screen-${System.currentTimeMillis()}.png"
//        val fileName = "/sdcard/tmp/screen.png"
        HelpFunc.screencap(fileName)
        val file = File(fileName)
        Log.w("LogDemo", "screen size : " + file.length().toString())

        val response = Utensil.postFile("http://${this.ip}:${this.port}/files", File(fileName))
        val json = Utensil.readJson(response)

        for (action in json) {
          Log.i("action", "do $action")
          when (action) {
            is DelayAction -> Thread.sleep(action.time.toLong())
            is TapAction   -> tap(x = action.x, y = action.y)
          }
        }
        // default delay 1000ms
        if (json.find { it is DelayAction } == null) {
          Log.i("default-delay", "1000")
          Thread.sleep(1000)
        }

      } catch (e: InterruptedException) {
        return
      } catch (e: Exception) {
        e.printStackTrace()
        try {
          Thread.sleep(1000)
        } catch (e: Exception) {
          return
        }
      }
    }


  }


  fun test_screencap() {

    val timeByShell = (1..60).map { it ->
      val startTime = System.currentTimeMillis()
      val fileName = "/sdcard/tmp/screen-${System.currentTimeMillis()}.png"
      val file = File(fileName)
      HelpFunc.screencap(fileName)
      val endTime = System.currentTimeMillis()
      Log.w("LogDemo", "screen size : " + file.length().toString())
      Log.w("time", ((endTime - startTime).toDouble() / 1000).toString())

      Thread.sleep(1000)
      (endTime - startTime).toDouble() / 1000
    }
    Log.w("timeBySHell", (timeByShell.sum() / timeByShell.size).toString())
  }
}