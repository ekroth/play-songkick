package com.github.ekroth.songkick

trait Extensions {
  self: Commands =>

  import scala.concurrent._

  import play.api.Application
  import play.api.libs.json._
  import play.api.libs.iteratee._
  import play.api.libs.ws._

  class ResultsPager[T : Reads](private val query: String, private val key: String, private val underlying: ResultsPage[T]) {
    /** If page is the first one. */
    def isFirstPage: Boolean = underlying.page == 1

    /** If page is the last one. */
    def isLastPage: Boolean = underlying.totalEntries - underlying.perPage * underlying.page <= 0

    def previousPage()(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[Option[ResultsPager[T]]] = {
      if (isFirstPage) Future.successful(None)
      else WS.url(query.withKey + s"&page=${underlying.page - 1}").get.map { resp =>
        resp.json.validate[ResultsPage[T]].asOpt.map(_.withExt(query, key))
      }
    }

    def nextPage()(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[Option[ResultsPager[T]]] = {
      if (isLastPage) Future.successful(None)
      else WS.url(query.withKey + s"&page=${underlying.page + 1}").get.map { resp =>
        resp.json.validate[ResultsPage[T]].asOpt.map(_.withExt(query, key))
      }
    }

    def allPages()(implicit app: Application, ec: ExecutionContext, srv: Credentials): Enumerator[ResultsPager[T]] =
      Enumerator(this) >>> Enumerator.unfoldM(this) { page =>
        page.nextPage.map { nextOpt =>
          nextOpt.map(next => (next, next))
        }
      }

    /** Current and all remaining items. */
    def allItems(implicit app: Application, ec: ExecutionContext, srv: Credentials): Future[Seq[T]] =
      allPages().through(Enumeratee.map(_.underlying.results.getOrElse(key, Seq.empty))).run(Iteratee.consume[Seq[T]]())
  }

  implicit final class RichResultsPage[T : Reads](private val underlying: ResultsPage[T]) {
    def withExt(query: String, key: String): ResultsPager[T] = new ResultsPager(query, key, underlying)
  }
}
