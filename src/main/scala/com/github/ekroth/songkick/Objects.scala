/* Copyright (c) 2015 Andrée Ekroth.
 * Distributed under the MIT License (MIT).
 * See accompanying file LICENSE or copy at
 * http://opensource.org/licenses/MIT
 */

package com.github.ekroth
package songkick

/** Objects corresponding to Songkick's object model.
  */
trait Objects {

  import scala.collection.immutable.Seq

  import spray.json._
  import DefaultJsonProtocol._

  implicit class RichJsonFormat[T](val underlying: JsonFormat[T]) {

    private def fix(in: String, out: String, obj: JsValue): JsValue = obj match {
      case JsObject(fields) => JsObject(fields.map {
        case (`in`, v) => (out, v)
        case x => x
      })

      case x => x
    }

    def withTipe: JsonFormat[T] = new JsonFormat[T] {
      override def write(obj: T): JsValue = fix("tipe", "type", underlying.write(obj))
      override def read(obj: JsValue): T = underlying.read(fix("type", "tipe", obj))
    }
  }

  object DisplayName {
    implicit val displayNameFormat = jsonFormat1(DisplayName.apply).withTipe
  }
  case class DisplayName(displayName: String)

  object MetroArea {
    implicit val metroAreaFormat = jsonFormat7(MetroArea.apply).withTipe
  }
  case class MetroArea(uri: String, displayName: String, country: DisplayName, id: Int, lng: Option[Double], lat: Option[Double], state: Option[DisplayName])

  object City {
    implicit val cityFormat = jsonFormat4(City.apply).withTipe
  }
  case class City(displayName: String, country: DisplayName, lng: Option[Double], lat: Option[Double])

  object LocationArea {
    implicit val locationAreaFormat = jsonFormat2(LocationArea.apply).withTipe
  }
  case class LocationArea(city: City, metroArea: MetroArea)

  object Date {
    implicit val dateFormat = jsonFormat3(Date.apply).withTipe
  }
  case class Date(time: Option[String], date: String, datetime: Option[String])

  object Location {
    implicit val locationFormat = jsonFormat3(Location.apply).withTipe
  }
  case class Location(city: String, lng: Option[Double], lat: Option[Double])

  object Venue {
    implicit val venueFormat = jsonFormat6(Venue.apply).withTipe
  }
  case class Venue(id: Option[Int], displayName: String, uri: Option[String], lng: Option[Double], lat: Option[Double], metroArea: MetroArea)

  object Artist {
    implicit val artistFormat = jsonFormat3(Artist.apply).withTipe
  }
  case class Artist(uri: String, displayName: String, id: Int/*, identifier: Seq[JsValue]*/)

  object Performance {
    implicit val performanceFormat = jsonFormat5(Performance.apply).withTipe
  }
  case class Performance(artist: Artist, displayName: String, billingIndex: Int, id: Int, billing: String)

  object Event {
    implicit val eventFormat = jsonFormat8(Event.apply).withTipe
  }
  case class Event(id: Int, tipe: String, uri: String, displayName: String, start: Date, /*performance: Seq[Performance],*/ location: Location, venue: Venue, popularity: Double)

  object ResultsPage {
    implicit def resultsPageFormat[T : JsonFormat] = jsonFormat4(ResultsPage.apply[T]).withTipe
    def emptyAt(page: Int): ResultsPage[Nothing] = ResultsPage(0, 0, page, Map.empty)
    val empty = emptyAt(1)
  }
  case class ResultsPage[+T](totalEntries: Int, perPage: Int, page: Int, results: Map[String, Seq[T]])
}
