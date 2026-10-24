//
// Created by github.com/seedhollow on 17/10/25.
// Template by Wxrnin
//

#include "Visuals.hpp"

// Called every frame by the overlay (only created when Menu.ENABLE_OVERLAY is true in Menu.java).
// Colors are 0-255: Unity::Color(r, g, b, a)
void Visuals::Update(Draw draw, int screenWidth, int screenHeight) {
    auto &v = Vars::Template;

    if (v.crosshair) {
        Unity::Vector2 center(screenWidth / 2.0f, screenHeight / 2.0f);
        draw.DrawCrosshair(Unity::Color(56, 189, 248, 255), center, v.crosshairSize);
        draw.DrawCircle(Unity::Color(59, 130, 246, 200), 2.0f, center, v.crosshairSize * 0.75f);
    }

    // ESP example: loop over the objects your hooks collected (see Includes/ESPManager.h)
    //  for (auto *enemy : *espManager->enemies) {
    //      Unity::Vector2 pos = /* WorldToScreen(enemy position) */;
    //      draw.DrawBox(Unity::Color(59, 130, 246, 255), 2, Unity::Rect(pos.x - 40, pos.y - 120, 80, 120));
    //      draw.DrawLine(Unity::Color(255, 255, 255, 180), 1.5f, Unity::Vector2(screenWidth / 2.0f, 0), pos);
    //      draw.DrawText(Unity::Color(255, 255, 255, 255), "Enemy", pos, 24);
    //      draw.DrawHorizontalHealthBar(pos, 80, 100, 65);
    //  }
}
