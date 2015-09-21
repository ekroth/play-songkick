package com.github.ekroth.songkick

/** Objects corresponding to Songkick's object model.
  */
private[songkick] trait Objects {
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

  object Location {
    implicit val LocationWrites = Json.writes[Location].transform(TypeNameFix.afterWrite)
    implicit val LocationTrackReads = Json.reads[Location].compose(TypeNameFix.beforeRead)
  }
  case class Location(city: String, lng: Option[String], lat: Option[String])

  object Date {
    implicit val DateWrites = Json.writes[Date].transform(TypeNameFix.afterWrite)
    implicit val DateReads = Json.reads[Date].compose(TypeNameFix.beforeRead)
  }
  case class Date(time: String, date: String, datetime: Option[String])

  object Venue {
    implicit val VenueWrites = Json.writes[Venue].transform(TypeNameFix.afterWrite)
    implicit val VenueReads = Json.reads[Venue].compose(TypeNameFix.beforeRead)
  }
  case class Venue(displayName: String, id: Int, lng: Option[String], lat: Option[String])

  case class Performance(displayName: String, id: Int, artist: Option[Artist], billingIndex: Option[Int], billing: Option[String])

  case class Artist(uri: String, displayName: String, id: Int, identifier: Map[String, String], onTourUntil: Option[String])

  case class Event(displayName: String, tipe: String, uri: String, venue: Venue, location: Location, start: Date, performance: Seq[Performance], id: Int) {
    require(tipe == "Concert", "API must be updated")
  }


  /** The Json reads/writes macro can't handle generics very well. */
  object ResultsPage {
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
  }
  case class ResultsPage[T](totalEntries: Int, perPage: Int, page: Int, results: Map[String, Seq[T]])

}
