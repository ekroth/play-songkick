/* Copyright (c) 2015 Andrée Ekroth.
 * Distributed under the MIT License (MIT).
 * See accompanying file LICENSE or copy at
 * http://opensource.org/licenses/MIT
 */

package com.github.ekroth.songkick

/** Objects corresponding to Songkick's object model.
  */
private[songkick] trait Objects {

  import scala.collection.immutable.Seq
  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  /** Rename json names that are reserved words in Scala.
    *
    * In order to be able to use the Json.writes/reads macro we
    * need to pre- and post-process the JsValues accordingly.
    */
  object TypeNameFix {
    private[this] def replace(in: String, out: String)(json: JsValue): JsValue = json match {
      case x: JsObject => (x \ in) match {
        case y: JsString => (x - in) + (out, y)
        case _ => x
      }
      case x => x
    }

    /** Convert from 'name' to 'keyword'. */
    val afterWrite: JsValue => JsValue = replace("tipe", "type")(_)

    /** Convert from 'keyword' to 'name'. */
    val beforeRead = Reads[JsValue] { js => JsSuccess(replace("type", "tipe")(js)) }
  }

  object DisplayName {
    implicit val DisplayNameWrites = Json.writes[DisplayName].transform(TypeNameFix.afterWrite)
    implicit val DisplayNameTrackReads = Json.reads[DisplayName].compose(TypeNameFix.beforeRead)
  }
  case class DisplayName(displayName: String)

  object MetroArea {
    implicit val MetroAreaWrites = Json.writes[MetroArea].transform(TypeNameFix.afterWrite)
    implicit val MetroAreaTrackReads = Json.reads[MetroArea].compose(TypeNameFix.beforeRead)
  }
  case class MetroArea(uri: String, displayName: String, country: DisplayName, id: Int, lng: Option[Double], lat: Option[Double], state: Option[DisplayName])

  object City {
    implicit val CityWrites = Json.writes[City].transform(TypeNameFix.afterWrite)
    implicit val CityTrackReads = Json.reads[City].compose(TypeNameFix.beforeRead)
  }
  case class City(displayName: String, country: DisplayName, lng: Option[Double], lat: Option[Double])

  object LocationArea {
    implicit val LocationAreaWrites = Json.writes[LocationArea].transform(TypeNameFix.afterWrite)
    implicit val LocationAreaTrackReads = Json.reads[LocationArea].compose(TypeNameFix.beforeRead)
  }
  case class LocationArea(city: City, metroArea: MetroArea)

  object Date {
    implicit val DateWrites = Json.writes[Date].transform(TypeNameFix.afterWrite)
    implicit val DateTrackReads = Json.reads[Date].compose(TypeNameFix.beforeRead)
  }
  case class Date(time: Option[String], date: String, datetime: Option[String])

  object Location {
    implicit val LocationWrites = Json.writes[Location].transform(TypeNameFix.afterWrite)
    implicit val LocationTrackReads = Json.reads[Location].compose(TypeNameFix.beforeRead)
  }
  case class Location(city: String, lng: Option[Double], lat: Option[Double])

  object Venue {
    implicit val VenueWrites = Json.writes[Venue].transform(TypeNameFix.afterWrite)
    implicit val VenueTrackReads = Json.reads[Venue].compose(TypeNameFix.beforeRead)
  }
  case class Venue(id: Int, displayName: String, uri: String, lng: Option[Double], lat: Option[Double], metroArea: MetroArea)

  object Artist {
    implicit val ArtistWrites = Json.writes[Artist].transform(TypeNameFix.afterWrite)
    implicit val ArtistTrackReads = Json.reads[Artist].compose(TypeNameFix.beforeRead)
  }
  case class Artist(uri: String, displayName: String, id: Int/*, identifier: Seq[JsValue]*/)

  object Performance {
    implicit val PerformanceWrites = Json.writes[Performance].transform(TypeNameFix.afterWrite)
    implicit val PerformanceTrackReads = Json.reads[Performance].compose(TypeNameFix.beforeRead)
  }
  case class Performance(artist: Artist, displayName: String, billingIndex: Int, id: Int, billing: String)

  object Event {
    implicit val EventWrites = Json.writes[Event].transform(TypeNameFix.afterWrite)
    implicit val EventTrackReads = Json.reads[Event].compose(TypeNameFix.beforeRead)
  }
  case class Event(id: Int, tipe: String, uri: String, displayName: String, start: Date, performance: Seq[Performance], location: Location, venue: Venue, popularity: Double)


  /** The Json reads/writes macro can't handle generics very well. */
  object ResultsPage {
    // scalastyle:off method.name

    implicit def ResultsPageWrites[T : Writes]: Writes[ResultsPage[T]] = (
      (JsPath \ "totalEntries").write[Int] and
        (JsPath \ "perPage").write[Int] and
        (JsPath \ "page").write[Int] and
        (JsPath \ "results").write[Map[String, Seq[T]]]
    )(unlift(ResultsPage.unapply[T])).transform(TypeNameFix.afterWrite)

    implicit def ResultsPageReads[T : Reads]: Reads[ResultsPage[T]] = (
      (JsPath \ "totalEntries").read[Int] and
        (JsPath \ "perPage").read[Int] and
        (JsPath \ "page").read[Int] and
        (JsPath \ "results").read[Map[String, Seq[T]]]
    )(ResultsPage.apply[T] _).compose(TypeNameFix.beforeRead)

    def emptyAt(page: Int): ResultsPage[Nothing] = ResultsPage(0, 0, page, Map.empty)
    val empty = emptyAt(1)
  }
  case class ResultsPage[+T](totalEntries: Int, perPage: Int, page: Int, results: Map[String, Seq[T]])

}
