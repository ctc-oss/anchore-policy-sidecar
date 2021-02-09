package com.ctc.g2w

import com.ctc.g2w.git.Git
import spray.json._
import zio.stream.ZStream
import zio.{IO, Ref, ZIO}

import java.nio.file.{Files, Path}
import scala.annotation.tailrec

object greylist {
  def readSingle(): ZIO[Git, Throwable, Greylist] =
    for {
      path <- Git.dir().map(_.resolve("redhat/ubi/ubi8/ubi8.greylist"))
      grey <- IO(Files.readAllBytes(path)).map(b => new String(b))
    } yield grey.parseJson.convertTo[Greylist]

  def read(path: Path): ZIO[Any, Throwable, Greylist] =
    IO(Files.readAllBytes(path)).map(b =>
      new String(b))
      .map(x =>
      x.parseJson.convertTo[Greylist]
    )

  def readAll(): ZStream[Git, Throwable, Greylist] =
    ZStream.fromIteratorEffect(Git.dir().map(greylistPaths)).mapM(read)

  def readAll(root: Path): ZStream[Any, Throwable, Greylist] =
    ZStream.fromIterator(greylistPaths(root)).mapM(read)

  def full(root: Path)=   {
    for {
      m <- Ref.make(Map.empty[String, Greylist])
      walk = for {
        _ <- readAll(root).foreach(gl => m.update(_ + (gl.image -> gl)))
      } yield ()
      _ <- walk
      r <- m.map(mm => mm.map { kv =>
        @tailrec
        def walkHierarchy(optGl: Option[Greylist], vulns: List[Vulnerability]): List[Vulnerability] = {
          if(optGl.isEmpty) vulns
          else {
            val gl = optGl.get
            if (gl.parent.isEmpty)
              vulns ++ gl.whitelisted_vulnerabilities
            else {
              val resolvedParent = mm.get(gl.parent.get)
              if(resolvedParent.isEmpty) println(s"WARNING: [${gl.image}] Parent ${gl.parent} is not found")
              walkHierarchy(resolvedParent, vulns ++ gl.whitelisted_vulnerabilities)
            }
          }
        }
        val all = walkHierarchy(Some(kv._2), List.empty)
        kv._2.copy(whitelisted_vulnerabilities = all)
      }).get
    } yield r
  }

  case class Vulnerability(vulnerability: String)
  object Vulnerability extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[Vulnerability] = jsonFormat1(Vulnerability.apply)
  }

  case class Greylist(image_name: String, image_tag: String, image_parent_name: String, image_parent_tag: String, whitelisted_vulnerabilities: List[Vulnerability])
  object Greylist extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[Greylist] = jsonFormat5(Greylist.apply)

    implicit class RichGreylist(gl: Greylist) {
      val image: String = s"${gl.image_name}:${gl.image_tag}"
      val parent: Option[String] = (gl.image_parent_name, gl.image_parent_tag) match {
        case ("", _) => None
        case (i, "") => Some(i)
        case (i, t) => Some(s"$i:$t")
      }
    }
  }

  private def greylistPaths(repo: Path): Iterator[Path] = {
    import better.files._
    repo.toFile.toScala.glob("**.greylist").map(_.toJava.toPath)
  }
}
