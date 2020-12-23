package com.ctc.g2w

object api {
  case class InitError(m: String, c: Throwable) extends RuntimeException(m, c)
}
