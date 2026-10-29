package com.circulation.ae_chisel.common;

import ae2.block.AEBaseTileBlock;
import com.circulation.ae_chisel.AppliedChisel;
import com.circulation.ae_chisel.Tags;
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

public class BlockAEChisel extends AEBaseTileBlock<TileEntityAEChisel> {

    public static final ResourceLocation rl = new ResourceLocation(Tags.MOD_ID, "ae_chisel");
    private static final CreativeTabs creativeTab = new CreativeTabs(Tags.MOD_ID) {
        @Override
        public @NotNull ItemStack createIcon() {
            return new ItemStack(INSTANCE);
        }
    };
    private static final BlockAEChisel INSTANCE = new BlockAEChisel();
    private static final Item ITEM_BLOCK = new ItemBlock(INSTANCE)
        .setTranslationKey(Tags.MOD_ID + ".ae_chisel").setCreativeTab(creativeTab).setRegistryName(rl);

    protected BlockAEChisel() {
        super(Material.IRON);
        this.setTileEntity(TileEntityAEChisel.class);
        this.setRegistryName(rl);
        this.setTranslationKey(Tags.MOD_ID + ".ae_chisel");
        this.setCreativeTab(creativeTab);
    }

    public static ResourceLocation getRl() {
        return rl;
    }

    public static BlockAEChisel getINSTANCE() {
        return INSTANCE;
    }

    public static Item getITEM_BLOCK() {
        return ITEM_BLOCK;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.getTileEntity(pos) instanceof TileEntityAEChisel) {
            if (!super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ)
                    && player instanceof EntityPlayerMP serverPlayer) {
                serverPlayer.openGui(AppliedChisel.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }
}
