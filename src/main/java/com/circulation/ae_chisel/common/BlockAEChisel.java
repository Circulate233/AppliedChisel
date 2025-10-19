package com.circulation.ae_chisel.common;

import appeng.block.AEBaseTileBlock;
import com.circulation.ae_chisel.AppliedChisel;
import com.circulation.ae_chisel.utils.SyncParallel;
import lombok.Getter;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.circulation.ae_chisel.AppliedChisel.MOD_ID;
import static com.circulation.ae_chisel.AppliedChisel.NET_CHANNEL;

//TODO:错误的破坏粒子效果
public class BlockAEChisel extends AEBaseTileBlock {

    @Getter
    private static final ResourceLocation rl = new ResourceLocation(MOD_ID, "ae_chisel");
    @Getter
    private static final BlockAEChisel INSTANCE = new BlockAEChisel();
    private static final CreativeTabs creativeTab = new CreativeTabs(MOD_ID) {
        @Override
        @NotNull
        public ItemStack createIcon() {
            return new ItemStack(INSTANCE);
        }
    };
    @Getter
    private static final Item ITEM_BLOCK = new ItemBlock(INSTANCE).setTranslationKey(MOD_ID + '.' + "ae_chisel").setCreativeTab(creativeTab).setRegistryName(rl);

    protected BlockAEChisel() {
        super(Material.SPONGE);
        this.setTileEntity(TileEntityAEChisel.class);
        setRegistryName(rl);
        setTranslationKey(MOD_ID + '.' + "ae_chisel");
        setCreativeTab(creativeTab);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.getTileEntity(pos) instanceof TileEntityAEChisel te) {
            if (!super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ)) {
                if (player instanceof EntityPlayerMP p) {
                    if (te.getParallel() > 1) {
                        NET_CHANNEL.sendTo(new SyncParallel(te), p);
                    }
                    p.openGui(AppliedChisel.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
                }
            }
            return true;
        }
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(World w, BlockPos pos, IBlockState state) {
        w.removeTileEntity(pos);
    }
}