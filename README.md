# 盟新的scala攻略;用scala能做哪些好玩的事-给游戏写辅助

## 初衷/目的
- **核心目的: scala 入门教程,给各位盟新一个实例项目,和可以练手的项目(给自己喜欢的手游写辅助)**

- 按键精灵语法辣鸡,找图功能辣鸡
### 原理

- 在手机或模拟器上运行按键精灵
- 通过按键精灵 截取屏幕保存到文件
- 在将文件发送(通过`curl`)到服务器端进行分析处理(如,找图,找色),决定要执行那些命令(如:点击(X,Y)...)
- 然后在按键精灵上执行这些命令
- 如此往复,周而复始

### 声明

- 此份攻略得分服务器端使用`scala`实现,当然你也可以使用自己熟悉的语言实现;
- 此项目和文档供且仅供学习使用,禁止任何人将其挪威他用
- 此文使用`XXXX`游戏作示例,因为逻辑简单,实现简单


### 目标/效果
![goal](doc/war.gif)

### 准备 - android

- 一些用到的软件 https://pan.baidu.com/s/1kVDw5KV
- 默认分辨率**540 * 960**

- curl
copy `curl`(在项目`android/curl`已经准备好了) 到你的手机或模拟器的`/sdcard`(即sd卡)去

### 准备 - 按键精灵(吾曾尝试使用screencap 截取屏幕,然效果不甚理想,故退而使用按键精灵)

- 安装按键精灵android到手机或模拟器上
- 安装按键精灵手机版到电脑上
- 新建一个脚本
- 将[ajjl-script](android/ajjl.script) 复制粘贴进去,**记得将ip换成自己机器的ip**
- 点击`调试`按钮 应该看到 log : `get result failure or result action is empty`

### 安装python3.6

