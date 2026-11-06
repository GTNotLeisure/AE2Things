package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.vcwdfca.appbot.item.ItemManaMarker;
import com.vcwdfca.appbot.util.ManaFormat;

import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.core.localization.ButtonToolTips;

@Mixin(GuiMEMonitorable.class)
public class MixinGuiMEMonitorable {

    @Inject(method = "handleItemTooltip", at = @At("RETURN"), remap = false)
    private void ae2thing$manaStoredLine(ItemStack stack, int mouseX, int mouseY, List<String> tooltip,
        CallbackInfoReturnable<List<String>> cir) {
        if (tooltip == null || !ItemManaMarker.isMarker(stack)) {
            return;
        }
        final String stored = ButtonToolTips.ItemsStored.getLocal();
        final int pct = stored.indexOf('%');
        if (pct <= 0) {
            return;
        }
        final String prefix = stored.substring(0, pct)
            .trim();
        for (int i = 0; i < tooltip.size(); i++) {
            final String plain = EnumChatFormatting.getTextWithoutFormattingCodes(tooltip.get(i));
            if (plain == null || !plain.startsWith(prefix)) {
                continue;
            }
            final String raw = plain.substring(prefix.length())
                .trim()
                .replace(",", "");
            String amount = raw;
            try {
                amount = ManaFormat.format(Long.parseLong(raw));
            } catch (NumberFormatException ignored) {}
            tooltip.set(i, EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("ab.mana.stored", amount));
            break;
        }
    }
}
