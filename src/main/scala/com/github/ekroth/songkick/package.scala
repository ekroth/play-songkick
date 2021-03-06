/* Copyright (c) 2015 Andrée Ekroth.
 * Distributed under the MIT License (MIT).
 * See accompanying file LICENSE or copy at
 * http://opensource.org/licenses/MIT
 */

package com.github.ekroth

package object songkick extends Objects {
  case class Credentials(key: String)

  import errorhandling._

  object SongkickError extends Errors {
    case class Usage(reason: String) extends Error
  }

  implicit class RichString(private val underlying: String) extends AnyVal {
    def withKey()(implicit srv: Credentials): String =
      underlying + (if (underlying.contains('?')) '&' else '?') + s"apikey=${srv.key}"

   def escaped: String = akka.http.scaladsl.model.Uri.Path(underlying).toString
  }

}
