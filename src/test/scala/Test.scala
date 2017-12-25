

object Test {

  object Hello {

    trait Image

    trait Goal extends Image

    trait Original extends Image

    trait Nothing extends Image

    def apply(): Hello[Nothing] = new Hello[Nothing](None, None)
  }

  import scala.annotation.implicitNotFound

  import Hello.Goal
  import Hello.Original

  class Hello[_Arr <: Hello.Image](goal: Option[Goal], original: Option[Original]) {
    type Arr = _Arr

    def withGoal(goal: Goal) = new Hello[Arr with Goal](goal = Some(goal), original)

    def withOriginal(original: Original) = new Hello[Arr with Original](original = Some(original), goal = goal)
  }

  new Hello[Hello.Nothing](None, None).withGoal(new Goal {})

  @implicitNotFound("hello not with goal or original")
  def f[Arr <: Hello.Image](hello: Hello[Arr])
                           (implicit x: Arr <:< (Hello.Goal with Hello.Original)) = hello

//  println("hello world")
//    f(new Hello[Hello.Nothing](None, None))
//  f(Hello().withGoal(new Goal {}).withOriginal(new Original {}))
}
