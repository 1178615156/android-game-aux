package nyhx

import better.files.File

object Images {
  val returns        = image("returns.png")
  val returns_gakuen = image("returns-gakuen.png")
  val returns_room   = image("returns-room.png")
  val start          = image("start.png")
  val start2         = image("start-2.png")
  val determine      = image("determine.png")

  object YuanZiWu {
    val yuanZiWu           = image("yzw.png")
    val dismissed          = image("yzw-dismissed.png")
    val selectStudent      = image("yzw-select-student.png")
    val dismissedDetermine = image("yzw-dismissed-determine.png")

    val dismissedSelectStudentDetermine = image("yzw-dismissed-select-student-determine.png")
  }

  val lv1 = image("lv1.png")

  object Retrieve {
    val retrieve   = image("retrieve.png")
    val an         = image("retrieve-an.png")
    val shui       = image("retrieve-shui.png")
    val attributes = image("retrieve-attributes.png")
  }


  object Area {
    val one   = image("area-one.png")
    val two   = image("area-two.png")
    val three = image("area-three.png")
    val four  = image("area-four.png")
    val five  = image("area-five.png")
    val six   = image("area-six.png")
  }

  object Explore {
    val explore       = image("explore")
    val complete      = image("explore-complete")
    val settlement    = image("explore-settlement")
    val earnReward    = image("explore-earn-reward")
    val exitAdventure = image("explore-exit-adventure")
    val getPrize      = image("explore-get-prize")
  }

  object Adventure {
    val needSurvey = image("need-survey.png")

    val start             = Images.start
    val adventure         = image("adventure.png")
    val grouping          = image("adventure-grouping.png")
    val totalTurn         = image("adventure-total-turn.png")
    val mpEmpty           = image("adventure-mp-empty.png")
    val navigateCondition = image("adventure-navigate-condition.png")
    val selectA           = image("select-a.png")
  }

  object Wdj {
    val wuDouJi     = image("wdj.png")
    val shenShen    = image("wdj-sen_shen.png")
    val matchBattle = image("wdj-match-battle.png")
    val fightResult = image("wdj-fight-result.png")
  }

  object Tx {
    val fightResult = image("tx-fight-result.png")
    val next        = image("tx-next.png")
    val reward      = image("tx-reward.png")
    val group       = image("tx-group.png")
    val result      = image("tx-result.png")
    val reset       = image("tx-reset")

    val _1  = image("tx-1.png")
    val _2  = image("tx-2.png")
    val _3  = image("tx-3.png")
    val _4  = image("tx-4.png")
    val _5  = image("tx-5.png")
    val _6  = image("tx-6.png")
    val _7  = image("tx-7.png")
    val _8  = image("tx-8.png")
    val _9  = image("tx-9.png")
    val _10 = image("tx-10.png")
    val _11 = image("tx-11.png")
    val _12 = image("tx-12.png")
    val _13 = image("tx-13.png")
    val _14 = image("tx-14.png")
    val _15 = image("tx-15.png")
  }

  def image(s: String) = {
    val fileName = Some(s)
      .map(e => if(e.endsWith(".png")) e else s"$e.png")
      .map(e => if(e.startsWith("images-")) e else s"images-goal/$e")
      .get

    require(File(fileName).exists, fileName + " is not exists")
    models.Image(File(fileName).pathAsString)
  }
}
