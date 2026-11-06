package com.vcwdfca.appbot.util;

import java.text.DecimalFormat;

import net.minecraft.util.StatCollector;

import com.vcwdfca.appbot.ae.AEManaStackType;

public final class ManaFormat {

    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.##");

    private ManaFormat() {}

    public static String number(long mana) {
        return FORMAT.format(mana / (double) AEManaStackType.AMOUNT_PER_UNIT);
    }

    public static String unit() {
        return StatCollector.translateToLocal("ab.mana.unit");
    }

    public static String format(long mana) {
        return number(mana) + " " + unit();
    }
}
