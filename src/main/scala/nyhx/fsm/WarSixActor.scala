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

