package nyhx.fsm

import nyhx.Points

object WarSixActor {
  def four_b(num: Int) = SeqenceActor(
    WarHelper.goToAdventure(),
    WarHelper.goToWarArea(Points.Area.six, 4),
    ReplaceActor(num, SeqenceActor(
      WarHelper.warReady(),
      WarHelper.warPoint(Points.Adventure.Six.Four.b),
      WarHelper.warEarlyEnd()
    ))
  )
}

object WarTowActor {
  def tow_b(num: Int) = SeqenceActor(
    WarHelper.goToAdventure(),
    WarHelper.goToWarArea(Points.Area.two, 1),
    ReplaceActor(num, SeqenceActor(
      WarHelper.warReady(),
      WarHelper.warPoint(Points.Adventure.Two.One.c),
      WarHelper.warPoint(Points.Adventure.Two.One.f),
      WarHelper.warPoint(Points.Adventure.Two.One.g),
      JustActor.justDelay(2000)
    ))
  )

}

object WarOneActor {
  def four_boss(num: Int) = SeqenceActor(
    WarHelper.goToAdventure(),
    WarHelper.goToWarArea(Points.Area.one, 4),
    ReplaceActor(num, SeqenceActor(
      WarHelper.warReady(),
      WarHelper.warPoint(Points.Adventure.One.Four.b),
      WarHelper.warPoint(Points.Adventure.One.Four.c),
      WarHelper.randomPoint(Points.Adventure.One.Four.f),
      WarHelper.warPoint(Points.Adventure.One.Four.g),
      WarHelper.warPoint(Points.Adventure.One.Four.boss),

      JustActor.justDelay(2000)
    ))
  )

  def five_boos(num: Int) = NameProps("five boos", SeqenceActor(
    WarHelper.goToAdventure(),
    WarHelper.goToWarArea(Points.Area.one, 5),
    NameProps("replace", ReplaceActor(num, NameProps("start-war", SeqenceActor(
      WarHelper.warReady(),
      WarHelper.warPoint(Points.Adventure.One.Five.b),
      WarHelper.randomPoint(Points.Adventure.One.Five.c),
      NameProps("condition select", WarHelper.randomPointCheck()
        .onSuccess(NameProps("continue", SeqenceActor(
          WarHelper.warPoint(Points.Adventure.One.Five.d),
          WarHelper.warPoint(Points.Adventure.One.Five.boos)
        )))
        .onFailure(
          WarHelper.warEarlyEnd()
        )
        .build())
    ))))
  ))
}


