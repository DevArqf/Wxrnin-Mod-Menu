package com.android.support.components;

import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public final class Theme {

    // ----------- Colors (blue) ------------
    public static int ACCENT        = 0xFF3B82F6; // main blue
    public static int ACCENT_LIGHT  = 0xFF38BDF8; // sky, gradient start for toggles, sliders and buttons
    public static int ACCENT_DEEP   = 0xFF6366F1; // indigo, gradient end
    public static int ACCENT_KNOB   = 0xFFFFFFFF; // knob color of a toggle that is on

    public static int PANEL_BG      = 0xF20A1426; // top of the panel gradient
    public static int PANEL_BG_END  = 0xF2050A16; // bottom of the panel gradient
    public static int PANEL_GLOW    = 0x403B82F6; // soft light in the panel corners
    public static int PANEL_STROKE  = 0x6638BDF8;
    public static int SIDEBAR_BG    = 0x26000814;
    public static int TAB_ACTIVE    = 0x2E3B82F6; // pill behind the selected tab
    public static int CARD_BG       = 0xCC101C34;
    public static int CARD_PRESSED  = 0xE61A2C50;
    public static int CARD_STROKE   = 0x1F60A5FA;
    public static int DIVIDER       = 0x1F60A5FA;

    public static int TEXT          = 0xFFEAF2FF;
    public static int TEXT_DIM      = 0xFF8DA2C8;
    public static int TEXT_FAINT    = 0xFF51658C;

    public static int TRACK_OFF     = 0xFF1C2A47;
    public static int KNOB_OFF      = 0xFFC7D5EE;

    // ----------- Sizes ------------
    public static int PANEL_MAX_WIDTH  = 400;
    public static int PANEL_MAX_HEIGHT = 370;
    public static int PANEL_MARGIN     = 12;   // minimum distance from the screen edges
    public static int PANEL_RADIUS     = 20;
    public static float SIDEBAR_RATIO  = 0.23f;

    public static int HEADER_HEIGHT = 50;
    public static int CARD_RADIUS   = 12;
    public static int CARD_HEIGHT   = 46;
    public static int CARD_GAP      = 8;
    public static int CARD_PADDING  = 14;

    public static int BUBBLE_SIZE   = 54;
    public static String BUBBLE_TEXT = "W"; // drawn when Setup.cpp's Icon() is empty

    // ----------- Text sizes (sp) ------------
    public static float TEXT_TITLE  = 16f;
    public static float TEXT_BODY   = 14.5f;
    public static float TEXT_SMALL  = 12f;
    public static float TEXT_TAB    = 11f;

    // ----------- Font ------------
    // false: system font, true: custom font (loaded from native LoadFontData)
    public static boolean USE_CUSTOM_FONT = false;

    private Theme() {
    }
}
