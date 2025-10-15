package com.circulation.ae_chisel.proxy;

import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTile;
import com.circulation.ae_chisel.AppliedChisel;
import com.circulation.ae_chisel.client.ContainerAEChisel;
import com.circulation.ae_chisel.common.BlockAEChisel;
import com.circulation.ae_chisel.common.TileEntityAEChisel;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.Nullable;

public class CommonProxy implements IGuiHandler {

    public void preInit() {
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.registerGuiHandler(AppliedChisel.instance, this);
        GameRegistry.registerTileEntity(TileEntityAEChisel.class, BlockAEChisel.getRl());
        AEBaseTile.registerTileItem(TileEntityAEChisel.class, new BlockStackSrc(BlockAEChisel.getINSTANCE(), 0, ActivityState.Enabled));
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(BlockAEChisel.getINSTANCE());
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(BlockAEChisel.getITEM_BLOCK());
    }

    @Override
    public @Nullable Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        var t = world.getTileEntity(new BlockPos(x, y, z));
        if (t instanceof TileEntityAEChisel a) {
            return new ContainerAEChisel(player.inventory, a);
        }
        return null;
    }

    @Override
    public @Nullable Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }
}