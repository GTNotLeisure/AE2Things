package com.asdflj.ae2thing.proxy;

import java.io.File;

import com.asdflj.ae2thing.api.adapter.terminal.item.EquippableTerminalHandler;
import com.asdflj.ae2thing.api.adapter.terminal.item.ITerminalHandler;
import com.asdflj.ae2thing.api.adapter.terminal.item.TerminalItems;
import com.asdflj.ae2thing.inventory.gui.GuiType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.p455w0rd.wirelesscraftingterminal.items.ItemWirelessCraftingTerminal;

import com.asdflj.ae2thing.AE2Thing;
import com.asdflj.ae2thing.api.AE2ThingAPI;
import com.asdflj.ae2thing.api.adapter.crafting.AECraftingTerminal;
import com.asdflj.ae2thing.api.adapter.crafting.WCTCraftingTerminal;
import com.asdflj.ae2thing.api.adapter.findit.MEChestAdapter;
import com.asdflj.ae2thing.api.adapter.findit.MEDriverAdapter;
import com.asdflj.ae2thing.api.adapter.findit.PartStorageBusAdapter;
import com.asdflj.ae2thing.api.adapter.terminal.item.FCBaseTerminalHandler;
import com.asdflj.ae2thing.api.adapter.terminal.item.UltraTerminalHandler;
import com.asdflj.ae2thing.api.adapter.terminal.item.WCTWirelessCraftingTerminalHandler;
import com.asdflj.ae2thing.api.container.EmptyContainerProviders;
import com.asdflj.ae2thing.api.container.FluidContainerProvider;
import com.asdflj.ae2thing.common.Config;
import com.asdflj.ae2thing.common.item.ItemBackpackTerminal;
import com.asdflj.ae2thing.common.item.ItemPatternModifier;
import com.asdflj.ae2thing.common.item.ItemWirelessDualInterfaceTerminal;
import com.asdflj.ae2thing.common.parts.PartThaumatoriumInterface;
import com.asdflj.ae2thing.common.parts.universal.PartUniversalStorageBus;
import com.asdflj.ae2thing.common.storage.StorageManager;
import com.asdflj.ae2thing.common.storage.backpack.OKBackpackHandler;
import com.asdflj.ae2thing.common.storage.infinityCell.InfinityStorageCellSnapshot;
import com.asdflj.ae2thing.common.tile.TileInfusionInterface;
import com.asdflj.ae2thing.integration.Mods;
import com.asdflj.ae2thing.inventory.item.PatternModifierInventory;
import com.asdflj.ae2thing.loader.BRLoader;
import com.asdflj.ae2thing.loader.ItemAndBlockHolder;
import com.asdflj.ae2thing.loader.PatternTerminalLoader;
import com.asdflj.ae2thing.loader.PatternTerminalMouseWheelLoader;
import com.asdflj.ae2thing.network.CPacketCellContentsRequest;
import com.asdflj.ae2thing.network.SPacketServerConfig;
import com.asdflj.ae2thing.network.wrapper.AE2ThingNetworkWrapper;
import com.asdflj.ae2thing.util.AspectUtil;
import com.asdflj.ae2thing.util.ItemGuiHintCache;
import com.asdflj.ae2thing.util.ModAndClassUtil;
import com.darkona.adventurebackpack.item.ItemAdventureBackpack;
import com.glodblock.github.common.item.ItemWirelessInterfaceTerminal;
import com.glodblock.github.common.item.ItemWirelessLevelTerminal;
import com.glodblock.github.common.item.ItemWirelessPatternTerminal;
import com.glodblock.github.common.item.ItemWirelessUltraTerminal;
import com.glodblock.github.common.parts.PartFluidStorageBus;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.parts.misc.PartStorageBus;
import appeng.util.Platform;
import appeng.util.item.AEFluidStackType;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import de.eydamos.backpack.item.ItemBackpackBase;
import forestry.storage.items.ItemBackpack;
import ic2.core.Ic2Items;
import thaumicenergistics.common.parts.PartEssentiaStorageBus;
import thaumicenergistics.common.storage.AEEssentiaStackType;

public class CommonProxy {

    public AE2ThingNetworkWrapper netHandler = new AE2ThingNetworkWrapper(AE2Thing.MODID);

    private int snapshotTick;

