package com.vcwdfca.appbot.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.vcwdfca.appbot.AppBotTabs;
import com.vcwdfca.appbot.item.ItemBlockFluixPool;
import com.vcwdfca.appbot.tile.TileFluixPool;
import com.vcwdfca.appbot.util.FluixPoolRenderId;

import cpw.mods.fml.common.registry.GameRegistry;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.wand.IWandHUD;
import vazkii.botania.api.wand.IWandable;
import vazkii.botania.common.block.BlockModContainer;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.mana.TilePool;

public class BlockFluixPool extends BlockModContainer<TileFluixPool> implements IWandHUD, IWandable {

    public static final String NAME = "fluix_mana_pool";

    public BlockFluixPool() {
        super(Material.rock);
        setHardness(2.0F);
        setResistance(10.0F);
        setStepSound(soundTypeStone);
        setBlockName(NAME);
        setBlockBounds(0F, 0F, 0F, 1F, 0.5F, 1F);
        setCreativeTab(AppBotTabs.INSTANCE);
    }

    @Override
    protected boolean shouldRegisterInNameSet() {
        return false;
    }

    @Override
    public Block setBlockName(String name) {
        GameRegistry.registerBlock(this, ItemBlockFluixPool.class, name);
        return super.setBlockName(name);
    }

    @Override
    public TileFluixPool createNewTileEntity(World world, int meta) {
        return new TileFluixPool();
    }

    @Override
    public int getRenderType() {
        return FluixPoolRenderId.id;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        this.blockIcon = register.registerIcon("appliedenergistics2:BlockFluix");
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return this.blockIcon != null ? this.blockIcon : ModBlocks.livingrock.getIcon(side, 0);
    }

    @Override
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List<AxisAlignedBB> list,
        Entity collider) {
        final float f = 1F / 16F;
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, f, 1.0F);
        super.addCollisionBoxesToList(world, x, y, z, mask, list, collider);
        setBlockBounds(0.0F, 0.0F, 0.0F, f, 0.5F, 1.0F);
        super.addCollisionBoxesToList(world, x, y, z, mask, list, collider);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, f);
        super.addCollisionBoxesToList(world, x, y, z, mask, list, collider);
        setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
        super.addCollisionBoxesToList(world, x, y, z, mask, list, collider);
        setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 0.5F, 1.0F);
        super.addCollisionBoxesToList(world, x, y, z, mask, list, collider);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side == ForgeDirection.DOWN;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity collider) {
        if (!(collider instanceof EntityItem)) return;
        final TilePool pool = (TilePool) world.getTileEntity(x, y, z);
        if (pool != null && pool.collideEntityItem((EntityItem) collider)) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(world, x, y, z);
        }
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        final TilePool pool = (TilePool) world.getTileEntity(x, y, z);
        if (pool == null) return 0;
        final int cap = pool.manaCap > 0 ? pool.manaCap : TilePool.MAX_MANA;
        int val = (int) ((double) pool.getCurrentMana() / (double) cap * 15.0);
        if (pool.getCurrentMana() > 0) val = Math.max(val, 1);
        return val;
    }

    @Override
    public void renderHUD(Minecraft mc, ScaledResolution res, World world, int x, int y, int z) {
        final TilePool pool = (TilePool) world.getTileEntity(x, y, z);
        if (pool != null) pool.renderHUD(mc, res);
    }

    @Override
    public boolean onUsedByWand(EntityPlayer player, ItemStack stack, World world, int x, int y, int z, int side) {
        final TilePool pool = (TilePool) world.getTileEntity(x, y, z);
        if (pool == null) return false;
        pool.onWanded(player, stack);
        return true;
    }
}
