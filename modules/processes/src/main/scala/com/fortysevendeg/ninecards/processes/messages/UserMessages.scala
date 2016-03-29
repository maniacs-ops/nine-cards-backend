package com.fortysevendeg.ninecards.processes.messages

object UserMessages {

  case class LoginRequest(
    email: String,
    androidId: String,
    tokenId: String)

  case class LoginResponse(
    apiKey: String,
    sessionToken: String)

}