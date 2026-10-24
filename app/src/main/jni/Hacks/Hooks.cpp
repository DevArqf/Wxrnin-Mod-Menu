//
// Created by github.com/seedhollow on 17/10/25.
// Template by Wxrnin
//
// Everything a hook needs is here, but nothing is hooked until you uncomment the examples in InitHooks().
//

#include "Hooks.hpp"
#include <dlfcn.h>
#include <cstring>
#include "dobby/dobby.h"
#include "Includes/Logger.h"
#include "Includes/Utils.hpp"
#include "KittyMemory/MemoryPatch.h"

// ---------------- il2cpp API (exported by libil2cpp.so, stable across game updates) ----------------

struct Il2CppApi {
    void *(*domain_get)();
    void *(*thread_attach)(void *domain);
    void **(*domain_get_assemblies)(void *domain, size_t *size);
    void *(*assembly_get_image)(void *assembly);
    const char *(*image_get_name)(void *image);
    void *(*class_from_name)(void *image, const char *ns, const char *name);
    void *(*class_get_method_from_name)(void *klass, const char *name, int argc);
    bool ok = false;
};
static Il2CppApi il2cpp;

static bool ResolveIl2CppApi() {
    void *h = dlopen(OBFUSCATE("libil2cpp.so"), RTLD_NOW | RTLD_NOLOAD);
    if (!h) return false;
#define API(field, sym) il2cpp.field = (decltype(il2cpp.field)) dlsym(h, OBFUSCATE(sym))
    API(domain_get, "il2cpp_domain_get");
    API(thread_attach, "il2cpp_thread_attach");
    API(domain_get_assemblies, "il2cpp_domain_get_assemblies");
    API(assembly_get_image, "il2cpp_assembly_get_image");
    API(image_get_name, "il2cpp_image_get_name");
    API(class_from_name, "il2cpp_class_from_name");
    API(class_get_method_from_name, "il2cpp_class_get_method_from_name");
#undef API
    il2cpp.ok = il2cpp.domain_get && il2cpp.thread_attach && il2cpp.domain_get_assemblies && il2cpp.assembly_get_image &&
                il2cpp.image_get_name && il2cpp.class_from_name && il2cpp.class_get_method_from_name;
    return il2cpp.ok;
}

static void *FindImage(const char *imageName) {
    size_t count = 0;
    void **assemblies = il2cpp.domain_get_assemblies(il2cpp.domain_get(), &count);
    for (size_t i = 0; i < count; i++) {
        void *image = il2cpp.assembly_get_image(assemblies[i]);
        const char *n = image ? il2cpp.image_get_name(image) : nullptr;
        if (n && strcmp(n, imageName) == 0) return image;
    }
    return nullptr;
}

static void *FindClass(const char *imageName, const char *ns, const char *name) {
    void *image = FindImage(imageName);
    void *k = image ? il2cpp.class_from_name(image, ns, name) : nullptr;
    if (!k) LOGE(OBFUSCATE("Class not found: %s.%s"), ns, name);
    return k;
}

// MethodInfo* of a method; its first field is the native method pointer
static void *FindMethod(const char *imageName, const char *ns, const char *klass, const char *method, int argc) {
    void *k = FindClass(imageName, ns, klass);
    void *mi = k ? il2cpp.class_get_method_from_name(k, method, argc) : nullptr;
    if (!mi) LOGE(OBFUSCATE("Not found: %s.%s::%s(%d)"), ns, klass, method, argc);
    return mi;
}

static void *MethodPointer(void *methodInfo) { return methodInfo ? *reinterpret_cast<void **>(methodInfo) : nullptr; }

static int gHooked = 0, gFailed = 0;

/* Hook a method by name: survives game updates as long as the names do not change.
 * image: "Assembly-CSharp.dll", "UnityEngine.CoreModule.dll"...   argc: number of parameters (without "this")
 */
[[maybe_unused]]
static void HookByName(const char *image, const char *ns, const char *klass, const char *method, int argc, void *replacement, void **orig) {
    void *target = MethodPointer(FindMethod(image, ns, klass, method, argc));
    if (target && DobbyHook(target, replacement, orig) == 0) {
        LOGI(OBFUSCATE("Hooked %s::%s"), klass, method);
        gHooked++;
    } else {
        LOGE(OBFUSCATE("Hook failed: %s::%s"), klass, method);
        gFailed++;
    }
}

