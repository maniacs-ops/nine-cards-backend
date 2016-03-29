package com.fortysevendeg.ninecards.api.messages

object UserMessages {

  case class ApiLoginRequest(
    email: String,
    androidId: String,
    tokenId: String)

  case class ApiLoginResponse(
    apiKey: String,
    sessionToken: String)
}