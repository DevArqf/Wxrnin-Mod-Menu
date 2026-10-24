//
// Created by rosetta on 01/06/2024.
// Template by Wxrnin
//

// Store the values of your features here.
// The menu writes them from the UI thread (FeatureModule::OnFeatureChanged),
// your hooks read them from the game thread. Use std::atomic for "do it once" flags.

#pragma once
#include <atomic>
#include <string>

namespace Vars {

    // One field per example widget in FeatureModule.cpp, rename them to fit your game
    struct Template_t {
        // ISwitch / ICheckBox
        bool  exampleSwitch   = false;
        bool  exampleCheckBox = false;

        // ISlider (int range)
        int   exampleSlider   = 1;

        // ISpinner / IRadioButton (index of the selected option)
        int   exampleSpinner  = 0;
        int   exampleRadio    = 0;

        // IInputInt / IInputFloat / IInputText
        int         exampleInt    = 0;
        float       exampleFloat  = 1.0f;
        std::string exampleText;

        // IButton: the UI thread raises the flag, the game thread does the work and clears it
        std::atomic<bool> pendingAction{false};

        // Visuals (drawn by Hacks/Visuals.cpp when Menu.ENABLE_OVERLAY is true)
        bool  crosshair       = false;
        int   crosshairSize   = 20;
    };

    inline Template_t Template;
}
