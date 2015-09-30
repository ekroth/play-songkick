/* Copyright (c) 2015 Andrée Ekroth.
 * Distributed under the MIT License (MIT).
 * See accompanying file LICENSE or copy at
 * http://opensource.org/licenses/MIT
 */

package com.github.ekroth.songkick

/** Commands corresponding to the Songkick Web API. */
trait Commands {
  self: Extensions =>

  import scala.concurrent.Future
  import scala.concurrent.ExecutionContext

  import play.api.Logger
  import play.api.Application
  import play.api.http.Status._
  import play.api.libs.ws._
  import play.api.libs.json._

  private def pagerOf[T : Reads](query: String, key: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[ResultsPager[T]] =
    WS.url(query.withKey).get.map { resp =>

//      println(resp.json.toString)
      val page = (resp.json \ "resultsPage").validate[ResultsPage[T]] match {
        case JsError(errs) => {
          println(s"""Invalid json response: '${errs.mkString("\n")}'""")
          ResultsPage.empty
        }

        case JsSuccess(res, _) => res
      }
      val pager = page.withExt(query, key)

      /* Check validity */
      {
        (resp.json \ "resultsPage" \ "results" \ key).validate[Seq[JsValue]] match {
          case JsSuccess(res, _) => assert(res.length == pager.items.length)
          case _ =>
        }
      }

      pager
    }

  /* Search */

  def locationNameSearch(query: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[ResultsPager[LocationArea]] =
    pagerOf[LocationArea](s"http://api.songkick.com/api/3.0/search/locations.json?query=$query", "location")


  /* Calendars */

  def metroEvents(id: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[ResultsPager[Event]] =
    pagerOf[Event](s"http://api.songkick.com/api/3.0/metro_areas/$id/calendar.json", "event")
}
