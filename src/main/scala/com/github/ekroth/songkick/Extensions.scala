/* Copyright (c) 2015 Andrée Ekroth.
 * Distributed under the MIT License (MIT).
 * See accompanying file LICENSE or copy at
 * http://opensource.org/licenses/MIT
 */

package com.github.ekroth
package songkick

trait Extensions {
  self: Commands =>

  import scala.concurrent._

  import scalaz._
  import Scalaz._

  import play.api.Application
  import play.api.libs.json._
  import play.api.libs.iteratee.{ Error => ItError, _ }
  import play.api.libs.ws._

  import errorhandling._

  class ResultsPager[T : Reads](val query: String, val key: String, val underlying: ResultsPage[T]) {

    /** If page is the first one. */
    def isFirstPage: Boolean = underlying.page == 1

    /** If page is the last one. */
    def isLastPage: Boolean = underlying.totalEntries - underlying.perPage * underlying.page <= 0

    /** Total amount of pages. */
    def totalPages: Int = underlying.totalEntries / underlying.perPage

    /** Items on current page. */
    def items: Seq[T] = underlying.results.getOrElse(key, Seq.empty)

    /** All items on all remaining pages. */
    def allItems(implicit app: Application, ec: ExecutionContext, srv: Credentials): ResultF[Seq[T]] = {

      val pagesF = for {
        i <- (underlying.page + 1) to totalPages
        pageR = pageAt(i)
      } yield pageR.run

      val pages = Result.sequenceF(pagesF)
      val all = pages.map { page =>
        items ++ page.map(_.items).flatten
      }

      all
    }

    /** View page at specific index. */
    def pageAt(i: Int)(implicit app: Application, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[T]] = {
      if (isLastPage) {
        Result.failF(SongkickError.Permission("invalid page"))
      } else {
        pagerOf[T](query + s"&page=$i", key)
      }
    }

    /** Next page. */
    def nextPage()(implicit app: Application, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[T]] =
      pageAt(underlying.page + 1)
  }

  implicit final class RichResultsPage[T : Reads](private val underlying: ResultsPage[T]) {
    def withExt(query: String, key: String): ResultsPager[T] = new ResultsPager(query, key, underlying)
  }
}
