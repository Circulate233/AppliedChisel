package com.circulation.ae_chisel.client;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotFake;
import com.circulation.ae_chisel.common.TileEntityAEChisel;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class ContainerAEChisel extends AEBaseContainer {
    public TileEntityAEChisel tile;

    public ContainerAEChisel(InventoryPlayer ip, TileEntityAEChisel myTile) {
        super(ip, myTile, null);
        this.tile = myTile;
        this.addSlotToContainer(new SlotFake(myTile.getInternalInventory(), 0, 80, 37){
            public void putStack(ItemStack is) {
                if (!is.isEmpty()) {
                    is = is.copy();
                    is.setCount(1);
                }

                super.putStack(is);
            }
        });
        this.bindPlayerInventory(ip, 0, 84);
    }

}