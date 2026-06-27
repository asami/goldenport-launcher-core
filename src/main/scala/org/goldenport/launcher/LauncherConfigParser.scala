package org.goldenport.launcher

import java.nio.file.Path

/*
 * @since   Jun. 27, 2026
 * @version Jun. 27, 2026
 * @author  ASAMI, Tomoharu
 */
object LauncherConfigParser {
  def parse(
    path: Path,
    text: String
  ): Map[String, Vector[String]] =
    _file_type(path) match {
      case "yaml" | "yml" => _parse_light_yaml(text)
      case "properties" | "props" | "conf" => _parse_properties(text)
      case other =>
        throw LauncherCoreException(s"unsupported launcher config file type: .$other; use yaml, yml, properties, props, or conf")
    }

  private def _file_type(path: Path): String = {
    val name = path.getFileName.toString
    val i = name.lastIndexOf('.')
    if (i >= 0 && i + 1 < name.length)
      name.substring(i + 1).toLowerCase
    else
      "yaml"
  }

  private def _parse_properties(text: String): Map[String, Vector[String]] = {
    var values = Map.empty[String, Vector[String]]
    text.linesIterator.foreach { raw =>
      val uncommented = _strip_comment(raw)
      val trimmed = uncommented.trim
      if (trimmed.nonEmpty) {
        val idx = _key_value_index(trimmed)
        if (idx >= 0) {
          val key = trimmed.substring(0, idx).trim
          val value = trimmed.substring(idx + 1).trim
          _put_value(key, value, values).foreach(v => values = v)
        }
      }
    }
    values
  }

  private def _parse_light_yaml(text: String): Map[String, Vector[String]] = {
    val builder = Map.newBuilder[String, Vector[String]]
    var values = Map.empty[String, Vector[String]]
    var stack = Vector.empty[(Int, String)]
    var pendingkey: Option[String] = None

    def _put_(path: String, value: String): Unit =
      _put_value(path, value, values).foreach(v => values = v)

    text.linesIterator.foreach { raw =>
      val uncommented = _strip_comment(raw)
      if (uncommented.trim.nonEmpty) {
        val indent = uncommented.takeWhile(_ == ' ').length
        val trimmed = uncommented.trim
        stack = stack.dropRight(stack.count(_._1 >= indent))
        if (trimmed.startsWith("- ")) {
          pendingkey.foreach(k => _put_(k, trimmed.drop(2)))
        } else {
          val idx = _key_value_index(trimmed)
          if (idx >= 0) {
            val key = trimmed.substring(0, idx).trim
            val value = trimmed.substring(idx + 1).trim
            val path = (stack.map(_._2) :+ key).mkString(".")
            if (value.isEmpty) {
              stack = stack :+ (indent, key)
              pendingkey = Some(path)
            } else {
              _put_(path, value)
              pendingkey = Some(path)
            }
          }
        }
      }
    }
    builder ++= values
    builder.result()
  }

  private def _put_value(
    path: String,
    value: String,
    values: Map[String, Vector[String]]
  ): Option[Map[String, Vector[String]]] = {
    val key = path.trim
    val clean = _unquote(value.trim)
    if (key.nonEmpty && clean.nonEmpty)
      Some(values.updated(key, values.getOrElse(key, Vector.empty) :+ clean))
    else
      None
  }

  private def _strip_comment(s: String): String = {
    val idx = s.indexOf('#')
    if (idx < 0) s else s.substring(0, idx)
  }

  private def _key_value_index(s: String): Int = {
    val colon = s.indexOf(':')
    val equals = s.indexOf('=')
    (colon, equals) match {
      case (-1, -1) => -1
      case (-1, x) => x
      case (x, -1) => x
      case (x, y) => math.min(x, y)
    }
  }

  private def _unquote(s: String): String =
    if (s.length >= 2 && ((s.head == '"' && s.last == '"') || (s.head == '\'' && s.last == '\'')))
      s.substring(1, s.length - 1)
    else
      s
}
