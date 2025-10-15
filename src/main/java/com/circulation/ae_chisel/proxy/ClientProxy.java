package com.circulation.ae_chisel.proxy;

import com.circulation.ae_chisel.client.GuiAEChisel;
import com.circulation.ae_chisel.common.BlockAEChisel;
import com.circulation.ae_chisel.common.TileEntityAEChisel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        ModelBakery.registerItemVariants(BlockAEChisel.getITEM_BLOCK(), BlockAEChisel.getRl());
        ModelLoader.setCustomModelResourceLocation(BlockAEChisel.getITEM_BLOCK(), 0, new ModelResourceLocation(BlockAEChisel.getRl(), "inventory"));
    }

    @Override
    public @Nullable Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        var t = world.getTileEntity(new BlockPos(x, y, z));
        if (t instanceof TileEntityAEChisel a) {
            return new GuiAEChisel(player.inventory, a);
        }
        return null;
    }
}