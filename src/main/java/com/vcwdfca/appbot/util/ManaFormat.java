package com.vcwdfca.appbot.util;

import java.text.DecimalFormat;

import com.vcwdfca.appbot.ae.AEManaStackType;
import com.vcwdfca.appbot.client.localization.Tooltips;

public final class ManaFormat {

    private static final DecimalFormat FORMAT = new DecimalFormat("#,##0.##");

    private ManaFormat() {}

    public static String number(long mana) {
        return FORMAT.format(mana / (double) AEManaStackType.AMOUNT_PER_UNIT);
    }

    public static String unit() {
        return Tooltips.unit.getLocal();
    }

    public static String format(long mana) {
        return number(mana) + " " + unit();
    }
}
