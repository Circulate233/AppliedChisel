package com.circulation.ae_chisel.common;

import ae2.api.crafting.IPatternDetails;
import ae2.api.implementations.blockentities.PatternContainerGroup;
import ae2.api.inventories.BaseInternalInventory;
import ae2.api.inventories.InternalInventory;
import ae2.api.networking.GridFlags;
import ae2.api.networking.IGrid;
import ae2.api.networking.IGridNode;
import ae2.api.networking.IGridNodeListener;
import ae2.api.networking.crafting.ICraftingProvider;
import ae2.api.networking.ticking.IGridTickable;
import ae2.api.networking.ticking.TickRateModulation;
import ae2.api.networking.ticking.TickingRequest;
import ae2.api.orientation.BlockOrientation;
import ae2.api.stacks.AEItemKey;
import ae2.api.stacks.AEKey2LongMap;
import ae2.api.stacks.GenericStack;
import ae2.api.stacks.KeyCounter;
import ae2.api.storage.StorageHelper;
import ae2.api.upgrades.IUpgradeInventory;
import ae2.api.upgrades.IUpgradeableObject;
import ae2.api.upgrades.UpgradeInventories;
import ae2.core.definitions.AEItems;
import ae2.helpers.patternprovider.PatternContainer;
import ae2.me.helpers.MachineSource;
import ae2.tile.grid.AENetworkedTile;
import ae2.util.inv.AppEngInternalInventory;
import ae2.util.inv.InternalInventoryHost;
import com.circulation.ae_chisel.AppliedChisel;
import com.circulation.ae_chisel.utils.ChiselPatternDetails;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.chisel.api.carving.CarvingUtils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class TileEntityAEChisel extends AENetworkedTile implements InternalInventoryHost, ICraftingProvider, IGridTickable, IUpgradeableObject, PatternContainer {

    public static final int MAX_MARKERS = 4;
    public static final int MAX_UPGRADES = 3;
    private static final Set<EnumFacing> facing = Collections.unmodifiableSet(EnumSet.complementOf(EnumSet.of(EnumFacing.UP)));

    private final AppEngInternalInventory inventory = new AppEngInternalInventory(this, MAX_MARKERS, 1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot < getMarkerSlots() && (stack.isEmpty() || ChiselPatternDetails.canChisel(stack));
        }
    };
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(
            BlockAEChisel.getITEM_BLOCK(), MAX_UPGRADES, this::onUpgradesChanged);
    private final MachineSource source = new MachineSource(this);
    private final AEKey2LongMap cache = new AEKey2LongMap.OpenHashMap();
    private List<ChiselPatternDetails> patterns = List.of();
    private final InternalInventory terminalPatterns = new BaseInternalInventory() {
        @Override
        public int size() {
            return patterns.size();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return patterns.get(slot).getDefinition().toStack();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return false;
        }

        @Override
        public void setItemDirect(int slot, ItemStack stack) {
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    };
    private int parallel = 1;

    public TileEntityAEChisel() {
        this.getMainNode()
                .setIdlePowerUsage(10)
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(ICraftingProvider.class, this)
                .addService(IGridTickable.class, this);
    }

    @Override
    public void onReady() {
        this.rebuildPatterns();
        super.onReady();
        this.wakeForCachedOutputs();
    }

    @Override
    public Set<EnumFacing> getGridConnectableSides(BlockOrientation orientation) {
        return facing;
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State state) {
        if (state != IGridNodeListener.State.GRID_BOOT) {
            ICraftingProvider.requestUpdate(this.getMainNode());
        }
        this.wakeForCachedOutputs();
    }

    @Override
    public ItemStack getItemFromTile() {
        return new ItemStack(BlockAEChisel.getITEM_BLOCK());
    }

    public AppEngInternalInventory getInternalInventory() {
        return this.inventory;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return this.upgrades;
    }

    public int getMarkerSlots() {
        return 1 + Math.min(MAX_UPGRADES, this.upgrades.getInstalledUpgrades(AEItems.PATTERN_EXPANSION_CARD.item()));
    }

    private void onUpgradesChanged() {
        if (!this.isClientSide()) {
            this.rebuildPatterns();
            this.saveChanges();
        }
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inventory) {
        this.saveChanges();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inventory, int slot) {
        if (!this.isClientSide()) {
            this.rebuildPatterns();
        }
    }

    @Override
    public boolean isClientSide() {
        return this.world != null && this.world.isRemote;
    }

    public int getParallel() {
        return this.parallel;
    }

    public void setParallel(int parallel) {
        int value = Math.max(1, parallel);
        if (this.parallel == value) {
            return;
        }
        this.parallel = value;
        if (this.world != null && !this.world.isRemote) {
            this.rebuildPatterns();
            this.saveChanges();
        }
    }

    private void rebuildPatterns() {
        var registry = CarvingUtils.getChiselRegistry();
        var patterns = new ObjectArrayList<ChiselPatternDetails>();
        var inputs = new ObjectOpenHashSet<AEItemKey>();
        for (int slot = 0; slot < this.inventory.size(); slot++) {
            var stack = this.inventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (slot >= this.getMarkerSlots()) {
                this.inventory.setItemDirect(slot, ItemStack.EMPTY);
                continue;
            }
            if (registry != null) {
                var input = AEItemKey.of(stack);
                var options = ChiselPatternDetails.createPatterns(input, registry.getItemsForChiseling(stack), this.parallel);
                if (options.isEmpty()) {
                    this.inventory.setItemDirect(slot, ItemStack.EMPTY);
                } else if (inputs.add(input)) {
                    patterns.addAll(options);
                }
            }
        }
        this.patterns = Collections.unmodifiableList(patterns);
        ICraftingProvider.requestUpdate(this.getMainNode());
    }

    @Override
    public void saveAdditional(NBTTagCompound data) {
        super.saveAdditional(data);
        this.upgrades.writeToNBT(data, "upgrades");
        this.inventory.writeToNBT(data, "inv");
        data.setInteger("parallel", this.parallel);
        var items = new NBTTagList();
        for (var entry : this.cache.object2LongEntrySet()) {
            if (entry.getLongValue() > 0) {
                items.appendTag(GenericStack.writeTag(new GenericStack(entry.getKey(), entry.getLongValue())));
            }
        }
        data.setTag("cacheItems", items);
    }

    @Override
    public void loadTag(NBTTagCompound data) {
        super.loadTag(data);
        this.parallel = Math.max(1, data.getInteger("parallel"));
        this.upgrades.readFromNBT(data, "upgrades");
        this.inventory.clear();
        this.inventory.readFromNBT(data, "inv");
        this.cache.clear();
        var items = data.getTagList("cacheItems", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            var stack = GenericStack.readTag(items.getCompoundTagAt(i));
            if (stack != null && stack.amount() > 0) {
                this.cache.addTo(stack.what(), stack.amount());
            }
        }
        if (this.getMainNode().isReady()) {
            this.rebuildPatterns();
            this.wakeForCachedOutputs();
        }
    }

    @Override
    public List<ChiselPatternDetails> getAvailablePatterns() {
        return this.getMainNode().isOnline() ? this.patterns : List.of();
    }

    @Override
    public @Nullable IGrid getGrid() {
        return this.getMainNode().getGrid();
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        return this.terminalPatterns;
    }

    @Override
    public boolean containsPattern(AEItemKey definition) {
        for (var pattern : this.patterns) {
            if (pattern.getDefinition().equals(definition)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        return new PatternContainerGroup(AEItemKey.of(this.getItemFromTile()),
                this.hasCustomName() ? new TextComponentString(this.getCustomName())
                        : new TextComponentTranslation("tile.ae_chisel.ae_chisel.name"), List.of());
    }

    @Override
    public void openTerminalPatternContainerGui(EntityPlayer player) {
        if (this.world != null && !this.world.isRemote) {
            player.openGui(AppliedChisel.instance, 0, this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
        }
    }

    @Override
    public int getPatternPriority() {
        return 1;
    }

    @Override
    public boolean pushPattern(IPatternDetails details, KeyCounter[] inputHolder, int multiplier) {
        if (!this.getMainNode().isActive() || multiplier <= 0 || inputHolder.length != 1
                || inputHolder[0] == null) {
            return false;
        }
        var pattern = this.findPattern(details);
        if (pattern == null) {
            return false;
        }
        long amount = (long) pattern.parallel() * multiplier;
        long cached = this.cache.getLong(pattern.output());
        if (amount > Long.MAX_VALUE - cached || inputHolder[0].get(pattern.input()) != amount) {
            return false;
        }
        for (var input : inputHolder[0]) {
            if (input.getLongValue() != 0 && !input.getKey().equals(pattern.input())) {
                return false;
            }
        }
        this.cache.addTo(pattern.output(), amount);
        inputHolder[0].clear();
        this.saveChanges();
        this.wakeForCachedOutputs();
        return true;
    }

    @Override
    public boolean canMergePatternPush(IPatternDetails details) {
        return this.findPattern(details) != null;
    }

    @Override
    public int getMaxPatternPushMultiplier(IPatternDetails details, int maxMultiplier) {
        if (!this.getMainNode().isActive() || maxMultiplier <= 0) {
            return 0;
        }
        var pattern = this.findPattern(details);
        if (pattern == null) {
            return 0;
        }
        long capacity = (Long.MAX_VALUE - this.cache.getLong(pattern.output())) / pattern.parallel();
        return (int) Math.min(maxMultiplier, capacity);
    }

    private ChiselPatternDetails findPattern(IPatternDetails details) {
        var inputs = details.getInputs();
        var outputs = details.getOutputs();
        if (inputs.length != 1 || outputs.size() != 1) {
            return null;
        }
        var variants = inputs[0].possibleInputs();
        var output = outputs.getFirst();
        long multiplier = inputs[0].getMultiplier();
        if (variants.length != 1 || variants[0].amount() <= 0 || multiplier <= 0
                || variants[0].amount() > Long.MAX_VALUE / multiplier
                || variants[0].amount() * multiplier != output.amount()) {
            return null;
        }
        for (var pattern : this.patterns) {
            if (pattern.input().equals(variants[0].what()) && pattern.output().equals(output.what())
                    && pattern.parallel() == output.amount()) {
                return pattern;
            }
        }
        return null;
    }

    @Override
    public boolean isBusy() {
        return !this.getMainNode().isActive();
    }

    @Override
    public @NotNull TickingRequest getTickingRequest(@NotNull IGridNode node) {
        return new TickingRequest(1, 20, this.cache.isEmpty());
    }

    @Override
    public @NotNull TickRateModulation tickingRequest(@NotNull IGridNode node, int ticksSinceLastCall) {
        if (this.cache.isEmpty() || !node.isActive()) {
            return TickRateModulation.SLEEP;
        }
        var grid = node.grid();
        var energy = grid.getEnergyService();
        var storage = grid.getStorageService().getInventory();
        boolean changed = false;
        var iterator = this.cache.object2LongEntrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            long amount = entry.getLongValue();
            if (amount > 0) {
                long inserted = StorageHelper.poweredInsert(energy, storage, entry.getKey(), amount, this.source);
                if (inserted > 0) {
                    if (inserted == amount) {
                        iterator.remove();
                    } else {
                        entry.setValue(amount - inserted);
                    }
                    changed = true;
                }
            }
        }
        if (changed) {
            this.saveChanges();
        }
        return this.cache.isEmpty() ? TickRateModulation.SLEEP
                : changed ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }

    private void wakeForCachedOutputs() {
        if (!this.cache.isEmpty() && this.getMainNode().isActive()) {
            this.getMainNode().ifPresent((grid, node) -> grid.getTickManager().alertDevice(node));
        }
    }

    @Override
    public void addAdditionalDrops(List<ItemStack> drops) {
        super.addAdditionalDrops(drops);
        for (var upgrade : this.upgrades) {
            if (!upgrade.isEmpty()) {
                drops.add(upgrade.copy());
            }
        }
        for (var entry : this.cache.object2LongEntrySet()) {
            entry.getKey().addDrops(entry.getLongValue(), drops, this.world, this.pos);
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.inventory.clear();
        this.upgrades.clear();
        this.cache.clear();
    }
}
