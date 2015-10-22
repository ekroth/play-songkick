/* Copyright (c) 2015 Andrée Ekroth.
 * Distributed under the MIT License (MIT).
 * See accompanying file LICENSE or copy at
 * http://opensource.org/licenses/MIT
 */

package com.github.ekroth

package object songkick extends Objects {
  case class Credentials(key: String)

  import errorhandling._

  object SongkickError {
    case class Permission(reason: String) extends Error
    case class Json(error: play.api.libs.json.JsError, reason: String) extends Error
    case class Impl(reason: String) extends Error
  }

  implicit class RichString(private val underlying: String) extends AnyVal {
    def withKey()(implicit srv: Credentials): String =
      underlying + (if (underlying.contains('?')) '&' else '?') + s"apikey=${srv.key}"
  }
}
