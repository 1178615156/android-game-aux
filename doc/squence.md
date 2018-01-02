
```scala
def goToAdventure = {
  case ClientRequest(image) =>
  val result = FindPicBuild().withGoal(Images.adventure).withOrinal(image).run()
  result match {
    case IsFindPic(point) => 
      sender() ! Commands().addTap(point)
      become(selectGoalMap)
    case NoFindPic() => 
      ???
  }
}
def selectGoalMap = ???
```
好一切完美运行让我们大肝出奇迹,肝完这一切.....   

...我才不肝呢,`become`来`become`去,这不正是goto指令么,复杂的`become`可以让我们失去对代码的控制权,
而且也没法保证没个行动都有正常跳用 `sender() ! ...`;而且就算调用了,也可能发错消息了.还是让我们想想这么避免上述的问题吧;  
整理问题如下: 
- 不能让每个函数都能执行`become`我们需要统一集中化对流程的控制
- 每个函数都应该返回`Commands`

让我们回想一下著名的三定律之一:**所有的问题都能通过增加间接层解决,除了**

好让我们增加一层间接层

```scala
sealed trait Result {
  def commands: Commands
}
object Result {
  case class Success(commands: Commands = Commands()) extends Result
  case class Execution(commands: Commands) extends Result
  case class Failure(exception: Exception) extends Result  {
    override def commands: Commands = throw exception
  }
}

trait RecAction extends (ClientRequest => Result)

```
每个动作`RecAction`都应该返回`Result`,这下不会有谁没返回,或者返回错了信息  
那么这么控制流程呢,不用`become`的话 那就在加一层;  
```scala
trait Action {
  def name = ""
}

object Action {
  implicit def rec2action(rec: RecAction): Rec = Rec(rec)
  implicit def sequ2action(sequence: Sequence): Sequ = Sequ(sequence)

  case class Rec(recAction: RecAction) extends Action
  case class Sequ(sequence: Sequence) extends Action

}

trait Patten
object Patten {
  case class Next(action: Action) extends Patten
}

case class Sequence(name: String, actions: Seq[Patten] = Nil){
  def next(recAction: Action) = Sequence(name, actions :+ Patten.Next(recAction))

  val isEnd = actions.isEmpty
  def head = actions.head
  def tail = actions.tail
}
```
定义个`Sequence` 模型,只能线性一个接一个的执行;示例:  
```
val a = RecAction(if find pic A then touch it)
val b = RecAction(if no find pic B then touch it )
val c = RecAction(if find pic C then return)
val sequence = (Sequence()
  next a 
  next (Sequence()
    next b 
    next c 
  )
)
```
如果将`sequence`展开的话就是 `a next b next c` 你会发现这还是线性执行;这也是为什么`Action` 有 `Rec` 和 `Sequ` 两个模式的原因;  
既然有`Sequence`我们需要`run`;那么`run`需要哪些参数才能运行么?
- 首先需要 `sequence:Sequenc` 
- 然后应该需要个 `clientRequest:ClientRequest`才可以提供给`RecAction`运行
- 还应该需要个 `sender`才可以将`RecAction`返回的`Commands`发送回去

那么返回值什么呢;嗯,`run` 应该只执行第一个`Action`,那么剩下的未执行的`Action`怎么办呢,可以将它返回回去不做处理

所以`run`的声明应该如下:
```scala
def run(sequence: Sequence)(clientRequest: ClientRequest, sender: ActorRef): Sequence
```

好了到了实现了:
```scala
def execRecAction(recAction: RecAction) = recAction(clientRequest) match {
  //如果失败了,那就失败了
  case Result.Failure(x)   => throw x
  //如果是Execution,我们会继续执行这个action直到success为止
  //感觉名字叫做`continue` 会更易懂,
  case Result.Execution(x) =>
    sender ! x
    Some(recAction)
  //完事,准备执行下一条指令
  case Result.Success(x)   =>
    sender ! x
    None
}
```


```scala
//定义两个辅助函数
def runByRec(action: RecAction) = {
  val result = execRecAction(action)
  result match {
    case Some(x) => Sequence(sequence.name, Patten.Next(x) +: sequence.tail)
    case None    => Sequence(sequence.name, sequence.tail)
  }
}
def runBySequence(sequ: Sequence) = {
  val result = run(sequ)(clientRequest, sender)
  if(result.isEnd)
    Sequence(sequence.name, sequence.tail)
  else
    Sequence(sequence.name, Patten.Next(result) +: sequence.tail)
}
//主体
val action = sequence.head
action match {
  case Next(Action.Rec(action)) => runByRec(action)
  case Next(Action.Sequ(sequ))  => runBySequence(sequ)
}
```
[完整的代码](src/main/scala/nyhx/sequence/Sequence.scala)

终于完成了

让我们来实现WarAcotr吧

例如看下`Images.returns`这张图片  
![](images-goal/returns.png)

定义一些辅助方法,我们要找的图片[完整代码](src/main/scala/nyhx/sequence/FindAux.scala)
```scala
object Find{
  val returns           = find(Images.returns.toGoal)
  val goToRoom          = find(Images.returns_room.toGoal)
  val adventure         = find(Images.Adventure.adventure.toGoal)
  val grouping          = find(Images.Adventure.grouping.toGoal)
  val start             = find(Images.start.toGoal)

  def find(image: GoalImage) = (clientRequest: ClientRequest) => FindPicBuild()
    .withGoal(image.toGoal)
    .withOriginal(clientRequest.image.toOriginal)
}
```

以及两个拓展函数
```scala
implicit class FindAux (f: ClientRequest => FindPicBuild[FindPicBuild.Request]){

  //常用的模式之一 if find then touch else ???
  def touch = RecAction { implicit c => ???)
  //常用的模式之一 if no find then continue else goto next
  def waitFind = RecAction { implicit c =>???}
}
```
这样这我们就能便捷的调用`Find.returns.touch`表示:找return这张image,如果找到这`touch`否则`error`

来实现to go adventure ; 点击去冒险按钮,开始战斗
```scala
def goToAdventure = (Sequence("goToAdventure")
  next touchReturns
  next goToRoom
  next Find.adventure.touch
  next Find.grouping.waitFind
  )
```
so easy;来做更多
```scala
  def warPoint_B = (Sequence()
      next warReady
      next warPoint(Point(199,199))
      next warEnd
      )

  def warReady = (Sequence("warReady")
    next Find.grouping.touch
    next Find.start.touch
    )

  def warPoint(point: Point) = (Sequence()
    next Find.navigateCondition.waitFind
    next justTap(point, 2000)
    next Find.start.waitFind
    next Find.start.touch
    next waitWarEnd
    next sureWarReward
    )

  def warEnd = (Sequence("warEnd")
    next Find.returns.waitFind
    next Find.returns.touch
    next Find.determine.waitFind
    next Find.determine.touch
    next Find.grouping.waitFind
    )
```
终于完成了:::

最后一步`receive`
```scala
  var sequences = Sequence() next gotoAdventure next warPoint_b 

  override def receive: Receive = {
    case c: ClientRequest =>
      val result = Sequence.run(sequences)(c, sender())
      sequences = result
  }
```

在来点小改进,这样子就不需要`var`了
```scala
val sequences = Sequence() next gotoAdventure next warPoint_b 
def rec(action: Sequence): PartialFunction[Any, Sequence] = PartialFunction { case c: ClientRequest =>
    Sequence.run(action)(c, sender())
  }

def onRec(action: Sequence): Receive =
    rec(action).andThen(action => context.become(onRec(action)))

override def receive: Receive = onRec(sequences)

```