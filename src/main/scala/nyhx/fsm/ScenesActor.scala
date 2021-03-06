package nyhx.fsm

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.{ClientRequest, Commands, NoFindPicException}
import nyhx.{Find, Images}
import nyhx.Find.FindPicBuildingWithRun
import org.slf4j.LoggerFactory
import utensil.{IsFindPic, NoFindPic}


object ScenesActor {


  def returns =
    Props(new MyFsmAct {
      exec(c =>
        Find(Images.returns).run(c) -> Find(Images.determine).run(c) match {
          case (_, IsFindPic(point))      =>
            log.info("in return find determine -> tap it ")
            Build.stay().replying(Commands().tap(point).delay(2500)).build()
          case (IsFindPic(point), _)      =>
            log.info("find return -> tap it ")
            Build.stay().replying(Commands().tap(point).delay(2500)).build()
          case (NoFindPic(), NoFindPic()) =>
            log.info("[finish] no find return ")
            Build.goto(Finish).replying(Commands()).build()
        })
    })

  def goToRoom = Props(new MyFsmAct {
    exec { c =>
      val adventure = Find(Images.Adventure.adventure).run(c)
      val gotoRoom = Find(Images.returns_room).run(c)
      (adventure.isFind, gotoRoom.isFind) match {
        case (false, true)  => stay().replying(Commands().tap(gotoRoom.point).delay(2500))
        case (true, _)      => goto(Finish).replying(Commands())
        case (false, false) => goto(Error).replying(Commands())
      }
    }
  })

  def goToGruen = Props(new MyFsmAct {
    exec(c =>
      Find(Images.returns_gakuen).run(c) match {
        case IsFindPic(point) =>
          log.info("find return gakuen -> assum is room")
          goto(Finish).replying(Commands().tap(point).delay(2500))
        case NoFindPic()      =>
          log.info("no find return gakuen -> assum is gakuen")
          goto(Finish).replying(Commands())
      })
  })

  def goToAdventure() = SeqenceActor(
    ScenesActor.returns,
    ScenesActor.goToRoom,
    FindActor.touch(Find(Images.Adventure.adventure)),
    FindActor.waitIsFind(Find(Images.Adventure.grouping)),
  )

  def goToExport() = SeqenceActor(
    returns,
    goToRoom,
    FindActor.touch(Find(Images.Explore.explore)),
    FindActor.waitIsFind(Find(Images.returns)),
  )

}
