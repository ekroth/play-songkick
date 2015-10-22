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
  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext

  import scalaz._
  import Scalaz._
  import scalaz.contrib._
  import scalaz.contrib.std._

  import play.api.Logger
  import play.api.Application
  import play.api.http.Status._
  import play.api.libs.ws._
  import play.api.libs.json._

  import errorhandling._

  private[songkick] def pagerOf[T : Reads](query: String, key: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[T]] =
    Result.okF {
      (for {
        resp <- WS.url(query.withKey).get()
      } yield {

        val pager = (resp.json \ "resultsPage").validate[ResultsPage[T]] match {
          case JsSuccess(res, _) => res.withExt(query, key).right
          case e : JsError => SongkickError.Json(e, resp.json).left
        }

        val proper = pager.fold(
          l => pager,
          { r =>
            if ((resp.json \ "resultsPage" \ "totalEntries") == JsNumber(0))
              r.right
            else {
              (resp.json \ "resultsPage" \ "results" \ key).validate[Seq[JsValue]] match {
                case JsSuccess(res, _) if res.length == r.items.length => r.right
                case JsSuccess(_, _) => SongkickError.Impl(s"parsing failed: pages are missing, json: ${resp.json}, $pager").left
                case e : JsError => SongkickError.Json(e, resp.json).left
              }
            }
          })

        proper
      }).recover {
        case x: Exception => SongkickError.Thrown(x).left
        case x => SongkickError.Unknown(s"Odd error: $x").left
      }
    }

  /* Search */

  def locationNameSearch(query: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[LocationArea]] =
    pagerOf[LocationArea](s"http://api.songkick.com/api/3.0/search/locations.json?query=$query", "location")

  def artistSearch(query: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[Artist]] =
    pagerOf[Artist](s"http://api.songkick.com/api/3.0/search/artists.json?query=$query", "artist")


  /* Calendars */

  def metroEvents(id: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[Event]] =
    pagerOf[Event](s"http://api.songkick.com/api/3.0/metro_areas/$id/calendar.json", "event")

  def artistEvents(id: Int)(implicit app: Application, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[Event]] =
    pagerOf[Event](s"http://api.songkick.com/api/3.0/artists/$id/calendar.json", "event")

}
