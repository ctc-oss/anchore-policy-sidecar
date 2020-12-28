package com.ctc.g2w.api

object exceptions {
  case class ReadActivePolicyError() extends Exception
  case class ReadActivePolicyVersionError() extends Exception
}
