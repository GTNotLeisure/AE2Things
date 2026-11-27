package com.asdflj.ae2thing.coremod.mixin.ae;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.client.me.AdvItemRepo;
import com.asdflj.ae2thing.client.me.IDisplayRepoExtend;
import com.asdflj.ae2thing.client.pin.IPlayerPinRepo;
import com.asdflj.ae2thing.client.pin.PlayerPinLayout;
import com.asdflj.ae2thing.coremod.hooker.CoreModHooksClient;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IDisplayRepo;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.me.ItemRepo;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

@SuppressWarnings("UnusedMixin")
@Mixin(ItemRepo.class)
public abstract class MixinItemRepo implements IDisplayRepo, IDisplayRepoExtend, IPlayerPinRepo {

    @Shadow(remap = false)
    @Final
    private IScrollSource src;

    @Unique
    private PlayerPinLayout ae2t$pinLayout;

    @Shadow(remap = false)
    private IAEStack<?>[] pinsRepo;

    @Unique
    private IAEStack<?>[] ae2t$pinSource;

    @Unique
    private IAEStack<?>[] ae2t$occupiedPins;

    @Redirect(
        method = "postUpdate(Lappeng/api/storage/data/IAEStack;)V",
        at = @At(
            value = "FIELD",
            target = "Lappeng/client/me/ItemRepo;pinsRepo:[Lappeng/api/storage/data/IAEStack;",
            opcode = Opcodes.GETFIELD),
        remap = false)
    private IAEStack<?>[] ae2t$occupiedPinsForUpdate(ItemRepo repo) {
        // setAEPins replaces the backing array. Stock updates mutate the stack objects in place.
        if (ae2t$pinSource != pinsRepo) {
            List<IAEStack<?>> occupied = new ArrayList<>();
            for (IAEStack<?> pin : pinsRepo) {
                if (pin != null) occupied.add(pin);
            }
            ae2t$occupiedPins = occupied.toArray(new IAEStack<?>[0]);
            ae2t$pinSource = pinsRepo;
        }
        return ae2t$occupiedPins;
    }

    @Override
    public void ae2t$setPinLayout(PlayerPinLayout layout) {
        this.ae2t$pinLayout = (Object) this instanceof AdvItemRepo ? null : layout;
    }

    @Override
    public int ae2t$playerPinIndex(int screenIndex) {
        return ae2t$pinLayout == null ? -1 : ae2t$pinLayout.playerIndex(screenIndex, src.getCurrentScroll());
    }

    @Override
    public boolean ae2t$isPinPadding(int screenIndex) {
        return ae2t$pinLayout != null && ae2t$pinLayout.isPadding(screenIndex, src.getCurrentScroll());
    }

    @Unique
    private IAEStack<?> ae2t$compositeStack(int screenIndex) {
        int absolute = ae2t$pinLayout.absoluteIndex(screenIndex, src.getCurrentScroll());
        int pinIndex = ae2t$playerPinIndex(screenIndex);
        if (pinIndex >= 0) return getAEPin(pinIndex);
        int viewIndex = absolute - ae2t$pinLayout.prefixSize();
        return viewIndex >= 0 && viewIndex < view.size() ? view.get(viewIndex) : null;
    }

