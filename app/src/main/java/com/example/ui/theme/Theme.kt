package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.model.FontScaleOption
import com.example.model.UserRole

@Immutable
data class UniSyncExtendedColors(
    val cardBorder: Color,
    val highlightAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color
)

val LocalUniSyncColors = staticCompositionLocalOf {
    UniSyncExtendedColors(
        cardBorder = StudentBorderLight,
        highlightAccent = StudentHighlightLight,
        textPrimary = StudentTextPrimaryLight,
        textSecondary = StudentTextSecondaryLight
    )
}

private val UniSyncShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun UniSyncTheme(
    role: UserRole,
    darkMode: Boolean,
    fontScale: FontScaleOption,
    isRtl: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkMode && role == UserRole.STUDENT -> darkColorScheme(
            primary = StudentPrimaryDark,
            onPrimary = Color.White,
            secondary = StudentHighlightDark,
            onSecondary = Color.White,
            background = DarkCanvas,
            onBackground = DarkTextPrimary,
            surface = DarkCard,
            onSurface = DarkTextPrimary,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = DarkTextSecondary,
            outline = DarkBorder,
            outlineVariant = DarkBorder,
            primaryContainer = Color(0xFF14352C),
            onPrimaryContainer = Color(0xFFCFF5E9),
            secondaryContainer = Color(0xFF3A2116),
            onSecondaryContainer = Color(0xFFFCD9C7)
        )
        darkMode && role == UserRole.INSTRUCTOR -> darkColorScheme(
            primary = InstructorPrimaryDark,
            onPrimary = Color.White,
            secondary = InstructorHighlightDark,
            onSecondary = Color.White,
            background = DarkCanvas,
            onBackground = DarkTextPrimary,
            surface = DarkCard,
            onSurface = DarkTextPrimary,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = DarkTextSecondary,
            outline = DarkBorder,
            outlineVariant = DarkBorder,
            primaryContainer = Color(0xFF172C4A),
            onPrimaryContainer = Color(0xFFD6E6FC),
            secondaryContainer = Color(0xFF12323D),
            onSecondaryContainer = Color(0xFFC8EFF8)
        )
        role == UserRole.INSTRUCTOR -> lightColorScheme(
            primary = InstructorPrimaryLight,
            onPrimary = Color.White,
            secondary = InstructorHighlightLight,
            onSecondary = Color.White,
            background = InstructorCanvasLight,
            onBackground = InstructorTextPrimaryLight,
            surface = InstructorCardLight,
            onSurface = InstructorTextPrimaryLight,
            surfaceVariant = InstructorSurfaceVariantLight,
            onSurfaceVariant = InstructorTextSecondaryLight,
            outline = InstructorBorderLight,
            outlineVariant = InstructorBorderLight,
            primaryContainer = Color(0xFFDCE8F8),
            onPrimaryContainer = InstructorPrimaryLight,
            secondaryContainer = Color(0xFFD6F0F5),
            onSecondaryContainer = InstructorHighlightLight
        )
        else -> lightColorScheme(
            primary = StudentPrimaryLight,
            onPrimary = Color.White,
            secondary = StudentHighlightLight,
            onSecondary = Color.White,
            background = StudentCanvasLight,
            onBackground = StudentTextPrimaryLight,
            surface = StudentCardLight,
            onSurface = StudentTextPrimaryLight,
            surfaceVariant = StudentSurfaceVariantLight,
            onSurfaceVariant = StudentTextSecondaryLight,
            outline = StudentBorderLight,
            outlineVariant = StudentBorderLight,
            primaryContainer = Color(0xFFD8EFE8),
            onPrimaryContainer = StudentPrimaryLight,
            secondaryContainer = Color(0xFFFAE5DA),
            onSecondaryContainer = StudentHighlightLight
        )
    }

    val extendedColors = when {
        darkMode -> UniSyncExtendedColors(
            cardBorder = DarkBorder,
            highlightAccent = if (role == UserRole.INSTRUCTOR) InstructorHighlightDark else StudentHighlightDark,
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary
        )
        role == UserRole.INSTRUCTOR -> UniSyncExtendedColors(
            cardBorder = InstructorBorderLight,
            highlightAccent = InstructorHighlightLight,
            textPrimary = InstructorTextPrimaryLight,
            textSecondary = InstructorTextSecondaryLight
        )
        else -> UniSyncExtendedColors(
            cardBorder = StudentBorderLight,
            highlightAccent = StudentHighlightLight,
            textPrimary = StudentTextPrimaryLight,
            textSecondary = StudentTextSecondaryLight
        )
    }

    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    val typography = buildUniSyncTypography(fontScale, isRtl)

    CompositionLocalProvider(
        LocalLayoutDirection provides layoutDirection,
        LocalUniSyncColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = UniSyncShapes,
            content = content
        )
    }
}
