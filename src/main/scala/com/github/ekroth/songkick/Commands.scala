/* Copyright (c) 2015 Andrée Ekroth.
 * Distributed under the MIT License (MIT).
 * See accompanying file LICENSE or copy at
 * http://opensource.org/licenses/MIT
 */

package com.github.ekroth
package songkick

/** Commands corresponding to the Songkick Web API. */
trait Commands {
  self: Extensions =>

  import scala.collection.immutable.Seq
  import scala.concurrent.ExecutionContext

  import scalaz._
  import Scalaz._

  import akka.actor.ActorSystem
  import akka.http.scaladsl.Http
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import akka.http.scaladsl.model.HttpRequest
  import akka.http.scaladsl.unmarshalling.Unmarshal
  import akka.stream.Materializer
  import spray.json._

  import errorhandling._

  private val baseUrl = "http://api.songkick.com/api/3.0"

  def pagerOf[T : JsonFormat](query: String, key: String)(implicit sys: ActorSystem, fm: Materializer, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[T]] =
    Result.okF {
      (for {
        resp <- Http().singleRequest(HttpRequest(uri = query.withKey))
        js <- Unmarshal(resp.entity).to[JsValue]
      } yield {

        val JsObject(fields) = js.asJsObject
        val out = fields("resultsPage").convertTo[ResultsPage[T]].withExt(query, key)
        out.right
      }).recover {
        case x: Exception => SongkickError.Thrown(x).left
        case x => SongkickError.Unknown(s"Odd error: $x").left
      }
    }

  /* Search */

  def locationNameSearch(query: String)(implicit sys: ActorSystem, fm: Materializer, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[LocationArea]] =
    pagerOf[LocationArea](s"$baseUrl/locationsjson?query=${query.escaped}", "location")

  def artistSearch(query: String)(implicit sys: ActorSystem, fm: Materializer, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[Artist]] =
    pagerOf[Artist](s"$baseUrl/search/artists.json?query=${query/*.escaped*/}", "artist")


  /* Calendars */

  def metroEvents(id: String)(implicit sys: ActorSystem, fm: Materializer, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[Event]] =
    pagerOf[Event](s"$baseUrl/metro_areas/$id/calendar.json", "event")

  def artistEvents(id: Int)(implicit sys: ActorSystem, fm: Materializer, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[Event]] =
    pagerOf[Event](s"$baseUrl/artists/$id/calendar.json", "event")

}