/* Hook a method by its RVA from a dump (e.g. Il2CppDumper's dump.cs). Must be updated with every game update. */
[[maybe_unused]]
static void HookByOffset(const char *lib, uintptr_t offset, void *replacement, void **orig) {
    void *target = (void *) getAbsoluteAddress(lib, offset);
    if (target && DobbyHook(target, replacement, orig) == 0) {
        LOGI(OBFUSCATE("Hooked %s+0x%lx"), lib, (unsigned long) offset);
        gHooked++;
    } else {
        LOGE(OBFUSCATE("Hook failed: %s+0x%lx"), lib, (unsigned long) offset);
        gFailed++;
    }
}

/* Call a game method yourself (from the game thread, e.g. inside Hooks::Update):
 *
 *   static void *giveMethod = FindMethod(ACS, "", "Player", "AddCoins", 1);
 *   ((void (*)(void *, int32_t, void *)) MethodPointer(giveMethod))(player, 1000, giveMethod);
 */

#define ACS OBFUSCATE("Assembly-CSharp.dll")

// ---------------- Init ----------------

void Hooks::InitHooks() {
    if (!ResolveIl2CppApi()) {
        LOGE(OBFUSCATE("il2cpp API not resolved"));
        return;
    }
    il2cpp.thread_attach(il2cpp.domain_get());

    // ---- By name (recommended) ----
    // HookByName(ACS, OBFUSCATE("Game"), OBFUSCATE("Player"), OBFUSCATE("AddCoins"), 1, (void *) ExampleMethod, (void **) &orig_ExampleMethod);
    // HookByName(ACS, OBFUSCATE("Game"), OBFUSCATE("Player"), OBFUSCATE("get_Speed"), 0, (void *) ExampleGetter, (void **) &orig_ExampleGetter);
    // HookByName(OBFUSCATE("UnityEngine.UI.dll"), OBFUSCATE("UnityEngine.EventSystems"), OBFUSCATE("EventSystem"), OBFUSCATE("Update"), 0,
    //            (void *) Update, (void **) &orig_Update);

    // ---- By offset ----
    // HookByOffset(OBFUSCATE("libil2cpp.so"), 0x123456, (void *) ExampleMethod, (void **) &orig_ExampleMethod);

    // ---- Hex patch (KittyMemory) ----
    // Keep the MemoryPatch somewhere global, then call .Modify() to apply and .Restore() to undo (e.g. from OnFeatureChanged)
    // static MemoryPatch patch = MemoryPatch::createWithHex(OBFUSCATE("libil2cpp.so"), 0x123456, OBFUSCATE("20 00 80 D2 C0 03 5F D6")); // return 1
    // patch.Modify();

    LOGI(OBFUSCATE("Hooks installed: %d ok, %d failed"), gHooked, gFailed);
}

// ---------------- Examples ----------------

void (*Hooks::orig_ExampleMethod)(void *, int32_t, void *) = nullptr;
void Hooks::ExampleMethod(void *self, int32_t amount, void *mi) {
    auto &v = Vars::Template;
    // change an argument before the game sees it
    if (v.exampleSwitch && amount > 0) amount *= v.exampleSlider;
    orig_ExampleMethod(self, amount, mi);
}

float (*Hooks::orig_ExampleGetter)(void *, void *) = nullptr;
float Hooks::ExampleGetter(void *self, void *mi) {
    float value = orig_ExampleGetter(self, mi);
    // change the return value
    return Vars::Template.exampleCheckBox ? value * Vars::Template.exampleFloat : value;
}

void (*Hooks::orig_Update)(void *, void *) = nullptr;
void Hooks::Update(void *self, void *mi) {
    // one-shot actions requested by IButton
    if (Vars::Template.pendingAction.exchange(false)) {
        LOGI(OBFUSCATE("Example action ran on the game thread"));
    }
    orig_Update(self, mi);
}
