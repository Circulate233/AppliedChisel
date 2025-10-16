package com.circulation.ae_chisel;

import com.circulation.ae_chisel.proxy.CommonProxy;
import com.circulation.ae_chisel.utils.SyncParallel;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

}