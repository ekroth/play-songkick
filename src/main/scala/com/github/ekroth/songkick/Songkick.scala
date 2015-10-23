/* Copyright (c) 2015 Andrée Ekroth.
 * Distributed under the MIT License (MIT).
 * See accompanying file LICENSE or copy at
 * http://opensource.org/licenses/MIT
 */

package com.github.ekroth
package songkick

trait Songkick {
  val SongkickAPI: Commands with Extensions = new Commands with Extensions
}
