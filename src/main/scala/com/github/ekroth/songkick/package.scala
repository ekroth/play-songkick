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
    case class Json(error: play.api.libs.json.JsError, json: play.api.libs.json.JsValue) extends Error {
      override def reason = "unable to parse json"
    }
    case class Impl(reason: String) extends Error
    case class Usage(reason: String) extends Error
  }

  implicit class RichString(private val underlying: String) extends AnyVal {
    def withKey()(implicit srv: Credentials): String =
      underlying + (if (underlying.contains('?')) '&' else '?') + s"apikey=${srv.key}"

    def escaped: String = play.utils.UriEncoding.encodePathSegment(underlying, "UTF-8")
  }

}
