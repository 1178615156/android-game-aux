package com.example.yujieshui.myapplication

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Created by yujieshui on 2017/12/2.
 */
class UtensilTest {

  @Test
  fun readJsonTest() {
    val result: List<Action> = Utensil.readJson("""[{"action":"delay","time":1}]""")
    assert(result.size == 1)
    assert(result.get(0) is DelayAction)
    assert((result.get(0) as DelayAction).time == 1)
  }

  @Test
  fun postFileTest() {
    println(System.getProperty("user.dir"))
    println(File("test-image.png").length())
    (1..100).forEach { it ->

      val result = Utensil.postFile(
          url = "http://192.168.1.100:9898/files",
          file = File("test-image.png"))
      println(Utensil.readJson(result))
    }
  }

}