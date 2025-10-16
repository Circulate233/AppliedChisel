package com.circulation.ae_chisel;

import appeng.api.AEApi;
import com.circulation.ae_chisel.common.BlockAEChisel;
import com.circulation.ae_chisel.proxy.CommonProxy;
import com.circulation.ae_chisel.utils.SyncParallel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.chisel.common.init.ChiselBlocks;
import team.chisel.common.init.ChiselItems;

@Mod(modid = AppliedChisel.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "required-after:chisel;" +
                "required-after:appliedenergistics2@[v0.56.7,);"
)
public class AppliedChisel {
    public static final String MOD_ID = Tags.MOD_ID;
    public static final String CLIENT_PROXY = "com.circulation.ae_chisel.proxy.ClientProxy";
    public static final String COMMON_PROXY = "com.circulation.ae_chisel.proxy.CommonProxy";

    public static final SimpleNetworkWrapper NET_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(MOD_ID);

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);
    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY)
    public static CommonProxy proxy = null;

    @Mod.Instance(MOD_ID)
    public static AppliedChisel instance = null;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        NET_CHANNEL.registerMessage(SyncParallel.class, SyncParallel.class, 0, Side.SERVER);
        NET_CHANNEL.registerMessage(SyncParallel.class, SyncParallel.class, 1, Side.CLIENT);
        proxy.preInit();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        var materials = AEApi.instance().definitions().materials();
        var blocks = AEApi.instance().definitions().blocks();
        GameRegistry.addShapedRecipe(
                new ResourceLocation(MOD_ID, MOD_ID),
                null,
                new ItemStack(BlockAEChisel.getITEM_BLOCK()),
                "ABA",
                "CDC",
                "EFE",
                'A', materials.cell128SpatialPart().maybeStack(1).orElse(ItemStack.EMPTY),
                'B', new ItemStack(ChiselItems.chisel_hitech),
                'C', GameRegistry.makeItemStack("appliedenergistics2:interface", 0, 1, ""),
                'D', blocks.molecularAssembler().maybeStack(1).orElse(ItemStack.EMPTY),
                'E', materials.cardPatternExpansion().maybeStack(1).orElse(ItemStack.EMPTY),
                'F', new ItemStack(ChiselBlocks.auto_chisel)
        );
    }

}