    public void scheduleClientTask(Runnable task) {
        throw new IllegalStateException("Cannot schedule a client task on a dedicated server");
    }

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        ModAndClassUtil.init();

        EmptyContainerProviders.register(AEFluidStackType.FLUID_STACK_TYPE, FluidContainerProvider.INSTANCE);
        if (Mods.THAUMIC_ENERGISTICS.isModLoaded()) {
            EmptyContainerProviders
                .register(AEEssentiaStackType.ESSENTIA_STACK_TYPE, _ -> AspectUtil.HELPER.createEmptyPhial());
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP player) {
            netHandler.sendTo(new SPacketServerConfig(Config.SERVER_SETTINGS.writeServerValues()), player);
        }
    }

    @SubscribeEvent
    public void worldLoad(WorldEvent.Load event) {
        if (Platform.isClient()) {
            ItemGuiHintCache.clear();
            return;
        }
        if (event.world.provider.dimensionId == 0) {
            final WorldSavedData data = event.world.mapStorage.loadData(StorageManager.class, AE2Thing.MODID);
            final StorageManager manager;
            if (data instanceof StorageManager loaded) {
                manager = loaded;
            } else {
                manager = new StorageManager(AE2Thing.MODID);
                event.world.mapStorage.setData(AE2Thing.MODID, manager);
            }
            manager.setSaveDirectory(diskDirectory(event.world));
            manager.loadFromDirectory();
        }
    }

    private static File diskDirectory(World world) {
        return new File(
            new File(
                world.getSaveHandler()
                    .getWorldDirectory(),
                "data"),
            "ae2thing");
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (++this.snapshotTick < 20) return;
        this.snapshotTick = 0;
        final MinecraftServer server = MinecraftServer.getServer();
        if (server == null || server.getConfigurationManager().playerEntityList.isEmpty()) return;
        InfinityStorageCellSnapshot.flushAll();
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP player) {
            CPacketCellContentsRequest.Handler.forget(player);
        }
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        if (Platform.isClient() || event.world.provider.dimensionId != 0) return;
        InfinityStorageCellSnapshot.flushAllForce();
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if (Platform.isClient() || event.world.provider.dimensionId != 0) return;
        InfinityStorageCellSnapshot.flushAllForce();
        InfinityStorageCellSnapshot.clear();
        final StorageManager manager = AE2ThingAPI.instance()
            .getStorageManager();
        if (manager != null) manager.flushDirtyDisks();
    }

    public void init(FMLInitializationEvent event) {
        AE2ThingAPI.instance()
            .terminal()
            .registerCraftingTerminal(new AECraftingTerminal());
        if (Mods.WIRELESS_CRAFTING_TERMINAL.isModLoaded()) {
            AE2ThingAPI.instance()
                .terminal()
                .registerCraftingTerminal(new WCTCraftingTerminal());
        }
        if (Mods.BLOCK_RENDERER.isModLoaded()) {
            new BRLoader().run();
        }
        new PatternTerminalMouseWheelLoader().run();
        new PatternTerminalLoader().run();

    }

    public void postInit(FMLPostInitializationEvent event) {
        if (Mods.BACKPACK.isModLoaded()) {
            AE2ThingAPI.instance()
                .addBackpackItem(ItemBackpackBase.class);
        }
        if (Mods.FORESTRY.isModLoaded()) {
            AE2ThingAPI.instance()
                .addBackpackItem(ItemBackpack.class);
        }
        if (Mods.ADVENTURE_BACKPACK.isModLoaded()) {
            AE2ThingAPI.instance()
                .addBackpackItem(ItemAdventureBackpack.class);
        }
        if (Mods.OK_BACKPACK.isModLoaded()) {
            AE2ThingAPI.instance()
                .addBackpackItem(OKBackpackHandler.getBackpackItemClass());
        }
        Upgrades.ORE_FILTER.registerItem(ItemAndBlockHolder.ITEM_INFINITY_CELL.stack(), 1);
        Upgrades.FUZZY.registerItem(ItemAndBlockHolder.ITEM_INFINITY_CELL.stack(), 1);
        Upgrades.INVERTER.registerItem(ItemAndBlockHolder.ITEM_INFINITY_CELL.stack(), 1);
        Upgrades.ORE_FILTER.registerItem(ItemAndBlockHolder.ITEM_INFINITY_FLUID_CELL.stack(), 1);
        Upgrades.FUZZY.registerItem(ItemAndBlockHolder.ITEM_INFINITY_FLUID_CELL.stack(), 1);
        Upgrades.INVERTER.registerItem(ItemAndBlockHolder.ITEM_INFINITY_FLUID_CELL.stack(), 1);
        Upgrades.ORE_FILTER.registerItem(ItemAndBlockHolder.ITEM_INFINITY_UNIVERSAL_CELL.stack(), 1);
        Upgrades.FUZZY.registerItem(ItemAndBlockHolder.ITEM_INFINITY_UNIVERSAL_CELL.stack(), 1);
        Upgrades.INVERTER.registerItem(ItemAndBlockHolder.ITEM_INFINITY_UNIVERSAL_CELL.stack(), 1);

        // Upgrades.ORE_FILTER.registerItem(ItemAndBlockHolder.TOGGLE_VIEW_CELL.stack(), 1);
        // Upgrades.FUZZY.registerItem(ItemAndBlockHolder.TOGGLE_VIEW_CELL.stack(), 1);
        // Upgrades.INVERTER.registerItem(ItemAndBlockHolder.TOGGLE_VIEW_CELL.stack(), 1);
        if (Mods.THAUMIC_ENERGISTICS.isModLoaded()) {
            Upgrades.LOCK_CRAFTING.registerItem(ItemAndBlockHolder.INFUSION_INTERFACE.stack(), 1);
            Upgrades.LOCK_CRAFTING.registerItem(ItemAndBlockHolder.THAUMATRIUM_INTERFACE.stack(), 1);
            Upgrades.ADVANCED_BLOCKING.registerItem(ItemAndBlockHolder.INFUSION_INTERFACE.stack(), 1);
            Upgrades.ADVANCED_BLOCKING.registerItem(ItemAndBlockHolder.THAUMATRIUM_INTERFACE.stack(), 1);
            Upgrades.CRAFTING.registerItem(ItemAndBlockHolder.INFUSION_INTERFACE.stack(), 1);
            Upgrades.CRAFTING.registerItem(ItemAndBlockHolder.THAUMATRIUM_INTERFACE.stack(), 1);
            Upgrades.PATTERN_CAPACITY.registerItem(ItemAndBlockHolder.INFUSION_INTERFACE.stack(), 3);
            Upgrades.PATTERN_CAPACITY.registerItem(ItemAndBlockHolder.THAUMATRIUM_INTERFACE.stack(), 3);
            Upgrades.REDSTONE.registerItem(ItemAndBlockHolder.EX_IO_PORT.stack(), 1);
            Upgrades.SPEED.registerItem(ItemAndBlockHolder.EX_IO_PORT.stack(), 3);
            Upgrades.SUPERSPEED.registerItem(ItemAndBlockHolder.EX_IO_PORT.stack(), 3);
            if (Mods.IC2.isModLoaded()) {
                FluidContainerProvider.INSTANCE.setDefaultContainer(Ic2Items.cell);
            }
            AEApi.instance()
                .registries()
                .interfaceTerminal()
                .register(TileInfusionInterface.class);
            AEApi.instance()
                .registries()
                .interfaceTerminal()
                .register(PartThaumatoriumInterface.class);
        }
        if (!Mods.SNL.isModLoaded()) {
            Upgrades.FUZZY.registerItem(ItemAndBlockHolder.UNIVERSAL_STORAGE_BUS.stack(), 1);
            Upgrades.ORE_FILTER.registerItem(ItemAndBlockHolder.UNIVERSAL_STORAGE_BUS.stack(), 1);
            Upgrades.INVERTER.registerItem(ItemAndBlockHolder.UNIVERSAL_STORAGE_BUS.stack(), 1);
            Upgrades.CAPACITY.registerItem(ItemAndBlockHolder.UNIVERSAL_STORAGE_BUS.stack(), 5);
            Upgrades.STICKY.registerItem(ItemAndBlockHolder.UNIVERSAL_STORAGE_BUS.stack(), 1);

            for (ItemStack bus : new ItemStack[] { ItemAndBlockHolder.UNIVERSAL_IMPORT_BUS.stack(),
                ItemAndBlockHolder.UNIVERSAL_EXPORT_BUS.stack() }) {
                Upgrades.FUZZY.registerItem(bus, 1);
                Upgrades.ORE_FILTER.registerItem(bus, 1);
                Upgrades.REDSTONE.registerItem(bus, 1);
                Upgrades.CAPACITY.registerItem(bus, 2);
                Upgrades.SPEED.registerItem(bus, 4);
                Upgrades.SUPERSPEED.registerItem(bus, 4);
                Upgrades.SUPERLUMINALSPEED.registerItem(bus, 4);
            }
        }
        AE2ThingAPI.instance()
            .terminal()
            .registerFindItStorageProvider(new MEChestAdapter());
        AE2ThingAPI.instance()
            .terminal()
            .registerFindItStorageProvider(new MEDriverAdapter());
        AE2ThingAPI.instance()
            .terminal()
            .registerFindItStorageProvider(new PartStorageBusAdapter(PartStorageBus.class));
        AE2ThingAPI.instance()
            .terminal()
            .registerFindItStorageProvider(new PartStorageBusAdapter(PartFluidStorageBus.class));
        if (!Mods.SNL.isModLoaded()) {
            AE2ThingAPI.instance()
                .terminal()
                .registerFindItStorageProvider(new PartStorageBusAdapter(PartUniversalStorageBus.class));
        }
        if (Mods.THAUMIC_ENERGISTICS.isModLoaded()) {
            AE2ThingAPI.instance()
                .terminal()
                .registerFindItStorageProvider(new PartStorageBusAdapter(PartEssentiaStorageBus.class));
        }
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalItem(ItemWirelessUltraTerminal.class, new UltraTerminalHandler());
        FCBaseTerminalHandler h = new FCBaseTerminalHandler();
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalItem(ItemWirelessLevelTerminal.class, h);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalItem(ItemWirelessInterfaceTerminal.class, h);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalItem(ItemWirelessPatternTerminal.class, h);
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalItem(new EquippableTerminalHandler(ItemWirelessDualInterfaceTerminal.class, GuiType.WIRELESS_DUAL_INTERFACE_TERMINAL));
        AE2ThingAPI.instance()
            .terminal()
            .registerTerminalItem(new EquippableTerminalHandler(ItemBackpackTerminal.class, GuiType.BACKPACK_TERMINAL) {
                @Override
                public boolean canConnect(ItemStack item, ITerminalHandler terminal, TerminalItems items, EntityPlayerMP player) {
                    return true;
                }
            });
        if (Mods.WIRELESS_CRAFTING_TERMINAL.isModLoaded()) {
            AE2ThingAPI.instance()
                .terminal()
                .registerTerminalItem(ItemWirelessCraftingTerminal.class, new WCTWirelessCraftingTerminalHandler());
        }

    }

    @SubscribeEvent
    public void pickUpEvent(EntityItemPickupEvent event) {
        if (Platform.isClient() || event.entityPlayer == null) return;
        try {
            EntityPlayer player = event.entityPlayer;
            ItemStack pattern = event.item.getEntityItem();
            if (pattern.getItem() != null && pattern.getItem() instanceof ICraftingPatternItem) {
                for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                    ItemStack item = player.inventory.mainInventory[i];
                    if (item != null && item.getItem() != null && item.getItem() instanceof ItemPatternModifier) {
                        PatternModifierInventory patternModifierInventory = new PatternModifierInventory(
                            item,
                            i,
                            player);
                        if (patternModifierInventory.injectItems(pattern)) {
                            event.item.setDead();
                            event.setCanceled(true);
                            return;
                        }
                    }
                }
                if (player.inventory.addItemStackToInventory(pattern)) {
                    event.item.setDead();
                    event.setCanceled(true);
                } else if (event.item.isEntityAlive()) {
                    event.item.delayBeforeCanPickup = 20;
                }
            }
        } catch (Exception ignored) {}

    }

    public void onLoadComplete(FMLLoadCompleteEvent event) {

    }

    public void serverStarting(FMLServerStartingEvent event) {

    }

    public void serverStopping(FMLServerStoppingEvent event) {
        InfinityStorageCellSnapshot.flushAllForce();
        InfinityStorageCellSnapshot.clear();
        StorageManager m = AE2ThingAPI.instance()
            .getStorageManager();
        if (m != null) {
            m.flushDirtyDisks();
            m.setDirty(true);
        }
    }
}
