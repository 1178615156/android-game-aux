package com.example.yujieshui.myapplication

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
  private var thread: Thread? = null
  private var isRun = false
  private val lock = java.util.concurrent.locks.ReentrantReadWriteLock()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    Log.i("image", "hello world")

    val shared = PreferenceManager.getDefaultSharedPreferences(this);

    val runButton = findViewById<Button>(R.id.run)
    val stopButton = findViewById<Button>(R.id.stop)
    val ip = findViewById<EditText>(R.id.ip)
    val port = findViewById<EditText>(R.id.port)

    if (shared.getString("ip", "") != "") {
      ip.setText(shared.getString("ip", ""))
    }
    if (shared.getString("port", "") != "") {
      port.setText(shared.getString("port", ""))
    }


    runButton.setOnClickListener {
      synchronized(lock) {

        lock.writeLock().lock()
        if (isRun) {
          Toast.makeText(applicationContext, "service is run; do nothing", Toast.LENGTH_SHORT).show()
        } else {
          Log.i("ip", ip.text.toString())
          Log.i("port", port.text.toString())

          shared.edit().putString("ip", ip.text.toString()).commit()
          shared.edit().putString("port", port.text.toString()).commit()
          isRun = true
          thread = ServiceThread(ip.text.toString(), port.text.toString())
          thread?.start()
          Toast.makeText(applicationContext, "service start run", Toast.LENGTH_SHORT).show()
        }

      }
    }
    stopButton.setOnClickListener {
      synchronized(lock) {
        lock.writeLock().lock()
        if (isRun) {
          Toast.makeText(applicationContext, "[start]  stop service", Toast.LENGTH_SHORT).show()
          thread?.interrupt()
//            thread?.join()
          thread = null
          isRun = false
          Toast.makeText(applicationContext, "[finish] stop service", Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(applicationContext, "service is stop; do nothing", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

}
