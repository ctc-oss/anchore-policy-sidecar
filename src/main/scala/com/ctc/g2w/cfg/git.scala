package com.ctc.g2w.cfg

import zio.config._
import ConfigDescriptor._

object git {
  case class Repo(url: String)

  sealed trait Mode { def name: String }
  case class Ref(name: String) extends Mode
  case class Branch(name: String) extends Mode

  object Mode {
    def apply(mode: String, name: String): Mode = mode match {
      case "ref"    => Ref(name)
      case "branch" => Branch(name)
      case _        => throw new IllegalArgumentException(mode)
    }
    def unapply(m: Mode): Option[(String, String)] = m match {
      case Ref(n)    => Some("ref" -> n)
      case Branch(n) => Some("branch" -> n)
      case _         => None
    }
  }

  val mode: ConfigDescriptor[Mode] =
    (string("mode") |@| string("ref"))(Mode.apply, Mode.unapply)

  val repo: ConfigDescriptor[Repo] = string("repo")(Repo.apply, Repo.unapply)
}
