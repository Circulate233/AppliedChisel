package com.circulation.ae_chisel.client;

import appeng.client.gui.AEBaseGui;
import appeng.container.interfaces.IJEIGhostIngredients;
import appeng.container.slot.SlotFake;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import appeng.util.item.AEItemStack;
import com.circulation.ae_chisel.common.TileEntityAEChisel;
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

    public GuiAEChisel(InventoryPlayer inventoryPlayer, TileEntityAEChisel te) {
        super(new ContainerAEChisel(inventoryPlayer,te));
        this.ySize = 166;
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
    }

    private Map<?,?> slotMap = Object2ObjectMaps.emptyMap();

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
                    if (slotMap.isEmpty())slotMap = Object2ObjectMaps.singleton(target, slot);
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
