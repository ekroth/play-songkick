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
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import spray.json._

  /** Rename json names that are reserved words in Scala.
    *
    * In order to be able to use the Json.writes/reads macro we
    * need to pre- and post-process the JsValues accordingly.
    */
/*  object TypeNameFix {
    private[this] def replace(in: String, out: String)(json: JsValue): JsValue = json match {
      case x: JsObject => (x \ in) match {
        case y: JsString => (x - in) + (out, y)
        case _ => x
      }
      case x => x
    }*/
    /** Convert from 'name' to 'keyword'. */
//    val afterWrite: JsValue => JsValue = replace("tipe", "type")(_)

    /** Convert from 'keyword' to 'name'. */
//    val beforeRead = Reads[JsValue] { js => JsSuccess(replace("type", "tipe")(js)) }
//  }

  implicit class RichJsonFormat[T](val underlying: JsonFormat[T]) {

    private def fix(obj: JsValue): JsValue = obj match {
      case JsObject(fields) => JsObject(fields.map {
        case ("tipe", v) => ("type", v)
        case x => x
      })

      case x => x
    }

    def withTipe: JsonFormat[T] = new JsonFormat[T] {
      override def write(obj: T): JsValue = fix(underlying.write(obj))

      override def read(obj: JsValue): T = underlying.read(fix(obj))
    }
  }

  object DisplayName extends DefaultJsonProtocol {
    implicit val displayNameFormat = jsonFormat1(DisplayName.apply)
  }
  case class DisplayName(displayName: String)

  object MetroArea extends DefaultJsonProtocol {
    implicit val metroAreaFormat = jsonFormat7(MetroArea.apply)
  }
  case class MetroArea(uri: String, displayName: String, country: DisplayName, id: Int, lng: Option[Double], lat: Option[Double], state: Option[DisplayName])

  object City extends DefaultJsonProtocol {
    implicit val cityFormat = jsonFormat4(City.apply)
  }
  case class City(displayName: String, country: DisplayName, lng: Option[Double], lat: Option[Double])

  object LocationArea extends DefaultJsonProtocol {
    implicit val locationAreaFormat = jsonFormat2(LocationArea.apply)
  }
  case class LocationArea(city: City, metroArea: MetroArea)

  object Date extends DefaultJsonProtocol {
    implicit val dateFormat = jsonFormat3(Date.apply)
  }
  case class Date(time: Option[String], date: String, datetime: Option[String])

  object Location extends DefaultJsonProtocol {
    implicit val locationFormat = jsonFormat3(Location.apply)
  }
  case class Location(city: String, lng: Option[Double], lat: Option[Double])

  object Venue extends DefaultJsonProtocol {
    implicit val venueFormat = jsonFormat6(Venue.apply)
  }
  case class Venue(id: Option[Int], displayName: String, uri: Option[String], lng: Option[Double], lat: Option[Double], metroArea: MetroArea)

  object Artist extends DefaultJsonProtocol {
    implicit val artistFormat = jsonFormat3(Artist.apply)
  }
  case class Artist(uri: String, displayName: String, id: Int/*, identifier: Seq[JsValue]*/)

  object Performance extends DefaultJsonProtocol {
    implicit val performanceFormat = jsonFormat5(Performance.apply)
  }
  case class Performance(artist: Artist, displayName: String, billingIndex: Int, id: Int, billing: String)

  object Event extends DefaultJsonProtocol {
    implicit val eventFormat = jsonFormat8(Event.apply)
  }
  case class Event(id: Int, tipe: String, uri: String, displayName: String, start: Date, /*performance: Seq[Performance],*/ location: Location, venue: Venue, popularity: Double)

  object ResultsPage extends DefaultJsonProtocol {
    implicit def resultsPageFormat[T : JsonFormat] = jsonFormat4(ResultsPage.apply[T])
    def emptyAt(page: Int): ResultsPage[Nothing] = ResultsPage(0, 0, page, Map.empty)
    val empty = emptyAt(1)
  }
  case class ResultsPage[+T](totalEntries: Int, perPage: Int, page: Int, results: Map[String, Seq[T]])
}
