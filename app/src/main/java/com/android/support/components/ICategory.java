package com.android.support.components;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.support.entity.FeatureEntity;

import org.lsposed.lsparanoid.Obfuscate;

import java.util.Locale;

@Obfuscate
public class ICategory {

    private final Ui ui;

    public ICategory(Ui ui) {
        this.ui = ui;
    }

    public View build(FeatureEntity item) {
        return build(item.name);
    }

    /** Small diamond, the upper-case title in sky blue, then a line that fades out. */
    public View build(String name) {
        LinearLayout row = new LinearLayout(ui.context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(ui.dp(4), ui.dp(10), 0, 0);

        View diamond = new View(ui.context);
        diamond.setBackground(ui.gradient(1, Theme.ACCENT_LIGHT, Theme.ACCENT_DEEP));
        diamond.setRotation(45);
        row.addView(diamond, new LinearLayout.LayoutParams(ui.dp(5), ui.dp(5)));

        TextView text = ui.text(Ui.html(name.toUpperCase(Locale.ROOT)), Theme.TEXT_SMALL - 1f, Theme.ACCENT_LIGHT, ui.medium);
        text.setLetterSpacing(0.14f);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        textParams.leftMargin = ui.dp(8);
        row.addView(text, textParams);

        View line = new View(ui.context);
        line.setBackground(ui.gradient(0, Theme.DIVIDER | 0x30000000, Theme.DIVIDER & 0x00FFFFFF));
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(0, Math.max(1, ui.dp(1)), 1f);
        lineParams.leftMargin = ui.dp(10);
        row.addView(line, lineParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        params.bottomMargin = ui.dp(8);
        row.setLayoutParams(params);
        return row;
    }
}
