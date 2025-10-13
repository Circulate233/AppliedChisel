package com.circulation.ae_chisel.common;

import appeng.block.AEBaseTileBlock;
import com.circulation.ae_chisel.AppliedChisel;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static com.circulation.ae_chisel.AppliedChisel.MOD_ID;

public class BlockAEChisel extends AEBaseTileBlock {

    private static final CreativeTabs creativeTab = new CreativeTabs(MOD_ID) {
        @Override
        @NotNull
        public ItemStack createIcon() {
            return new ItemStack(INSTANCE);
        }
    };

    private static final BlockAEChisel INSTANCE = new BlockAEChisel();

    @NotNull
    public static BlockAEChisel getInstance(){
        return INSTANCE;
    }

    protected BlockAEChisel() {
        super(Material.SPONGE);
        this.setTileEntity(TileEntityAEChisel.class);
        setRegistryName(new ResourceLocation(MOD_ID, "ae_chisel"));
        setTranslationKey(MOD_ID + '.' + "ae_chisel");
        setCreativeTab(creativeTab);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.getTileEntity(pos) instanceof TileEntityAEChisel){
            player.openGui(AppliedChisel.instance,0,world,pos.getX(),pos.getY(),pos.getZ());
            return true;
        }
        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

}