    @Inject(method = "getReferenceStack", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$referenceStack(int idx, CallbackInfoReturnable<IAEStack<?>> cir) {
        if (ae2t$pinLayout != null && ae2t$pinLayout.scrolling()) cir.setReturnValue(ae2t$compositeStack(idx));
    }

    @Inject(method = "getReferenceItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2t$referenceItem(int idx, CallbackInfoReturnable<IAEItemStack> cir) {
        if (ae2t$pinLayout == null || !ae2t$pinLayout.scrolling()) return;
        IAEStack<?> stack = ae2t$compositeStack(idx);
        cir.setReturnValue(stack instanceof IAEItemStack item ? item : null);
    }

    @Inject(method = "size", at = @At("RETURN"), cancellable = true, remap = false)
    private void ae2t$compositeSize(CallbackInfoReturnable<Integer> cir) {
        if (ae2t$pinLayout != null) cir.setReturnValue(cir.getReturnValue() + ae2t$pinLayout.prefixSize());
    }

    @Redirect(
        method = "lambda$getFilter$7",
        at = @At(value = "INVOKE", target = "Lappeng/api/storage/data/IAEStack;getModId()Ljava/lang/String;"),
        remap = false)
    private static String ae2thing$getModId(IAEStack<?> stack) {
        return CoreModHooksClient.getModId(stack);
    }

    @Redirect(
        method = "lambda$getFilter$9",
        at = @At(value = "INVOKE", target = "Lappeng/api/storage/data/IAEStack;getDisplayName()Ljava/lang/String;"),
        remap = false)
    private static String ae2thing$getDisplayName(IAEStack<?> stack) {
        return CoreModHooksClient.getItemDisplayName(stack);
    }

    @Redirect(
        method = { "lambda$getFilter$6", "lambda$getFilter$9" },
        at = @At(value = "INVOKE", target = "Lappeng/util/Platform;getTooltip(Ljava/lang/Object;)Ljava/util/List;"),
        remap = false)
    private static List<String> ae2thing$getTooltip(Object o) {
        return CoreModHooksClient.getTooltip(o);
    }

    @Shadow(remap = false)
    @Final
    private ArrayList<IAEStack<?>> view;

    @Shadow(remap = false)
    @Final
    private IItemList<IAEStack<?>> list;

    @Shadow(remap = false)
    private boolean paused;

    @Unique
    private void ae2t$setAsEmpty(int i) {
        this.view.add(i, null);
    }

    @Unique
    private final Minecraft ae2t$mc = Minecraft.getMinecraft();

    @Redirect(
        method = "updateView",
        at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z"),
        remap = false)
    public boolean add(ArrayList<Object> view, Object o) {
        return ae2t$addView(view, o);
    }

    @Unique
    private boolean ae2t$addView(ArrayList<Object> view, Object o) {
        GuiScreen gui = ae2t$mc.currentScreen;
        if (gui == null) return view.add(o);
        if (!AE2ThingAPI.instance()
            .terminal()
            .isPinTerminal(gui)) {
            return view.add(o);
        } else if ((o instanceof IAEItemStack is && AE2ThingAPI.instance()
            .getPinned()
            .isPinnedItem(is))) {
                return false;
            } else {
                return view.add(o);
            }
    }

    @Redirect(
        method = "addEntriesToView",
        at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;add(Ljava/lang/Object;)Z"),
        remap = false,
        require = 0)
    public boolean addEntriesToView(ArrayList<Object> view, Object o) {
        return ae2t$addView(view, o);
    }

    @Inject(method = "updateView", at = @At(value = "HEAD"), remap = false)
    public void updateViewHead(CallbackInfo ci) {
        if (((Object) this) instanceof AdvItemRepo repo) {
            if (!repo.hasCache()) {
                repo.getLock()
                    .lock();
                ae2t$viewFilter();
                repo.getLock()
                    .unlock();
            }
        } else {
            ae2t$viewFilter();
        }
    }

    @Unique
    private void ae2t$viewFilter() {
        this.view.removeIf(Objects::isNull);
    }

    @Inject(method = "updateView", at = @At(value = "TAIL"), remap = false)
    public void updateViewTail(CallbackInfo ci) {
        GuiScreen gui = ae2t$mc.currentScreen;
        if (gui == null) return;
        if (!AE2ThingAPI.instance()
            .terminal()
            .isPinTerminal(gui)) {
            return;
        }
        final List<IAEItemStack> pinItems = AE2ThingAPI.instance()
            .getPinned()
            .getSortedPinnedItems();
        if (pinItems.isEmpty()) {
            return;
        }

        Set<IAEStack<?>> itemSet = new ObjectOpenHashSet<>(this.view);
        for (int i = 0; i < AE2ThingAPI.instance()
            .getPinned()
            .getMaxPinSize(); i++) {
            if (i >= pinItems.size()) {
                this.ae2t$setAsEmpty(i);
                continue;
            }
            IAEItemStack is = pinItems.get(i);
            IAEStack<?> item = this.list.findPrecise(is);
            if (item != null) {
                if (itemSet.add(item)) {
                    this.view.add(i, item);
                }
                continue;
            }
            this.ae2t$setAsEmpty(i);
        }
    }

    @Override
    public void ae2t$setAdvRepoPause(boolean pause) {
        this.paused = pause;
    }
}
