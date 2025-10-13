package com.circulation.ae_chisel.client;

import appeng.container.AEBaseContainer;
import appeng.container.slot.AppEngSlot;
import com.circulation.ae_chisel.common.TileEntityAEChisel;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerAEChisel extends AEBaseContainer {
    public TileEntityAEChisel tile;

    public ContainerAEChisel(InventoryPlayer ip, TileEntityAEChisel myTile) {
        super(ip, myTile, null);
        this.tile = myTile;
        this.addSlotToContainer(new AppEngSlot(myTile.getInternalInventory(), 0, 80, 37));
        this.bindPlayerInventory(ip, 0, 84);
    }

}