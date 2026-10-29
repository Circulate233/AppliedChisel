package com.circulation.ae_chisel.utils;

import ae2.api.crafting.IPatternDetails;
import ae2.api.crafting.PatternDetailsHelper;
import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKey;
import ae2.api.stacks.GenericStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import team.chisel.api.carving.CarvingUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ChiselPatternDetails implements IPatternDetails {

    @NotNull
    private final AEItemKey input;
    @NotNull
    private final AEItemKey output;
    private final int parallel;
    private final AEItemKey definition;
    private final IInput[] inputs;
    private final List<GenericStack> outputs;
    private final int hashcode;

    private ChiselPatternDetails(@NonNull AEItemKey input, @NonNull AEItemKey output, int parallel) {
        this.input = input;
        this.output = output;
        this.parallel = Math.max(1, parallel);
        this.inputs = new IInput[]{new Input(input, this.parallel)};
        this.outputs = List.of(new GenericStack(output, this.parallel));
        this.definition = AEItemKey.of(PatternDetailsHelper.encodeProcessingPattern(
            List.of(new GenericStack(input, this.parallel)), this.outputs));
        this.hashcode = input.hashCode() * 31 * 31 + output.hashCode() * 31 + parallel;
    }

    public static boolean canChisel(ItemStack stack) {
        var input = AEItemKey.of(stack);
        var registry = CarvingUtils.getChiselRegistry();
        if (input == null || registry == null) {
            return false;
        }
        var outputs = registry.getItemsForChiseling(stack);
        if (outputs != null) {
            for (var candidate : outputs) {
                var output = AEItemKey.of(candidate);
                if (output != null && !input.equals(output)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<ChiselPatternDetails> createPatterns(@Nullable AEItemKey input,
                                                            @Nullable Collection<ItemStack> outputs, int parallel) {
        if (input == null || outputs == null || outputs.isEmpty()) {
            return List.of();
        }
        var patterns = new ObjectArrayList<ChiselPatternDetails>(outputs.size());
        var seen = new ObjectOpenHashSet<AEItemKey>();
        seen.add(input);
        for (var stack : outputs) {
            var output = AEItemKey.of(stack);
            if (output != null && seen.add(output)) {
                patterns.add(new ChiselPatternDetails(input, output, parallel));
            }
        }
        return Collections.unmodifiableList(patterns);
    }

    public AEItemKey input() {
        return this.input;
    }

    public AEItemKey output() {
        return this.output;
    }

    public int parallel() {
        return this.parallel;
    }

    @Override
    public AEItemKey getDefinition() {
        return this.definition;
    }

    @Override
    public IInput[] getInputs() {
        return this.inputs.clone();
    }

    @Override
    public List<GenericStack> getOutputs() {
        return this.outputs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiselPatternDetails that)) return false;
        return parallel == that.parallel && input.equals(that.input) && output.equals(that.output);
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    private static class Input implements IInput {

        private final AEItemKey key;
        private final long multiplier;
        private final GenericStack[] inputs;

        private Input(AEItemKey key, long multiplier) {
            this.key = key;
            this.multiplier = multiplier;
            this.inputs = new GenericStack[]{new GenericStack(this.key, 1)};
        }

        @Override
        public GenericStack[] possibleInputs() {
            return inputs;
        }

        @Override
        public long getMultiplier() {
            return multiplier;
        }

        @Override
        public boolean isValid(AEKey input, World world) {
            return this.key.equals(input);
        }

        @Override
        public @Nullable AEKey getRemainingKey(AEKey template) {
            return null;
        }
    }
}
