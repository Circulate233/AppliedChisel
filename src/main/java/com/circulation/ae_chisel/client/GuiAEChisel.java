package com.circulation.ae_chisel.client;

import ae2.client.gui.AEBaseGui;
import ae2.client.gui.style.GuiStyleManager;
import ae2.client.gui.widgets.AETextField;
import ae2.client.gui.widgets.UpgradesPanel;
import ae2.container.SlotSemantics;
import ae2.container.slot.AppEngSlot;
import com.circulation.ae_chisel.common.TileEntityAEChisel;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiAEChisel extends AEBaseGui<ContainerAEChisel> {

    private final AETextField parallel;
    private int displayedParallel;

    public GuiAEChisel(InventoryPlayer inventory, TileEntityAEChisel tile) {
        super(new ContainerAEChisel(inventory, tile), inventory,
                GuiStyleManager.loadStyleDoc("/screens/ae_chisel.json"));
        this.parallel = new AETextField(this.style, Minecraft.getMinecraft().fontRenderer, 0, 0, 80, 16) {
            @Override
            public boolean textboxKeyTyped(char character, int key) {
                if (!this.isFocused()) {
                    return false;
                }
                boolean handled = super.textboxKeyTyped(character, key);
                if (handled) {
                    normalizeParallel();
                }
                return handled;
            }
        };
        this.parallel.setMaxStringLength(10);
        this.parallel.setValidator(text -> text == null || text.isEmpty() || text.chars().allMatch(c -> c >= '0' && c <= '9'));
        this.parallel.setTooltipMessage(List.of(new TextComponentTranslation("text.ae_chisel.gui.parallel")));
        this.displayedParallel = this.container.parallel;
        this.parallel.setText(Integer.toString(this.displayedParallel));
        this.widgets.add("parallel", this.parallel);
        this.widgets.add("upgrades", UpgradesPanel.create(this.widgets,
                this.container.getSlots(SlotSemantics.UPGRADE), tile));
        for (var slot : this.container.getSlots(SlotSemantics.CONFIG)) {
            if (slot instanceof AppEngSlot appEngSlot) {
                appEngSlot.setEmptyTooltip(() -> List.of(new TextComponentTranslation("text.ae_chisel.gui.markers")));
            }
        }
    }

    private int normalizeParallel() {
        long value;
        try {
            value = Long.parseLong(this.parallel.getText());
        } catch (NumberFormatException ignored) {
            value = 1;
        }
        int clamped = value < 1 || value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
        String normalized = Integer.toString(clamped);
        if (!normalized.equals(this.parallel.getText())) {
            this.parallel.setText(normalized);
        }
        return clamped;
    }

    private void syncParallel() {
        int clamped = this.normalizeParallel();
        this.container.setParallel(clamped);
        this.displayedParallel = clamped;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!this.parallel.isFocused() && this.displayedParallel != this.container.parallel) {
            this.displayedParallel = this.container.parallel;
            this.parallel.setText(Integer.toString(this.displayedParallel));
        }
    }

    @Override
    public void onGuiClosed() {
        this.syncParallel();
        super.onGuiClosed();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        boolean wasFocused = this.parallel.isFocused();
        super.mouseClicked(mouseX, mouseY, button);
        if (wasFocused && !this.parallel.isFocused()) {
            this.syncParallel();
        }
    }

    @Override
    protected void keyTyped(char character, int key) throws IOException {
        if (this.checkHotbarKeys(key)) {
            return;
        }
        if (this.parallel.isFocused() && (key == 28 || key == 156 || key == 1)) {
            this.syncParallel();
            this.parallel.setFocused(false);
        }
        super.keyTyped(character, key);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY, partialTicks);
        var background = this.style.getImage("slot");
        for (var slot : this.container.inventorySlots) {
            if (slot.isEnabled() && this.container.getSlotSemantic(slot) != SlotSemantics.UPGRADE) {
                background.copy().dest(offsetX + slot.xPos - 1, offsetY + slot.yPos - 1).blit();
            }
        }
    }
}
