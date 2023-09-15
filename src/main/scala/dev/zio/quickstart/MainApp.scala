package dev.zio.quickstart

import zio.ZIO.readFile
import zio._

import java.io.{File, FileInputStream, IOException}
import zio._
import zio.stream.{Stream, ZStream}

import java.net.URL
import java.nio.charset.StandardCharsets

object MainApp extends ZIOAppDefault {

  def program(args: Chunk[String]): ZIO[ServiceA with ServiceB, IOException, Unit] = {
    for {
      result <- args.headOption match {
        case None => Console.printLine("input empty")
        case Some(s) if s.length == 1 => ServiceA.m(s.length)
        case Some(s) => ServiceB.m(s.length)
      }
      _ <- Console.printLine(s"Result:[$result]")
    } yield ()
  }


  def run = {
    getArgs.map(args => program(args))
      .flatMap(_.provide(
        DependencyAImpl.layer,
        ServiceAImpl.layer,
        ServiceBImpl.layer
      ))

  }

}


trait DependencyA {
  def service(i: Int): UIO[Int]
}

object DependencyA {
  def service(i: Int): ZIO[DependencyA, Nothing, Int] =
    ZIO.serviceWithZIO(_.service(i))
}

case class DependencyAImpl() extends DependencyA() {
  override def service(i: Int): UIO[Int] = ZIO.succeed(i)
}

object DependencyAImpl {
  val layer: ZLayer[Any, Nothing, DependencyA] =
    ZLayer {
      for {
        _ <- ZIO.log("DependencyAImpl.layer")
      } yield DependencyAImpl()
    }
}

trait ServiceA {
  def m(i: Int): UIO[String]
}

object ServiceA {
  def m(i: Int): ZIO[ServiceA, Nothing, String] =
    ZIO.serviceWithZIO(_.m(i))
}

case class ServiceAImpl(dependencyA: DependencyA) extends ServiceA {
  override def m(i: Int): UIO[String] = dependencyA.service(i).map(_ => s"Compute by ${this.getClass}")
}

object ServiceAImpl {
  val layer: ZLayer[DependencyA, Nothing, ServiceA] =
    ZLayer {
      for {
        dependencyA <- ZIO.service[DependencyA]
        _ <- ZIO.log("ServiceAImpl.layer")
      } yield ServiceAImpl(dependencyA)
    }
}

trait ServiceB {
  def m(i: Int): UIO[String]
}

object ServiceB {
  def m(i: Int): ZIO[ServiceB, Nothing, String] =
    ZIO.serviceWithZIO(_.m(i))
}

case class ServiceBImpl() extends ServiceB {
  override def m(i: Int): UIO[String] = ZIO.succeed(s"Compute by ${this.getClass}")
}

object ServiceBImpl {
  val layer: ZLayer[Any, Nothing, ServiceB] =
    ZLayer {
      for {
        _ <- ZIO.log("ServiceBImpl.layer")
      } yield ServiceBImpl()
    }
}