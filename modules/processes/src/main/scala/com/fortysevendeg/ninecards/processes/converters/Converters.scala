package com.fortysevendeg.ninecards.processes.converters

import java.sql.Timestamp

import com.fortysevendeg.ninecards.processes.messages.ApplicationMessages._
import com.fortysevendeg.ninecards.processes.messages.InstallationsMessages._
import com.fortysevendeg.ninecards.processes.messages.SharedCollectionMessages._
import com.fortysevendeg.ninecards.processes.messages.UserMessages.LoginResponse
import com.fortysevendeg.ninecards.services.common.NineCardsConfig.defaultConfig
import com.fortysevendeg.ninecards.services.free.domain.GooglePlay.{
  AppsInfo,
  AppInfo ⇒ AppInfoServices,
  AuthParams ⇒ AuthParamServices
}
import com.fortysevendeg.ninecards.services.free.domain.{
  Installation ⇒ InstallationServices,
  SharedCollection ⇒ SharedCollectionServices,
  User ⇒ UserAppServices
}
import com.fortysevendeg.ninecards.services.persistence.SharedCollectionPersistenceServices.{
  SharedCollectionData ⇒ SharedCollectionDataServices
}
import org.http4s.Uri
import org.joda.time.DateTime

object Converters {

  implicit def toJodaDateTime(timestamp: Timestamp): DateTime = new DateTime(timestamp.getTime)

  implicit def toTimestamp(datetime: DateTime): Timestamp = new Timestamp(datetime.getMillis)

  def generateSharedCollectionLink(publicIdentifier: String) =
    Uri.fromString(defaultConfig.getString("ninecards.sharedCollectionsBaseUrl")) map { uri ⇒
      uri./(publicIdentifier).renderString
    } fold (
      error ⇒ "", //TODO: Maybe we should return an error if the url is malformed
      uri ⇒ uri
    )

  def toLoginResponse(info: (UserAppServices, InstallationServices)): LoginResponse = {
    val (user, _) = info
    LoginResponse(
      apiKey       = user.apiKey,
      sessionToken = user.sessionToken
    )
  }

  def toCreateCollectionResponse(
    collection: SharedCollectionServices,
    packages: List[String]
  ): CreateCollectionResponse =
    CreateCollectionResponse(
      toSharedCollection(collection, packages)
    )

  def toUpdateInstallationResponse(installation: InstallationServices): UpdateInstallationResponse =
    UpdateInstallationResponse(
      androidId   = installation.androidId,
      deviceToken = installation.deviceToken
    )

  implicit def toSharedCollectionDataServices(
    data: SharedCollectionData
  ): SharedCollectionDataServices =
    SharedCollectionDataServices(
      publicIdentifier = data.publicIdentifier,
      userId           = data.userId,
      publishedOn      = data.publishedOn,
      description      = data.description,
      author           = data.author,
      name             = data.name,
      installations    = data.installations.getOrElse(0),
      views            = data.views.getOrElse(0),
      category         = data.category,
      icon             = data.icon,
      community        = data.community
    )

  def toSharedCollection(
    collection: SharedCollectionServices,
    packages: List[String]
  ): SharedCollection =
    SharedCollection(
      publicIdentifier = collection.publicIdentifier,
      publishedOn      = collection.publishedOn,
      description      = collection.description,
      author           = collection.author,
      name             = collection.name,
      sharedLink       = generateSharedCollectionLink(collection.publicIdentifier),
      installations    = collection.installations,
      views            = collection.views,
      category         = collection.category,
      icon             = collection.icon,
      community        = collection.community,
      packages         = packages
    )

  def toSharedCollectionWithAppsInfo(
    collection: SharedCollection,
    appsInfo: List[AppInfoServices]
  ): SharedCollectionWithAppsInfo =
    SharedCollectionWithAppsInfo(
      collection = collection,
      appsInfo   = appsInfo map toAppInfo
    )

  def toAppInfo(info: AppInfoServices): AppInfo =
    AppInfo(
      packageName = info.packageName,
      title       = info.title,
      free        = info.free,
      icon        = info.icon,
      stars       = info.stars,
      downloads   = info.downloads,
      category    = info.categories.headOption getOrElse ""
    )

  def toCategorizeAppsResponse(info: AppsInfo): CategorizeAppsResponse = {
    val (appsWithoutCategories, apps) = info.apps.partition(app ⇒ app.categories.isEmpty)

    CategorizeAppsResponse(
      errors = info.missing ++ appsWithoutCategories.map(_.packageName),
      items  = apps map { app ⇒
      CategorizedApp(
        packageName = app.packageName,
        category    = app.categories.head
      )
    }
    )
  }

  def toAuthParamsServices(authParams: AuthParams): AuthParamServices = {
    AuthParamServices(
      androidId    = authParams.androidId,
      localization = authParams.localization,
      token        = authParams.token
    )
  }
}