- 推荐:[andconda](https://www.anaconda.com/wnload/)
- 安装openvc 执行命令 `pip install opencv-python`
- 安装[jep](https://github.com/ninia/jep) : `pip install jep`
- wind 用户将`<andconda 安装目录>\Lib\site-packages\jep` 添加到环境变量中去
- 如果在不记得andconda的安装目录的话,可以在C盘搜索 `Anaconda3` 说不定能找到)


## serivce implement 
 - 实现使用的是scala,
 - 本文假设各位已经有了scala开发环境  
 - 以及能正常阅读理解scala代码



### 实现找图功能

会在scala中通过jep调用此段代码,不感兴趣的小伙伴可以忽视
可以在jupyter中实验这个功能[see](python-script/FindPic.ipynb)

```python
import cv2
import numpy as np

def jvm_find_pic(original, goal):
    '''
    goal : 目标图片 文件路径
    original : 原始图片 文件路径
    '''

    # read image 
    # node : image  的维度是 (width * high * channel) 
    # node : imread 的维度是 (high * width * channel)
    if (type(goal) is str):
        goal = cv2.imread(goal)
    if (type(original) is str):
        original = cv2.imread(original)
    
    # 计算原始图片中每个点与目标图片的相似度
    result = cv2.matchTemplate(goal, original, cv2.TM_CCOEFF_NORMED)

    # 得到相似度最大的index即坐标
    point = np.unravel_index(result.argmax(), result.shape)
    return str((np.max(result), point[1], point[0]))
```
来让我们在scala 中调用它
```scala
//和python交户的lib
val jep = new Jep(false)
val regex = "\\(([0-9|.]+), ?([0-9]+), ?([0-9]+)\\)".r

def findPic(originalName:String,goalName:String) = 
jep.getValue(s"jvm_find_pic('$originalName','$goalName')") match {
  case regex(sim, x, y) => (sim.toDouble, x.toInt, y.toInt)
}
```

试一下能正常运行,让我们把他封装起来  
首先定义个`GoalImage` 和 `OriginalImage` 免得一步小心就传错参数
```scala
class Image(__name: String)
case class GoalImage(__name: String) extends Image(__name)
case class OriginalImage(__name: String) extends Image(__name)
```
然后呢,让我想想,我们希望设计一个`Build`模式如 `Build.withOriginal().withGoal().withThreshold()`;  (node `Threshold`:相似度大于这个值,就认为找到了)  
但是同时又希望`Original`和`Goal` 是必传的,如果没有都赋值的话,就编译错误
其他的属性不做要求(如`Threshold`)没有就给个默认值  

既然要控制编译通过和不通过,那意味着我们要把这些信息放进类型中  
不妨让我们先定义这些类型,稍后再想办法利用起来
```scala
  trait Image
  trait Original extends Image
  trait Goal extends Image
  trait Nothing extends Image
```

不妨先想想`Build`大致长什么样子
```scala
trait FindPicBuild{
  def withGoal(goal: GoalImage)
  def withOriginal(original: OriginalImage)
}
```
嗯,现在怎么办呢,看看我们的目标,我们希望在调用`withGoal`的返回类型中包含`Goal`这个信息大概就是`def withGoal(..):FindPicBuild with Goal`  
最简单的办法就是
```scala
def withGoal(..) = new FindPicBuild with Goal
def withOrignal(..) = new FindPicBuild with Original
```  
嗯,不过`FindPicBuild().withGoal().withOrignal()`返回的类型是`FindPicBuild with Original` 不是我们期望的`FindPicBuild with Goal with Original`  
看来我们需要用别的方法传递这些信息,`scala`中直接和类型相关的概念有`泛型`和`type` 不妨抛个硬币;;是正面好让我们试试泛型  
先看看我们期望的形式
```scala
trait FindPicBuild[Arr]{
  def withGoal(goal: GoalImage):FindPicBuild[Arr with Goal]
  def withOriginal(original: OriginalImage):FindPicBuild[Arr with Orignal]
}
```
在看看`FindPicBuild[Nothing]().withGoal().withOrignal()`返回的类型
- `FindPicBuild[Nothing]()` 返回 `FindPicBuild[Nothing]`
- `.withGoal()` 返回 `FindPicBuild[Nothing with Goal]`
- `.withOrignal()` 返回 `FindPicBuild[Nothing with Goal with Orignal]`  

完美正是我们所想要的,虽然不知为何多了个`Nothing`不过把头埋进沙子里就看不见问题了  
那怎么实现呢,在`new`的时候`with`进去就行了
```scala
trait FindPicBuild[Arr]{
  def withGoal(goal: GoalImage) :FindPicBuild[Arr with Goal]= 
    new FindPicBuild[Arr with Goal]
  def withOriginal(original: OriginalImage):FindPicBuild[Arr with Original] = 
    new FindPicBuild[Arr with Original]
}
```
终于实现好了,来给它加一个`run`方法,调用之前实现好了的`findPic`功能
```scala
case class Point(x: Int, y: Int)
case class IsFindPic(topLeftPoint: Point) extends FindPicResult
case class NoFindPic() extends FindPicResult

//implicit class可以为已有的class添加额外的方法
//implicit x: Arr <:< Original with Goal 以为这Arr 必须是Original with Goal的子类型否则编译不通过,正如我们所预期
implicit class WithRun[Arr <: Image](findPicBuild: FindPicBuild[Arr])(implicit x: Arr <:< Original with Goal) {
    def run() = {
      val original = findPicBuild.original.get
      val goal = findPicBuild.goal.get
      val (similarity, topLeftPoint) = {
        val originalName = original.name.replaceAll("\\\\", "/")
        val goalName = goal.name.replaceAll("\\\\", "/")
        val result = findPic(originalName,goalName)
        val (max, x, y) = result
        max -> Point(x, y)
      }
      
      if(similarity > threshold)
        IsFindPic(topLeftPoint)
      else
        NoFindPic()
    }
  }
```
完整的代码在[FindPic](src/main/scala/utensil/FindPic.scala)

### 通过akka http 实现[http service](src/main/scala/http/HttpService.scala) 接受按键精灵端发送过来的图片

盟新们先花几分钟看看`akka`的[文档](http://jasonqu.github.io/akka-doc-cn/2.3.6/scala/book/index.html)

```scala
仅仅一张图片
case class ClientRequest(image: Image)


//actor将是需要我们实现的,暂时忽视它
val actor: ActorRef = system.actorOf(Props(new ClientActor()))

// 这是一个post 请求
val route = post(
     // url 路径 为 scala/ajjl
      path(PathMatcher("scala") / "ajjl") {
        //接受上传过来的文件
        uploadedFile("screen") { case (fileInfo, jfile) =>
          //上传过来的问会被保存到一个临时文件中,将它copy到我们想要的目录
          val file = File("screen.png")
          File(jfile.getAbsolutePath).copyTo(file, true)
          
          //将图片发送给actor,然后将返回的结果转成json
          val feature = actor
            .ask(ClientRequest(Image(file.pathAsString))).mapTo[Commands]
            .map(_.seq.map(_.toJsonString).mkString(";"))
          //将结果返回client
          onComplete(feature) {
            case Success(x) => complete(x)
            case Failure(x) =>
              x.printStackTrace()
              System.exit(-1)
              ???
          }
        }
      })
```

好一个简单的 `route` 已经实现了

[collectRequestInfo](src/main/scala/http/CollectHttpRequest.scala) 是一个用来收集request log 的函数长成这样,**不感兴趣可以直接忽视**
```scala
//这个在akka http 中已经定义好了,我们就看看
type Route = RequestContext ⇒ Future[RouteResult]

def collectRequestInfo(route: Route): Route = (context: RequestContext) => {
  val startTime = System.currentTimeMillis()
  route.andThen { rt: Future[RouteResult] =>
    rt.onComplete {
      case Success(e)                       =>
        logger.info(...)
      case Failure(e)                       =>
        logger.info(...)
    }
    rt
  }(context)
```

然后将其绑定到9898 端口就可以  
```scala
val http = Http().bindAndHandle(collectRequestInfo(route), "0.0.0.0", 9898)
```
是不是很简单呢  
根据传统顺便加个`hello world`
```scala
val hello_world = get(path("hello")(complete("hello world")))
```
然后把上面的`http = Http(...)`换成:
```scala
val http = Http().bindAndHandle(collectRequestInfo(route ~ hello_world), "0.0.0.0", 9898)
```
访问下 `http://127.0.0.1:9898/hello`

### 实现 `ClientActor`

让我们实现[ClientActor](src/main/scala/nyhx/ClientActor.scala)(ps:实际实现使用了`FSM`进行复杂的逻辑控制) 它将进行逻辑控制,例如出征10就看看任务列表...,不过为了简单还是只实现一个功能就是出征(`war`)
```scala
class ClientActor() extends Actor {
  val logger         = LoggerFactory.getLogger("client-actor")
  var work: ActorRef = context.system.actorOf(Props(new WarActor()))


  override def receive = {
    case x@ClientRequest(screen) =>
      logger.debug(s"receive screen file :${x.image.name}")
      //就做一件事,把消息发个WarActor 
      work.forward(x)
  }
}
```
### 实现WarActor

不等等先,既然Actor之间要发送消息,我们应该先定义好消息的类型,那些是来,那些是回,不然会一团糊,不妨定义  
`httpSerivce` -> `ClientActor` 只发送 `ClientRequest`  
`httpSerivce` <- `ClientActor` 只发送 `Commands`//让按键精灵执行的命令  

`ClientActor` -> `WarActor` 只发送 `ClientRequest`  
`WarActor` -> `ClientActor` or `httpSerivce` 只发送 `Commands`

定义大致如下
```scala
case class ClientRequest(image: Image)

trait Command
case class TapCommand(x: Int, y: Int, action: String = "tap") extends Command
case class DelayCommand(time: Int, action: String = "delay") extends Command
case class Commands(seq: Command*)

```
好准备完成,让我们确定需求先,战斗流程大致如下
- 先点击`冒险`
- 在选择目标地图
- 调整队伍 -> 点击开始
- 开始战斗
- 等待战斗结束
- 返回

在`Actor`中我们如何控制`Actor`的行为呢;最常用的做法是用一个`var status = ...`  
想想一下伪代码:
```
if (status is init) and (find adventure) then 
  touch it 
  set status = finish touch adventure
else 
  ??? 
if (status is finish touch adventure ) and (find goal map) then 
  go to it
  set status = finish go to goal map 
else 
  ???
if find ....
```
不行恶心死我了,即繁琐又容易出错,还是让我们另寻她路  
有没有什么办法让`Actor`自己记住`status`而不用我们手动更新和判断,翻翻文档...翻...翻...翻;找到了  
有`become/unbecome` 和 `FSM` ; 扔个硬币是正面,让我们用`become`实现  

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

---
# FSM 
在来看看FSM  [akka 的fsm 文档](http://jasonqu.github.io/akka-doc-cn/2.3.6/scala/book/chapter3/07_fsm.html)  
目标->劝退:
![dismissed](doc/dismissed.gif)

看看伪代码
```
goto gruen
touch yzw
touch dismissed
touch select student
select student
find determine
if is find then
  touch it
  touch determine sure
  goto select student
else
  return
```
不难发现,大致可以分成两个部分
- 场景移动
- 劝退逻辑

首先实现场景移动  
先定义一个辅助`FindActor`,有两种模式 
- 找图并且点击
- 等到莫图出现

先定义`Status`
```scala
trait BaseStatus
trait BaseData
object NoData extends BaseData

object FindActor {
  trait Status extends BaseStatus

  object Touch extends Status

  object WaitFind extends Status

  object FailureNoFind extends Status

  object Success extends Status
}
```

好在来看看实现
```scala
class FindActor(status: FindActor.Status,
                findPicBuild: ClientRequest => FindPicBuild[FindPicBuild.Request])
              extends FSM[FindActor.Status, FindActor.Condition]{

  //当当前状态为Touch的时候
  when(Touch) {
    case Event(c: ClientRequest, _) =>
      val goal = findPicBuild(c).goal.get.simpleName
      findPicBuild.run(c) match {
        case NoFindPic()      => 
          //如果没找到图片就 go to fail
          goto(FailureNoFind).replying(Commands())
        case IsFindPic(point) =>
          logger.info(s"($goal) is find; touch")
          //找到了就goto success
          goto(Success).replying(Commands().tap(point))
      }
  }
}
```
恩,代码正常运行,...不过仔细思考一下就会发现许多问题
- 如果每个find and touch 操作是一个actor 的话,那么别的操作是不是应该也由一个actor实现
- 如果有多个 find and touch 那么就会有多个actor,该如何执行和控制呢
- 回复 `Commands` 有哪个(调用者还是被跳用者)actor 执行呢
- actor 的创建和销毁由谁负责
- ...

经过在下的深思熟虑作出如下约定
- 任何操作尽可能地包装成actor
- 定义一个`ExecWorkActor`执行多个顺序相连的actor
- 每当一个actor完成了任务之后会向父节点发送`TaskFinish`
- 谁创建谁销毁
- `Commands`的回复统一由被调用的actor执行回复
- 使用`Props`传递actor 动作

好来看看`ExecWorkActor`功能很简单:
```scala
class ExecWorkActor(val prosp: Seq[Props]) extends Actor {
  
  private var workSeq = prosp.map(context.actorOf)
  
  override def receive: Receive = {
    case c: ClientRequest =>
      workSeq.head forward c
    case TaskFinish       =>
      if(workSeq.tail.isEmpty)
        context.parent ! TaskFinish
      else {
        workSeq = workSeq.tail
      }
  }
}

object ExecWorkActor {
  def apply(seq: Props*): Props = Props(new ExecWorkActor(seq))
}
```
在个FindActor提供个辅助方法
```scala
object FindActor{
  type Func = ClientRequest => FindPicBuild[FindPicBuild.Request]
  def touch(f: Func) = Props(new FindActor(Touch,f))
  def waitFind(f: Func) = Props(new FindActor(WaitFind))
}
```

定义一个moveActor 负责场景移动
```scala
  def moveActors() = context actorOf ExecWorkActor(
    FindActor.touch(Find(Images.YuanZiWu.yuanZiWu)),
    FindActor.touch(Find(Images.YuanZiWu.dismissed))
  )
```