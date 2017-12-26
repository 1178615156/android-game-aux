package com.example.yujieshui.myapplication

import java.io.*
import java.util.*

object HelpFunc {
  fun upgradeRootPermission(pkgCodePath: String): Boolean {
    var process: Process? = null
    var os: DataOutputStream? = null
    try {
      val cmd = "chmod 777 " + pkgCodePath
      process = Runtime.getRuntime().exec("su") //切换到root帐号
      os = DataOutputStream(process!!.outputStream)
      os!!.writeBytes(cmd + "\n")
      os!!.writeBytes("exit\n")
      os!!.flush()
      process!!.waitFor()
    } catch (e: Exception) {
      return false
    } finally {
      try {
        if (os != null) {
          os!!.close()
        }
        process!!.destroy()
      } catch (e: Exception) {
      }

    }
    return true
  }

  fun execShellCmd(cmd: String): String? {

    try {
      // 申请获取root权限，这一步很重要，不然会没有作用
      val process: Process = Runtime.getRuntime().exec("su")
      val outputStream = process.outputStream
      val dataOutputStream = DataOutputStream(outputStream)
      dataOutputStream.writeBytes(cmd)
      dataOutputStream.flush()
      dataOutputStream.close()
      outputStream.close()

      return BufferedReader(InputStreamReader(process.inputStream)).readText()
    } catch (t: Throwable) {
      t.printStackTrace()
      return null;
    }

  }

  fun tap(x: Int, y: Int): Unit {
    execShellCmd("input tap " + x + " " + y);
  }

  fun screencap(fileName: String) {
//    execShellCmd("input keyevent 120")
    execShellCmd("/system/bin/screencap -p " + fileName)
//        execShellCmd("/system/bin/screenshot -p " + fileName)
  }
}