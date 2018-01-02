package nyhx

import models.Point

object Points {

  object Treatment {
    val huPoZiQuan = Point(304, 164, "huPoZiQuan")
  }

  object Area {
    val one   = Point(176, 76, "1")
    val two   = Point(285, 74, "2")
    val three = Point(391, 74, "3")
    val four  = Point(509, 73, "4")
    val five  = Point(626, 75, "5")
    val six   = Point(742, 77, "6")
  }

  object Adventure {

    object Six {

      object Four {
        val b = Point(353, 194, "b")
      }

    }

    object AreaThree {

      object One {
        val b    = Point(373, 203, "b")
        val e    = Point(207, 173, "e")
        val f    = Point(606, 172, "f")
        val boss = Point(863, 155, "boss")
      }

    }

    object Five {

      object One {
        val b = Point(263, 192, "b")
        val c = Point(137, 396, "g")
        val d = Point(307, 416, "d")
        val e = Point(480, 421, "e")
        val f = Point(680, 438, "f")
      }

    }

    object Three {

      object Six {
        val b = Point(131, 242)
        val c = Point(99, 437)
        val g = Point(362, 463)
        val h = Point(599, 441)
      }

    }

    object Two {

      object One {
        val c = Point(162, 373)
        val f = Point(655, 405)
        val g = Point(826, 465)
      }

      object Six {
        val b = Point(185, 237)
        val c = Point(123, 393)
        val e = Point(330, 369)
        val f = Point(438, 483)
      }

    }

    object One {

      object Three {
        val b = Point(327, 360)
      }

      object Four {
        val b    = Point(330, 116)
        val c    = Point(251, 378)
        val f    = Point(439, 408)
        val g    = Point(631, 405)
        val boss = Point(805, 353)
      }

    }

    val next = Point(914, 306, "next")
  }

  object Explore {

    object Map {
      val one   = Point(229, 441, "1")
      val two   = Point(480, 439, "2")
      val three = Point(753, 434, "3")
    }

    object OneThreeDirect {
      val one   = Point(453, 168)
      val two   = Point(784, 417)
      val three = Point(136, 258)
    }

    object ThreeThreeDirect {
      val one   = Point(359, 450, "one  ".trim)
      val two   = Point(388, 228, "two  ".trim)
      val three = Point(824, 117, "three".trim)
    }

    object FourThreeDirect {
      val one   = Point(232, 160, "one  ".trim)
      val two   = Point(502, 406, "two  ".trim)
      val three = Point(666, 227, "three".trim)
    }

    object FiveThreeDirect {
      val one   = Point(782, 119, "one  ".trim)
      val two   = Point(602, 269, "two  ".trim)
      val three = Point(449, 411, "three".trim)
    }

  }

  object Grean {
    val teXun = Point(123, 59)
  }

  object Group {
    val a = Point(42, 144, "a")
    val b = Point(42, 197, "b")
    val c = Point(42, 262, "c")
    val d = Point(42, 325, "d")
    val e = Point(42, 385, "e")
  }

}
