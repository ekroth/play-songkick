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

  private def pagerOf[T : Reads](query: String, key: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[Option[ResultsPager[T]]] =
    WS.url(query.withKey).get.map { resp =>
      (resp.json \ "resultsPage").validate[ResultsPage[T]].asOpt.map(_.withExt(query, key))
    }

  /* Search */

  def artistSearch(query: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[Option[ResultsPager[JsValue]]] =
    pagerOf[JsValue](s"http://api.songkick.com/api/3.0/search/artists.json?query=$query", "artist")

  def venueSearch(query: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[Option[ResultsPager[JsValue]]] =
    pagerOf[JsValue](s"http://api.songkick.com/api/3.0/search/venues.json?query=$query", "venue")

  def locationNameSearch(query: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[Option[ResultsPager[JsValue]]] =
    pagerOf[JsValue](s"http://api.songkick.com/api/3.0/search/locations.json?query=$query", "location")

  def locationGeoSearch(lng: Double, lat: Double)(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[Option[ResultsPager[JsValue]]] =
    pagerOf[JsValue](s"http://api.songkick.com/api/3.0/search/locations.json?location=geo:$lng,$lat", "location")

  def locationIpSearch(ip: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[Option[ResultsPager[JsValue]]] =
    pagerOf[JsValue](s"http://api.songkick.com/api/3.0/search/locations.json?location=ip:$ip", "location")


  /* Calendars */

  def artistEvents(id: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[Option[ResultsPager[JsValue]]] =
    pagerOf[JsValue](s"http://api.songkick.com/api/3.0/artists/$id/calendar.json", "event")

  def venueEvents(id: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[Option[ResultsPager[JsValue]]] =
    pagerOf[JsValue](s"http://api.songkick.com/api/3.0/venues/$id/calendar.json", "event")

  def metroEvents(id: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[Option[ResultsPager[JsValue]]] =
    pagerOf[JsValue](s"http://api.songkick.com/api/3.0/metro_areas/$id/calendar.json", "event")

  def userEvents(id: String)(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[Option[ResultsPager[JsValue]]] = ???


}
