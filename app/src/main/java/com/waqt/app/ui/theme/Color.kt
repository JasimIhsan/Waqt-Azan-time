package com.waqt.app.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================================
// Official Waqt Brand Palette (Sampled from Reference Palette)
// ==========================================================

// Base Dark Canvas & Surfaces (Obsidian Slate with gentle organic undertone)
val WaqtDarkBg = Color(0xFF0F1512)             // Clean deep dark canvas (not muddy)
val WaqtCardSurface = Color(0xFF16201B)        // Default dark card surface
val WaqtCardSurfaceElevated = Color(0xFF1E2C25)// Elevated card surface
val WaqtCardSurfaceVariant = WaqtCardSurfaceElevated
val WaqtCardBorder = Color(0xFF283A31)         // Clean card border
val WaqtActiveCardSurface = Color(0xFF17362C)  // Active/Upcoming card surface (subtle emerald tint)

// Palette Core Colors (Direct from user reference image cards)
val WaqtForestGreen = Color(0xFF377158)        // #377158 Deep Forest Jade (Top-Right card)
val WaqtTeal = Color(0xFF69B6AF)               // #69B6AF Seafoam / Cyan Teal (Bottom-Right card)
val WaqtSage = Color(0xFFAFD4A8)               // #AFD4A8 Soft Pistachio Sage (Top-Left card)
val WaqtMintCream = Color(0xFFE5F1E2)          // #E5F1E2 Clean Mint Cream / Light (Bottom-Left card)

// Palette Vibrant Accents (Direct from reference image Figma accents)
val WaqtCoral = Color(0xFFFF7262)              // #FF7262 Sunset / Sunrise Coral
val WaqtSky = Color(0xFF1ABCFE)                // #1ABCFE Sky Blue
val WaqtViolet = Color(0xFFA259FF)             // #A259FF Violet
val WaqtNeonGreen = Color(0xFF0ECF83)          // #0ECF83 Bright Green

// Text & Content Hierarchy
val WaqtTextPrimary = Color(0xFFFFFFFF)        // Pure crisp white for primary headers & titles
val WaqtTextSecondary = Color(0xFFE5F1E2)      // Mint Cream for prayer times and labels
val WaqtTextMuted = Color(0xFF90A89C)          // Crisp muted sage-gray for subtitles

// Functional Accents
val Amber400 = Color(0xFFFBBF24)
val Rose400 = WaqtCoral

// Backward-compatible mapping for existing design system tokens
val Midnight900 = WaqtDarkBg
val Slate800 = WaqtCardSurface
val Slate700 = WaqtCardSurfaceElevated
val Slate600 = WaqtCardBorder
val Slate400 = WaqtTextMuted
val Slate200 = WaqtMintCream
val Slate50 = Color.White

val Sky400 = WaqtTeal
val Sky500 = WaqtForestGreen
val Sky300 = WaqtSage
val Emerald400 = WaqtSage
