/* Copyright (c) 2015 Andrée Ekroth.
 * Distributed under the MIT License (MIT).
 * See accompanying file LICENSE or copy at
 * http://opensource.org/licenses/MIT
 */

package com.github.ekroth
package songkick

trait Extensions {
  self: Commands =>

  import scala.collection.immutable.Seq
  import scala.concurrent.ExecutionContext

  import scalaz._
  import Scalaz._

  import akka.actor.ActorSystem
  import akka.stream.Materializer
  import spray.json._

  import errorhandling._

  class ResultsPager[T : JsonFormat](val query: String, val key: String, val underlying: ResultsPage[T]) {

    /** If page is the first one. */
    def isFirstPage: Boolean = underlying.page == 1

    /** If page is the last one. */
    def isLastPage: Boolean = underlying.totalEntries - underlying.perPage * underlying.page <= 0

    /** Total amount of pages. */
    def totalPages: Int = underlying.totalEntries / underlying.perPage

    /** Items on current page. */
    def items: Seq[T] = underlying.results.getOrElse(key, Seq.empty)

    /** All items on all remaining pages. */
    def allItems(implicit sys: ActorSystem, fm: Materializer, ec: ExecutionContext, srv: Credentials): ResultF[Seq[T]] = {
      val indices = (underlying.page + 1) to totalPages
      val pagesR = indices.map(pageAt)
      val pages = Result.run(pagesR)
      val all = pages.map { page =>
        items ++ page.map(_.items).flatten
      }

      all
    }

    /** View page at specific index. */
    def pageAt(i: Int)(implicit sys: ActorSystem, fm: Materializer, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[T]] = {
      if (isLastPage) {
        Result.failF(SongkickError.Usage("invalid page"))
      } else {
        pagerOf[T](query + s"&page=$i", key)
      }
    }

    /** Next page. */
    def nextPage()(implicit sys: ActorSystem, fm: Materializer, ec: ExecutionContext, srv: Credentials): ResultF[ResultsPager[T]] =
      pageAt(underlying.page + 1)
  }

  implicit final class RichResultsPage[T : JsonFormat](private val underlying: ResultsPage[T]) {
    def withExt(query: String, key: String): ResultsPager[T] = new ResultsPager(query, key, underlying)
  }
}
