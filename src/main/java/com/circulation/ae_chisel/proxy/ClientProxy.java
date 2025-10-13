package com.circulation.ae_chisel.proxy;

import com.circulation.ae_chisel.client.GuiAEChisel;
import com.circulation.ae_chisel.common.TileEntityAEChisel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit() {
        super.preInit();
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void postInit() {
        super.postInit();
    }

    @Override
    public @Nullable Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        var t = world.getTileEntity(new BlockPos(x,y,z));
        if (t instanceof TileEntityAEChisel a) {
            return new GuiAEChisel(player.inventory,a);
        }
        return null;
    }
}