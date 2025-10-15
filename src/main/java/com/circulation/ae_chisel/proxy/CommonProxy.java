package com.circulation.ae_chisel.proxy;

import com.circulation.ae_chisel.AppliedChisel;
import com.circulation.ae_chisel.client.ContainerAEChisel;
import com.circulation.ae_chisel.common.BlockAEChisel;
import com.circulation.ae_chisel.common.TileEntityAEChisel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CommonProxy implements IGuiHandler {

    public void preInit() {
        NetworkRegistry.INSTANCE.registerGuiHandler(AppliedChisel.instance, this);
        ForgeRegistries.BLOCKS.register(BlockAEChisel.getInstance());
        var rl = Objects.requireNonNull(BlockAEChisel.getInstance().getRegistryName());
        ForgeRegistries.ITEMS.register(BlockAEChisel.getITEM_BLOCK());
        GameRegistry.registerTileEntity(TileEntityAEChisel.class, rl);
    }

    public void init() {
    }

    public void postInit() {

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