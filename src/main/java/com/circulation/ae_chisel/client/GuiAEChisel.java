package com.circulation.ae_chisel.client;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.MEGuiTooltipTextField;
import appeng.container.interfaces.IJEIGhostIngredients;
import appeng.container.slot.SlotFake;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import com.circulation.ae_chisel.AppliedChisel;
import com.circulation.ae_chisel.common.TileEntityAEChisel;
import com.circulation.ae_chisel.utils.SyncParallel;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
@Optional.Interface(
        iface = "appeng.container.interfaces.IJEIGhostIngredients",
        modid = "jei"
)
public class GuiAEChisel extends AEBaseGui implements IJEIGhostIngredients {

    private MEGuiTooltipTextField parallel;
    private final TileEntityAEChisel te;
    private int oldParallel;

    public GuiAEChisel(InventoryPlayer inventoryPlayer, TileEntityAEChisel te) {
        super(new ContainerAEChisel(inventoryPlayer, te));
        this.ySize = 166;
        this.te = te;
        this.oldParallel = te.getParallel();
    }

    @Override
    public void initGui() {
        super.initGui();
        this.parallel = new MEGuiTooltipTextField(64, 12) {
            @Override
            public boolean textboxKeyTyped(char keyChar, int keyID) {
                if (!this.isFocused()) {
                    return false;
                } else {
                    String oldText = this.getText();
                    boolean handled = this.field.textboxKeyTyped(keyChar, keyID);
                    if (!handled && (keyID == 28 || keyID == 156 || keyID == 1)) {
                        this.setFocused(false);
                    }

                    if (handled) {
                        this.onTextChange(oldText);
                    }

                    if (!StringUtils.isNumeric(this.getText())){
                        if (this.getText().isEmpty()) {
                            this.setText("1");
                            return true;
                        } else {
                            this.setText(oldText);
                            return false;
                        }
                    }

                    return handled;
                }
            }

            @Override
            public void setFocused(boolean focus) {
                final boolean i = this.field.isFocused() && !focus;
                super.setFocused(focus);
                if (i) syncParallel();
            }
        };
        this.parallel.setEnableBackgroundDrawing(false);
        this.parallel.setMaxStringLength(10);
        this.parallel.setTextColor(16777215);
        this.parallel.setText(te.getParallel() > 0 ? Integer.toString(te.getParallel()) : "1");
        this.parallel.x = this.guiLeft + 55;
        this.parallel.y = this.guiTop + 62;
    }

    private void syncParallel(){
        if (te.getParallel() != oldParallel) {
            AppliedChisel.NET_CHANNEL.sendToServer(new SyncParallel(te));
            oldParallel = te.getParallel();
        }
    }

    @Override
    public void onGuiClosed(){
        syncParallel();
        super.onGuiClosed();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawTooltip(this.parallel, mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int xPos, int yPos, int button) throws IOException {
        parallel.mouseClicked(xPos, yPos, button);
        super.mouseClicked(xPos, yPos, button);
    }

    @Override
    protected void keyTyped(char character, int key) throws IOException {
        if (!this.checkHotbarKeys(key)) {
            if (character == ' ') {
                if (this.parallel.getText().isEmpty() && this.parallel.isFocused()) {
                    return;
                }
            }

            if (!this.parallel.textboxKeyTyped(character, key)) {
                super.keyTyped(character, key);
                return;
            }
            var text = this.parallel.getText();
            long l;
            try {
                l = Long.parseLong(text);
            } catch (NumberFormatException e) {
                l = 1;
                this.parallel.setText("1");
            }
            if (l < 1 || l > Integer.MAX_VALUE) {
                l = 2147483647;
                this.parallel.setText("2147483647");
            }
            te.setParallel((int) l);
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(I18n.format("tile.ae_chisel.ae_chisel.name")), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture("guis/chest.png");
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);
        this.parallel.drawTextBox();
    }

    private Map<?, ?> slotMap = Object2ObjectMaps.emptyMap();

    @Override
    @Optional.Method(modid = "jei")
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        if (ingredient instanceof ItemStack itemStack) {
            for (final Slot slot : this.inventorySlots.inventorySlots) {
                if (slot instanceof SlotFake) {
                    IGhostIngredientHandler.Target<Object> target = new IGhostIngredientHandler.Target<>() {
                        public @NotNull Rectangle getArea() {
                            return new Rectangle(GuiAEChisel.this.getGuiLeft() + slot.xPos, GuiAEChisel.this.getGuiTop() + slot.yPos, 16, 16);
                        }

                        public void accept(@NotNull Object ingredient) {
                            try {
                                PacketInventoryAction p = new PacketInventoryAction(InventoryAction.PLACE_JEI_GHOST_ITEM, (SlotFake) slot, AEItemStack.fromItemStack(itemStack));
                                NetworkHandler.instance().sendToServer(p);
                            } catch (IOException ignored) {

                            }

                        }
                    };
                    if (slotMap.isEmpty()) slotMap = Object2ObjectMaps.singleton(target, slot);
                    return ObjectLists.singleton(target);
                }
            }
        }
        return ObjectLists.emptyList();
    }

    @SuppressWarnings("unchecked")
    @Override
    @Optional.Method(modid = "jei")
    public Map<IGhostIngredientHandler.Target<?>, Object> getFakeSlotTargetMap() {
        return (Map<IGhostIngredientHandler.Target<?>, Object>) slotMap;
    }
}