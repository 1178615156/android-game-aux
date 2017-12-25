package nyhx

import models.{ClientRequest, GoalImage, Image}
import utensil.{FindPicBuild, FindPicResult, NoFindPic}


trait Find[T] {
  def values :List[FindPicBuild[T]]
  def run(c: ClientRequest): FindPicResult

  def map[To<:T ](f: FindPicBuild[T] => FindPicBuild[To]): Find[To]
}

class FindVal[T <: FindPicBuild.Goal](v: FindPicBuild[T]) extends Find[T] {
  override def run(c: ClientRequest): FindPicResult = v.withOriginal(c.image.toOriginal).run()

  override def map[To<:T](f: FindPicBuild[T] => FindPicBuild[To]): Find[To] = new FindVal[To](f(v))

  override def values: List[FindPicBuild[T]] = v :: Nil
}

class FindOr[T <: FindPicBuild.Goal](l: Find[T], r: Find[T]) extends Find[T] {
  override def run(c: ClientRequest): FindPicResult = {
    lazy val lResult = l.run(c)
    lazy val rResult = r.run(c)
    if(lResult.isFind) lResult
    else if(rResult.isFind) rResult
    else if(rResult.similarity > lResult.similarity) rResult
    else lResult
  }

  override def map[To<:T](f: FindPicBuild[T] => FindPicBuild[To]): Find[To] = new FindOr[To](l.map(f), r.map(f))

  override def values: List[FindPicBuild[T]] = l.values ++ r.values
}

object Find {

  implicit class WithOr[T <: FindPicBuild.Goal](l: Find[T]) {
    def or(r: Find[T]) = new FindOr[T](l, r)
  }

  def apply[T<:FindPicBuild.Goal](fpb: FindPicBuild[T]): FindVal[T] = new FindVal[T](fpb)
  def build(image:Image): FindVal[FindPicBuild.Nothing with FindPicBuild.Goal] = apply(FindPicBuild().withGoal(image.toGoal))
  def apply(image: Image): FindVal[FindPicBuild.Nothing with FindPicBuild.Goal] = build(image)

  implicit class FindPicBuildingWithRun(f: ClientRequest => FindPicBuild[FindPicBuild.Request]) {
    def run(c: ClientRequest): FindPicResult = f(c).run()
  }

}
