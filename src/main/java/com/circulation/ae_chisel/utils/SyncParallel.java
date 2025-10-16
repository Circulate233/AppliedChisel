package com.circulation.ae_chisel.utils;

import com.circulation.ae_chisel.common.TileEntityAEChisel;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SyncParallel implements IMessage, IMessageHandler<SyncParallel,IMessage> {

    private BlockPos pos;
    private int parallel;

    public SyncParallel(){

    }

    public SyncParallel(TileEntityAEChisel te){
        pos = te.getPos();
        parallel = te.parallel;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        parallel = buf.readInt();
        pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(parallel);
        buf.writeLong(pos.toLong());
    }

    @Override
    public IMessage onMessage(SyncParallel message, MessageContext ctx) {
        switch (ctx.side){
            case SERVER -> {
                var world = ctx.getServerHandler().player.world;
                if (world.getTileEntity(message.pos) instanceof TileEntityAEChisel te){
                    if (te.parallel != message.parallel)
                        te.setParallel(message.parallel);
                }
            }
            case CLIENT -> onClient(message,ctx);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public void onClient(SyncParallel message, MessageContext ctx){
        var world = Minecraft.getMinecraft().player.world;
        if (world.getTileEntity(message.pos) instanceof TileEntityAEChisel te){
            te.parallel = message.parallel;
        }
    }
}
