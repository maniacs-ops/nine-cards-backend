package com.fortysevendeg.ninecards.services.free.interpreter.impl

import com.fortysevendeg.ninecards.services.free.domain._

class UserPersistenceImpl {

  def addUser(user: User) = user

  def getUserByUserName(username: String) = Option(User())

  def checkPassword(pass: String) = true

  def getUserByUserId(userId: String) =
    Option(
      User(
        id = Option(userId),
        username = Option("Ana"),
        email = Option("ana@47deg.com"),
        sessionToken = Option("asjdfoaijera"),
        authData = Option(AuthData(
          google = Option(GoogleAuthData(
            email = "ana@47deg.com",
            devices = List(
              GoogleAuthDataDeviceInfo(
                name = "aldfa",
                deviceId = "ladf",
                secretToken = "lakjdsflkadf",
                permissions = Nil
              )
            ))
          ))
        )
      )
    )

  def updateInstallation(installation: Installation) =
    Option(
      Installation(
        deviceType = installation.deviceType,
        userId = installation.userId
      ))

}

object UserPersistenceImpl {

  implicit def userPersistenceImpl = new UserPersistenceImpl()
}
