package com.interview.logviewer.ui.theme

import androidx.compose.ui.graphics.Color
import com.interview.logviewer.domain.model.Severity

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val BackgroundDark = Color(0xFF101114)
val SurfaceDark = Color(0xFF1A1B1F)

val SeverityFatal = Color(0xFF7F1D1D)
val SeverityError = Color(0xFFE53935)
val SeverityWarn = Color(0xFFFFB300)
val SeverityInfo = Color(0xFF2E7DD7)
val SeverityDebug = Color(0xFF8A8F98)
val SeverityUnknown = Color(0xFFBDBDBD)

fun Severity.toColor(): Color = when (this) {
    Severity.FATAL -> SeverityFatal
    Severity.ERROR -> SeverityError
    Severity.WARN -> SeverityWarn
    Severity.INFO -> SeverityInfo
    Severity.DEBUG -> SeverityDebug
    Severity.UNKNOWN -> SeverityUnknown
}
