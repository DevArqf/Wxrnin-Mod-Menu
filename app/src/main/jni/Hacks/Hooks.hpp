//
// Created by rosetta on 05/07/2024.
// Template by Wxrnin
//

#ifndef HOOKS_H
#define HOOKS_H

#include <cstdint>
#include "Vars.h"
#include "UnityResolve/UnityResolve.hpp"
#include "Includes/ObscuredTypes.hpp"
#include "Includes/obfuscate.h"

/* Put your hooks here.
 *
 * il2cpp ARM64 calling convention: (this, args..., const MethodInfo*)
 * Static methods have no "this".
 *
 * Every hook is a pair: a pointer to the original function and your replacement.
 *
 *   // Namespace.Class.Method(int amount)
 *   static void (*orig_ExampleMethod)(void *self, int32_t amount, void *mi);
 *   static void ExampleMethod(void *self, int32_t amount, void *mi);
 *
 * Install it in Hooks::InitHooks() (Hooks.cpp), either by name or by offset.
 */
class Hooks {
public:
    static void InitHooks();

    // Example: an instance method that receives an int
    static void (*orig_ExampleMethod)(void *self, int32_t amount, void *mi);
    static void ExampleMethod(void *self, int32_t amount, void *mi);

    // Example: a getter whose return value is changed
    static float (*orig_ExampleGetter)(void *self, void *mi);
    static float ExampleGetter(void *self, void *mi);

    // Example: a method that runs every frame on the game's main thread (UnityEngine.EventSystems.EventSystem.Update)
    // Use it for anything that must call game code: buttons, spawning, reading the scene...
    static void (*orig_Update)(void *self, void *mi);
    static void Update(void *self, void *mi);
};

#endif // HOOKS_H
