package com.dailyenglish.nativeapp.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
  primary = Color(0xFF4F6A4B),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD5E8CF),
  onPrimaryContainer = Color(0xFF152611),
  secondary = Color(0xFF5E6B57),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFE2E9DC),
  onSecondaryContainer = Color(0xFF1B2118),
  tertiary = Color(0xFF7A5C3E),
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFF4E0C9),
  onTertiaryContainer = Color(0xFF2B1A08),
  background = Color(0xFFF3F5F2),
  onBackground = Color(0xFF1B211A),
  surface = Color(0xFFF8FAF5),
  onSurface = Color(0xFF1B211A),
  surfaceVariant = Color(0xFFE6EBE0),
  onSurfaceVariant = Color(0xFF44483F),
  outline = Color(0xFF74796E),
  outlineVariant = Color(0xFFC4C9BD),
  error = Color(0xFFB3261E),
  onError = Color.White,
  errorContainer = Color(0xFFF9DEDC),
  onErrorContainer = Color(0xFF410E0B),
  inverseSurface = Color(0xFF2E322B),
  inverseOnSurface = Color(0xFFEFF1EA),
  inversePrimary = Color(0xFFB3D0AB),
  scrim = Color.Black,
)

private val DarkColors = darkColorScheme(
  primary = Color(0xFFB3D0AB),
  onPrimary = Color(0xFF1F371D),
  primaryContainer = Color(0xFF365231),
  onPrimaryContainer = Color(0xFFCFEBC6),
  secondary = Color(0xFFBFCBB8),
  onSecondary = Color(0xFF2A3325),
  secondaryContainer = Color(0xFF404A3A),
  onSecondaryContainer = Color(0xFFDBE6D3),
  tertiary = Color(0xFFE5C29D),
  onTertiary = Color(0xFF442B14),
  tertiaryContainer = Color(0xFF5D4128),
  onTertiaryContainer = Color(0xFFFFDCBA),
  background = Color(0xFF11140F),
  onBackground = Color(0xFFE2E5DB),
  surface = Color(0xFF161A14),
  onSurface = Color(0xFFE2E5DB),
  surfaceVariant = Color(0xFF44483F),
  onSurfaceVariant = Color(0xFFC4C9BD),
  outline = Color(0xFF8E9387),
  outlineVariant = Color(0xFF44483F),
  error = Color(0xFFFFB4AB),
  onError = Color(0xFF690005),
  errorContainer = Color(0xFF93000A),
  onErrorContainer = Color(0xFFFFDAD6),
  inverseSurface = Color(0xFFE2E5DB),
  inverseOnSurface = Color(0xFF2E322B),
  inversePrimary = Color(0xFF4F6A4B),
  scrim = Color.Black,
)

private val AppShapes = Shapes(
  extraSmall = RoundedCornerShape(4.dp),
  small = RoundedCornerShape(8.dp),
  medium = RoundedCornerShape(12.dp),
  large = RoundedCornerShape(16.dp),
  extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun DailyEnglishTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColors
    else -> LightColors
  }
  MaterialTheme(colorScheme = colorScheme, typography = AppTypography, shapes = AppShapes, content = content)
}
