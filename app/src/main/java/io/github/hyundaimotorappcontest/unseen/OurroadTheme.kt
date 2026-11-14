package io.github.hyundaimotorappcontest.unseen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * ourroad 디자인 토큰.
 *
 * Apple Human Interface 스타일을 기준으로 한다: 중립 배경, 진한 1차 레이블과 회색 2·3차 레이블,
 * 강조색 하나, 큰 글자에 약한 음수 자간. 서체는 Pretendard(OFL, assets/licenses/pretendard.txt)다.
 * 차량 화면(160dpi, 2560×1440)에서 팔 길이 거리를 기준으로 크기를 키웠다.
 */
object Ourroad {
    val Pretendard = FontFamily(
        Font(R.font.pretendard_regular, FontWeight.Normal),
        Font(R.font.pretendard_medium, FontWeight.Medium),
        Font(R.font.pretendard_semibold, FontWeight.SemiBold),
        Font(R.font.pretendard_bold, FontWeight.Bold),
    )

    // Labels and fills
    val Label = Color(0xFF1D1D1F)
    val Secondary = Color(0xFF6E6E73)
    val Tertiary = Color(0xFF86868B)
    val Separator = Color(0xFFE5E5EA)
    val Fill = Color(0xFFE8E8ED)
    val Surface = Color(0xFFFFFFFF)
    val Background = Color(0xFFF5F5F7)
    val BackgroundTint = Color(0xFFE6F0F6)

    // One accent: Hyundai teal. System green only for switches.
    val Accent = Color(0xFF007FA8)
    val Green = Color(0xFF34C759)

    private fun style(size: Int, line: Int, weight: FontWeight, tracking: Double) = TextStyle(
        fontFamily = Pretendard,
        fontSize = size.sp,
        lineHeight = line.sp,
        fontWeight = weight,
        letterSpacing = tracking.em,
    )

    /** 장면의 핵심 한 줄 ("다드리 · 듣고 있어요"). */
    val Title = style(52, 64, FontWeight.SemiBold, -0.022)

    /** 다드리 자막·들린 말. 읽기 편하도록 줄 간격을 넉넉히 둔다. */
    val Body = style(30, 44, FontWeight.Normal, -0.012)

    val Button = style(28, 34, FontWeight.SemiBold, -0.01)
    val Headline = style(24, 30, FontWeight.SemiBold, -0.01)
    val Eyebrow = style(22, 28, FontWeight.SemiBold, -0.005)
    val Callout = style(20, 28, FontWeight.Normal, -0.006)
    val Footnote = style(18, 24, FontWeight.Normal, -0.004)
    val Control = style(18, 22, FontWeight.Medium, -0.004)
}

@Composable
fun OurroadTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Ourroad.Label,
            onPrimary = Color.White,
            secondary = Ourroad.Accent,
            background = Ourroad.Background,
            surface = Ourroad.Surface,
            onSurface = Ourroad.Label,
            outline = Ourroad.Separator,
        ),
        content = content,
    )
}
