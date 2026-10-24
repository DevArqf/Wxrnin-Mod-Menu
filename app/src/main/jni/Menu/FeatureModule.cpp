//
// Created by rosetta on 13/09/2024.
// Template by Wxrnin
//

#include "FeatureModule.hpp"
#include "Menu/Menu.hpp"
#include "Includes/Utils.hpp"


void FeatureModule::OnDraw(JNIEnv *env, jclass clazz, jobject draw_view, jobject canvas) {
    Draw draw(env, draw_view, canvas);
    if (draw.isValid()) {
        Visuals::Update(draw, draw.getWidth(), draw.getHeight());
    } else {
        LOGE(OBFUSCATE("Draw is not valid in OnDraw"));
    }
}

jstring FeatureModule::GetFeatureList(JNIEnv *env, jobject context) {
    Widget::Clear();
    RegisterFeatures();

    std::string json = Widget::ToJsonString();
    return env->NewStringUTF(json.c_str());
}

// Feature IDs, OnFeatureChanged receives them. Keep them unique: saved values are stored by ID + name.
enum FeatureId : int {
    // Home
    ID_SWITCH = 1,
    ID_CHECKBOX,
    ID_TOAST_BUTTON,
    ID_ACTION_BUTTON,
    ID_LINK,
    // Values
    ID_SLIDER,
    ID_INPUT_INT,
    ID_INPUT_FLOAT,
    ID_INPUT_TEXT,
    // Choices
    ID_SPINNER,
    ID_RADIO,
    // Visuals
    ID_CROSSHAIR,
    ID_CROSSHAIR_SIZE,
    // More (inside collapses)
    ID_NESTED_SWITCH,
    ID_NESTED_SLIDER,
    ID_DEEP_CHECKBOX,
};

/** Every widget the menu supports, one example each. Replace them with your own features.
 *
 * - Each ITab is a sidebar entry, see ITab.hpp for the icon names
 * - Widgets added outside of an ITab go into a "General" tab
 * - A "Settings" tab (save settings, credits, game info) is always added last
 * - Names and descriptions accept simple HTML: <b>, <i>, <u>, <font color=#38BDF8>...</font>
 **/
