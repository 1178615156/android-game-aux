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
  - app-debug.apk
  - 模拟器
  - curl

- 默认分辨率**540 * 960**

#### android 准备 
- 方案一
  - 从 https://pan.baidu.com/s/1kVDw5KV 下载app-debug.apk  安装到模拟器上  
  - 填写 PC端的ip 和端口(默认 9898)

- 方案二 - 使用按键精灵
  - curl : copy `curl`到你的手机或模拟器的`/sdcard`(即sd卡)去
  - 安装按键精灵android到手机或模拟器上
  - 安装按键精灵手机版到电脑上
  - 新建一个脚本
  - 将[ajjl-script](doc/ajjl-script) 复制粘贴进去,**记得将ip换成自己机器的ip**
  - 点击`调试`按钮 应该看到 log : `get result failure or result action is empty`

### python
- 方案一
  - 推荐:[andconda](https://www.anaconda.com/wnload/)
- 方案二
  - 下载python3.6并安装 
  - `pip install numpy`
  - `pip install pandas`
- ---分割线---
- 安装openvc 执行命令 `pip install opencv-python`
- 安装[jep](https://github.com/ninia/jep) : `pip install jep`
- win 用户将`<andconda 安装目录>\Lib\site-packages\jep` 添加到环境变量中去
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
// image 是手机端截取的屏幕图片
case class ClientRequest(image: Image)

trait Command
case class TapCommand(x: Int, y: Int, action: String = "tap") extends Command
case class DelayCommand(time: Int, action: String = "delay") extends Command
case class Commands(seq: Command*)
```

让我们确定需求先,战斗流程大致如下
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
不行恶心死了,即繁琐又容易出错,还是让我们另寻她路  
有没有什么办法让`Actor`自己记住`status`而不用我们手动更新和判断,翻翻文档...翻...翻...翻;找到了  
有`become/unbecome` 和 `FSM` ; 扔个硬币是正面,让我们用`become`实现  

---
# FSM 

现在要面对的是如何执行莫一个动作如:
- 找到某张图片并点击
- 等待莫张图片出现
- ...


恩,先想办法实现个Find and touch 
```scala
object FindActor{
  trait Status 

  object Touch extends Status

  object WaitFind extends Status

  object FailureNoFind extends Status

  object Success extends Status
  
  trait Data
  object NoData extends Data
}
import FindActor._
class FindActor(status: FindActor.Status,
                findPicBuild: ClientRequest => FindPicBuild[FindPicBuild.Request])
              extends FSM[FindActor.Status, Data]{

  startWith(status,NoData)
  //当当前状态为Touch的时候
  when(Touch) {
    case Event(c: ClientRequest, _) =>
      findPicBuild.run(c) match {
        case NoFindPic()      => 
          //如果没找到图片就 go to fail
          goto(FailureNoFind).replying(Commands())
        case IsFindPic(point) =>
          //找到了就goto success
          goto(Success).replying(Commands().tap(point))
      }
  }
}
```
恩,代码正常运行如果找到某图的话就点击,否则错误,...不过仔细思考一下就会发现许多问题
- 如果每个find and touch 操作是一个actor 的话,那么别的操作是不是应该也由一个actor实现
- 如果有多个 find and touch 那么就会有多个actor,该如何执行和控制呢
- 回复 `Commands` 有哪个(调用者还是被跳用者)actor 执行呢
- actor 的创建和销毁由谁负责
- ...

经过在下的深思熟虑作出如下约定
- 任何操作尽可能地包装成actor
- 定义一个`SeqenceActor`执行多个顺序相连的actor
- 每当一个actor完成了任务之后会向父节点发送`TaskFinish`
- 谁创建谁销毁
- `Commands`的回复统一由被调用的actor执行回复 即最终的叶子节点的actor
- 使用`Props`传递actor 动作

好来看看`ExecWorkActor`功能很简单:
```scala
class SeqenceActor(val prosp: Seq[Props]) extends Actor {
  
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

object SeqenceActor {
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