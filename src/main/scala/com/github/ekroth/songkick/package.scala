package com.github.ekroth

package object songkick extends Objects {
  case class Credentials(key: String)

  implicit class RichString(private val underlying: String) extends AnyVal {
    def withKey()(implicit srv: Credentials): String =
      underlying + (if (underlying.contains('?')) '&' else '?') + s"apikey=${srv.key}"
  }
}