void FeatureModule::RegisterFeatures() {
    auto &v = Vars::Template;

    Widget::Add(ITab(OBFUSCATE("Home"), OBFUSCATE("star"), {
            ITab::Child(ITextView(OBFUSCATE("Welcome to the <font color=#38BDF8><b>Wxrnin</b></font> template. Every widget type has an example in these tabs."))),

            ITab::Child(ICategory(OBFUSCATE("Toggles"))),
            ITab::Child(ISwitch(ID_SWITCH, OBFUSCATE("Example Switch"), OBFUSCATE("Tap the <b>?</b> badge to show this description"), v.exampleSwitch)),
            ITab::Child(ICheckBox(ID_CHECKBOX, OBFUSCATE("Example Check Box"), OBFUSCATE("Works like a switch, drawn as a check box"), v.exampleCheckBox)),

            ITab::Child(ICategory(OBFUSCATE("Buttons"))),
            ITab::Child(IButton(ID_TOAST_BUTTON, OBFUSCATE("Show Rainbow Toast"))),
            ITab::Child(IButton(ID_ACTION_BUTTON, OBFUSCATE("Run Example Action"))),
            ITab::Child(IButtonLink(ID_LINK, OBFUSCATE("Open a Link"), OBFUSCATE("https://github.com"))),
    }));

    Widget::Add(ITab(OBFUSCATE("Values"), OBFUSCATE("bolt"), {
            ITab::Child(ICategory(OBFUSCATE("Slider"))),
            ITab::Child(ISlider(ID_SLIDER, OBFUSCATE("Example Slider"), 1, 100)),

            ITab::Child(ICategory(OBFUSCATE("Inputs"))),
            ITab::Child(IInputInt(ID_INPUT_INT, OBFUSCATE("Whole Number"), 999999)),
            ITab::Child(IInputFloat(ID_INPUT_FLOAT, OBFUSCATE("Decimal Number"), 100.0f, 2)),
            ITab::Child(IInputText(ID_INPUT_TEXT, OBFUSCATE("Text"))),
    }));

    Widget::Add(ITab(OBFUSCATE("Choices"), OBFUSCATE("target"), {
            ITab::Child(ICategory(OBFUSCATE("Dropdown"))),
            ITab::Child(ISpinner(ID_SPINNER, OBFUSCATE("Example Spinner"), {
                    OBFUSCATE("Option One"), OBFUSCATE("Option Two"), OBFUSCATE("<font color=#38BDF8>Option Three</font>")
            })),

            ITab::Child(ICategory(OBFUSCATE("Radio"))),
            ITab::Child(IRadioButton(ID_RADIO, OBFUSCATE("Example Radio"), {
                    OBFUSCATE("Low"), OBFUSCATE("Medium"), OBFUSCATE("High")
            })),
    }));

    Widget::Add(ITab(OBFUSCATE("Visuals"), OBFUSCATE("eye"), {
            ITab::Child(ITextView(OBFUSCATE("Drawn by <b>Hacks/Visuals.cpp</b>. Set <font color=#38BDF8>Menu.ENABLE_OVERLAY</font> to true to create the overlay."))),
            ITab::Child(ISwitch(ID_CROSSHAIR, OBFUSCATE("Crosshair"), OBFUSCATE("Example drawing in the screen center"), v.crosshair)),
            ITab::Child(ISlider(ID_CROSSHAIR_SIZE, OBFUSCATE("Crosshair Size"), 10, 100)),
    }));

    Widget::Add(ITab(OBFUSCATE("More"), OBFUSCATE("hex"), {
            ITab::Child(ITextView(OBFUSCATE("An <b>ICollapse</b> opens its children as a sub-page, and can be nested."))),
            ITab::Child(ICollapse(OBFUSCATE("Example Collapse"), {
                    ICollapse::Child(ISwitch(ID_NESTED_SWITCH, OBFUSCATE("Nested Switch"))),
                    ICollapse::Child(ISlider(ID_NESTED_SLIDER, OBFUSCATE("Nested Slider"), 0, 10)),
                    ICollapse::Child(ICollapse(OBFUSCATE("Deeper Collapse"), {
                            ICollapse::Child(ICheckBox(ID_DEEP_CHECKBOX, OBFUSCATE("Deep Check Box"))),
                    })),
            })),
    }));
}

void FeatureModule::OnFeatureChanged(JNIEnv *env, jclass clazz, jobject obj, jint featNum, jstring featName, jint value, jfloat floatValue, jboolean boolean, jstring str) {

    const char* name = env->GetStringUTFChars(featName, nullptr);
    const char* strVal = str != nullptr ? env->GetStringUTFChars(str, nullptr) : "";

    LOGD(OBFUSCATE("Feature changed: [%d] %s | value=%d | float=%f | bool=%d | str=%s"),
         featNum, name, value, floatValue, boolean, strVal);

    // obj is the Context, so feedback like a toast can be shown right away
    auto &v = Vars::Template;
    switch (featNum) {
        case ID_SWITCH:         v.exampleSwitch   = boolean;    break;
        case ID_CHECKBOX:       v.exampleCheckBox = boolean;    break;
        case ID_TOAST_BUTTON:
            Menu::showToast(env, obj, OBFUSCATE("Hello from Wxrnin!"), ToastLength::LENGTH_SHORT);
            break;
        case ID_ACTION_BUTTON:
            v.pendingAction = true;  // picked up by Hooks::Update on the game thread
            Menu::showToast(env, obj, OBFUSCATE("Example action queued"), ToastLength::LENGTH_SHORT);
            break;
        case ID_SLIDER:         v.exampleSlider   = value;      break;
        case ID_INPUT_INT:      v.exampleInt      = value;      break;
        case ID_INPUT_FLOAT:    v.exampleFloat    = floatValue; break;
        case ID_INPUT_TEXT:     v.exampleText     = strVal;     break;
        case ID_SPINNER:        v.exampleSpinner  = value;      break;
        case ID_RADIO:          v.exampleRadio    = value;      break;
        case ID_CROSSHAIR:      v.crosshair       = boolean;    break;
        case ID_CROSSHAIR_SIZE: v.crosshairSize   = value;      break;
        default: break;
    }

    env->ReleaseStringUTFChars(featName, name);
    if (str != nullptr) env->ReleaseStringUTFChars(str, strVal);
}
