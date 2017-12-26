package com.example.yujieshui.myapplication

import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Created by yujieshui on 2017/12/2.
 */
class ServiceThreadTest {

  @Test
  fun readJsonTest() {
    val result: List<Action> = ServiceThread().readJson("""[{"action":"delay","time":1}]""")
    assert(result.size == 1)
    assert(result.get(0) is DelayAction)
    assert((result.get(0) as DelayAction).time == 1)
  }

  @Test
  fun postFileTest() {
    val result = ServiceThread().postFile(
        url = "http://127.0.0.1:9898/run",
        file = File("D:\\2016-05-14_201535.png"))
    assert(ServiceThread().readJson(result).isEmpty())
  }

}