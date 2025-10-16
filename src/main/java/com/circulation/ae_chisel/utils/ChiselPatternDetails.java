package com.circulation.ae_chisel.utils;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.github.bsideup.jabel.Desugar;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Desugar
public record ChiselPatternDetails(IAEItemStack[] inputs, IAEItemStack[] outputs) implements ICraftingPatternDetails {

    public static boolean addChiselPatterns(@Nullable IAEItemStack input, @Nullable Collection<ItemStack> outputs, @NotNull Collection<ChiselPatternDetails> patterns,int parallel) {
        if (input == null) return false;
        if (outputs == null || outputs.isEmpty()) return false;
        for (var itemStack : outputs) {
            var out = AEItemStack.fromItemStack(itemStack);
            if (out != null && !input.equals(itemStack)) {
                patterns.add(new ChiselPatternDetails(input.setStackSize(parallel), out.setStackSize(parallel)));
            }
        }
        return true;
    }

    public void setParallel(int parallel){
        inputs[0].setStackSize(parallel);
        outputs[0].setStackSize(parallel);
    }

    private ChiselPatternDetails(@NotNull IAEItemStack input, @NotNull IAEItemStack output) {
        this(new IAEItemStack[]{input}, new IAEItemStack[]{output});
    }

    @Override
    public ItemStack getPattern() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isValidItemForSlot(int i, ItemStack itemStack, World world) {
        throw new IllegalStateException("Only crafting recipes supported.");
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public IAEItemStack[] getInputs() {
        return inputs;
    }

    @Override
    public IAEItemStack[] getCondensedInputs() {
        return inputs;
    }

    @Override
    public IAEItemStack[] getCondensedOutputs() {
        return outputs;
    }

    @Override
    public IAEItemStack[] getOutputs() {
        return outputs;
    }

    @Override
    public boolean canSubstitute() {
        return false;
    }

    @Override
    public ItemStack getOutput(InventoryCrafting inventoryCrafting, World world) {
        throw new IllegalStateException("Only crafting recipes supported.");
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void setPriority(int i) {

    }
}