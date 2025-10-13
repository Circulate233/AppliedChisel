package com.circulation.ae_chisel.client;

import appeng.client.gui.AEBaseGui;
import appeng.core.localization.GuiText;
import com.circulation.ae_chisel.common.TileEntityAEChisel;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAEChisel extends AEBaseGui {

    public GuiAEChisel(InventoryPlayer inventoryPlayer, TileEntityAEChisel te) {
        super(new ContainerAEChisel(inventoryPlayer,te));
        this.ySize = 166;
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(GuiText.Chest.getLocal()), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture("guis/chest.png");
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
    }
}
