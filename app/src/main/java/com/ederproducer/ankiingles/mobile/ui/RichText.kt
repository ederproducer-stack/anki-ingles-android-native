package com.ederproducer.ankiingles.mobile.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/** Converte o HTML simples dos cartões (b, i, u, br, font/span com cor) para texto. */
object RichText {

    private val TAG = Regex("<(/?)(b|strong|i|em|u|br|font|span)([^>]*)>", RegexOption.IGNORE_CASE)
    private val COLOR = Regex("(?:color|#)(?:=|:)?\\s*['\"]?(#[0-9a-fA-F]{6}|[a-zA-Z]+)", RegexOption.IGNORE_CASE)

    private fun decode(text: String): String = text
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("&#(\\d+);")) { it.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: "" }

    fun plain(html: String): String {
        val withBreaks = html.replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        return decode(withBreaks.replace(Regex("<[^>]+>"), "")).trim()
    }

    fun annotated(html: String): AnnotatedString = buildAnnotatedString {
        var bold = false
        var italic = false
        var underline = false
        var color: Color? = null
        var last = 0

        fun pushStyle() {
            pushStyle(
                SpanStyle(
                    fontWeight = if (bold) FontWeight.Bold else null,
                    fontStyle = if (italic) FontStyle.Italic else null,
                    textDecoration = if (underline) TextDecoration.Underline else null,
                    color = color ?: Color.Unspecified
                )
            )
        }

        TAG.findAll(html).forEach { match ->
            val text = html.substring(last, match.range.first)
            if (text.isNotEmpty()) {
                pushStyle()
                append(decode(text))
                pop()
            }
            val closing = match.groupValues[1] == "/"
            val name = match.groupValues[2].lowercase()
            val attrs = match.groupValues[3]
            when (name) {
                "br" -> append("\n")
                "b", "strong" -> bold = !closing
                "i", "em" -> italic = !closing
                "u" -> underline = !closing
                "font", "span" -> {
                    if (!closing) {
                        color = COLOR.find(attrs)?.groupValues?.get(1)?.let {
                            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
                        }
                    } else color = null
                }
            }
            last = match.range.last + 1
        }
        val tail = html.substring(last)
        if (tail.isNotEmpty()) {
            pushStyle()
            append(decode(tail))
            pop()
        }
    }
}
