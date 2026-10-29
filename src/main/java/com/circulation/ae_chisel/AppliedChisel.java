package com.circulation.ae_chisel;

import ae2.core.definitions.AEBlocks;
import ae2.core.definitions.AEItems;
import ae2.api.upgrades.Upgrades;
import com.circulation.ae_chisel.common.BlockAEChisel;
import com.circulation.ae_chisel.proxy.CommonProxy;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.chisel.common.init.ChiselBlocks;
import team.chisel.common.init.ChiselItems;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "required-after:chisel;" +
                "required-after:ae2@[1.1.0,);"
)
public class AppliedChisel {
    public static final String CLIENT_PROXY = "com.circulation.ae_chisel.proxy.ClientProxy";
    public static final String COMMON_PROXY = "com.circulation.ae_chisel.proxy.CommonProxy";

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy = null;

    @Mod.Instance(Tags.MOD_ID)
    public static AppliedChisel instance = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Upgrades.add(AEItems.PATTERN_EXPANSION_CARD.item(), BlockAEChisel.getITEM_BLOCK(), 3);
        proxy.preInit();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        GameRegistry.addShapedRecipe(
                new ResourceLocation(Tags.MOD_ID, Tags.MOD_ID),
                null,
                new ItemStack(BlockAEChisel.getITEM_BLOCK()),
                "ABA",
                "CDC",
                "EFE",
                'A', AEItems.SPATIAL_128_CELL_COMPONENT.stack(),
                'B', new ItemStack(ChiselItems.chisel_hitech),
                'C', AEBlocks.INTERFACE.stack(),
                'D', AEBlocks.MOLECULAR_ASSEMBLER.stack(),
                'E', AEItems.PATTERN_EXPANSION_CARD.stack(),
                'F', new ItemStack(ChiselBlocks.auto_chisel)
        );
    }

}
