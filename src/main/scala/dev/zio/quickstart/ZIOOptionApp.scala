package dev.zio.quickstart

import zio._

object ZIOOptionApp {


  ZIO.succeed("my String")
    .map(s => (s,s.length))
    .collect("Impossible"){ case (s,len) if len >  2 => s.toUpperCase}

  val maybeId: IO[Option[Nothing], String] = ???

  def getUser(userId: String): IO[Throwable, Option[User]] = ???

  def getTeam(teamId: String): IO[Throwable, Team] = ???

  def myUnsafeJavaMethod(s: String): Int = ???

  val e: Task[Int] = ZIO.attempt(myUnsafeJavaMethod("a"))

  def computeX(s:String):Task[String] = ???
  def computeY(s:String):Task[Int] = ???
  val r: Task[(String, Int)] = computeX("A").zip(computeY("B"))

  def searchUserTeam(userId: String): Task[Option[(User, Team)]] = {
    (for {
      user <- getUser(userId).some
      team <- getTeam(user.teamId).asSomeError
    } yield (user, team)).unsome
  }


  val result: IO[Throwable, Option[(User, Team)]] = (for {
    id <- maybeId

    user <- getUser(id).some
    team <- getTeam(user.teamId).asSomeError
  } yield (user, team)).unsome
}

case class User(userId: String, teamId: String)

case class Team(teamId: String)