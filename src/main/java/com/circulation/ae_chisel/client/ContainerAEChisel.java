package com.circulation.ae_chisel.client;

import ae2.container.AEBaseContainer;
import ae2.container.SlotSemantics;
import ae2.container.guisync.GuiSync;
import ae2.container.slot.FakeSlot;
import com.circulation.ae_chisel.common.TileEntityAEChisel;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class ContainerAEChisel extends AEBaseContainer {

    private static final String SET_PARALLEL = "setParallel";
    private final TileEntityAEChisel tile;
    @GuiSync(0)
    public int parallel;
    @GuiSync(1)
    public int markerSlots;

    public ContainerAEChisel(InventoryPlayer inventory, TileEntityAEChisel tile) {
        super(inventory, tile);
        this.tile = tile;
        this.parallel = tile.getParallel();
        this.markerSlots = tile.getMarkerSlots();
        for (int index = 0; index < TileEntityAEChisel.MAX_MARKERS; index++) {
            final int marker = index;
            this.addSlot(new FakeSlot(tile.getInternalInventory(), marker, 53 + 18 * marker, 33) {
                @Override
                public boolean isSlotEnabled() {
                    return super.isSlotEnabled() && marker < ContainerAEChisel.this.getMarkerSlots();
                }

                @Override
                public void putStack(ItemStack stack) {
                    if (!stack.isEmpty()) {
                        stack = stack.copy();
                        stack.setCount(1);
                    }
                    super.putStack(stack);
                }
            }, SlotSemantics.CONFIG);
        }
        this.setupUpgrades(tile.getUpgrades());
        this.addPlayerInventorySlots(8, 84);
        this.registerClientAction(SET_PARALLEL, Integer.class, value -> {
            if (value != null && this.canInteractWith(this.getPlayer())) {
                this.tile.setParallel(value);
            }
        });
    }

    public TileEntityAEChisel getTile() {
        return this.tile;
    }

    public int getMarkerSlots() {
        return this.isClientSide() ? this.markerSlots : this.tile.getMarkerSlots();
    }

    public void setParallel(int value) {
        value = Math.max(1, value);
        if (this.parallel != value) {
            this.parallel = value;
            this.sendClientAction(SET_PARALLEL, value);
        }
    }

    @Override
    public void broadcastChanges() {
        if (this.isServerSide()) {
            this.parallel = this.tile.getParallel();
            this.markerSlots = this.tile.getMarkerSlots();
        }
        super.broadcastChanges();
    }
}
