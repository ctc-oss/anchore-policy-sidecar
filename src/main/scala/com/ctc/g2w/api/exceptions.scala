package com.ctc.g2w.api

object exceptions {
  case class InitError(m: String, c: Throwable) extends RuntimeException(m, c)
}
