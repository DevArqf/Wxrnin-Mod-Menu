//
// Created by github.com/seedhollow on 17/10/25.
//

#ifndef IL2CPPANDROID_VISUALS_HPP
#define IL2CPPANDROID_VISUALS_HPP

#include "Includes/Draw.hpp"
#include "Hacks/Vars.h"
#include "Includes/Logger.h"
#include "Includes/obfuscate.h"
#include "UnityResolve/UnityResolve.hpp"

class Visuals {
public:
    // Called by the overlay (UI thread) every frame; draws the ESP snapshot taken on the game thread
    static void Update(Draw draw, int screenWidth, int screenHeight);

private:
    using Unity = UnityResolve::UnityType;
};

#endif //IL2CPPANDROID_VISUALS_HPP
