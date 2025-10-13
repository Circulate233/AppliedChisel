package com.circulation.ae_chisel.common;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.tile.grid.AENetworkInvTile;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import com.circulation.ae_chisel.utils.ChiselPatternDetails;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import team.chisel.api.carving.CarvingUtils;

import java.util.EnumSet;
import java.util.List;

public class TileEntityAEChisel extends AENetworkInvTile implements IInterfaceHost, ITickable {

    protected final DualityInterface duality = new DualityInterface(this.getProxy(), this);
    protected final AppEngInternalInventory inv = new AppEngInternalInventory(this, 1, 1);
    protected final MachineSource source = new MachineSource(this);
    protected final List<ICraftingPatternDetails> patterns = new ObjectArrayList<>();
    protected final ItemList cache = new ItemList();

    public TileEntityAEChisel() {
        this.getProxy().setIdlePowerUsage(100);
    }

    @MENetworkEventSubscribe
    public void stateChange(MENetworkChannelsChanged c) {
        this.duality.notifyNeighbors();
    }

    @MENetworkEventSubscribe
    public void stateChange(MENetworkPowerStatusChange c) {
        this.duality.notifyNeighbors();
    }

    @Override
    @NotNull
    public IItemHandler getInternalInventory() {
        return inv;
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation invOperation, ItemStack removed, ItemStack added) {
        patterns.clear();
        if (!added.isEmpty()) {
            var r = CarvingUtils.getChiselRegistry();
            if (r == null) throw new RuntimeException("Chisel Registry is Null!");
            var input = AEItemStack.fromItemStack(added);
            if (input == null) throw new RuntimeException("added is empty?");
            for (var itemStack : r.getItemsForChiseling(added)) {
                if (!input.equals(itemStack)) {
                    patterns.add(new ChiselPatternDetails(input, itemStack));
                }
            }
        }
        try {
            this.getProxy().getGrid().postEvent(new MENetworkCraftingPatternChange(this, this.getProxy().getNode()));
        } catch (GridAccessException ignored) {

        }
    }

    @Override
    @NotNull
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        NBTTagList list = new NBTTagList();
        for (var stack : cache) {
            NBTTagCompound nbt = new NBTTagCompound();
            stack.writeToNBT(nbt);
            list.appendTag(nbt);
        }
        data.setTag("cacheItems", list);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        var list = data.getTagList("cacheItems", 10);
        for (var nbtBase : list) {
            cache.addStorage(AEItemStack.fromNBT((NBTTagCompound) nbtBase));
        }
        super.readFromNBT(data);
    }

    @Override
    public DualityInterface getInterfaceDuality() {
        return duality;
    }

    @Override
    public EnumSet<EnumFacing> getTargets() {
        return EnumSet.allOf(EnumFacing.class);
    }

    @Override
    public TileEntity getTileEntity() {
        return this;
    }

    @Override
    public int getInstalledUpgrades(Upgrades upgrades) {
        return 0;
    }

    @Override
    public IItemHandler getInventoryByName(String s) {
        return null;
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper providerHelper) {
        for (var pattern : patterns) {
            providerHelper.addCraftingOption(this, pattern);
        }
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting crafting) {
        if (details.getCondensedInputs().length == 0 || details.getCondensedOutputs().length == 0) return false;
        var inputD = details.getCondensedInputs()[0].getDefinition();
        for (var i = 0; i < crafting.getSizeInventory(); i++) {
            var input = crafting.getStackInSlot(i);
            if (input.isEmpty()) continue;
            if (inputD.isItemEqual(input)) {
                var out = details.getCondensedOutputs()[0].copy().setStackSize(input.getCount());
                cache.addStorage(out);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return this.duality.getRequestedJobs();
    }

    @Override
    public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable actionable) {
        return this.duality.injectCraftedItems(link, items, actionable);
    }

    @Override
    public void jobStateChange(ICraftingLink iCraftingLink) {
        this.duality.jobStateChange(iCraftingLink);
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.duality.getConfigManager();
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    @NotNull
    public AECableType getCableConnectionType(@NotNull AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return null;
    }

    @Override
    public void update() {
        if (cache.isEmpty()) return;
        try {
            var grid = this.getProxy().getGrid();
            IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
            IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
            var storage = storageGrid.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
            ItemList o = null;
            for (var stack : cache) {
                if (stack == null || stack.getStackSize() == 0) return;
                var newItem = Platform.poweredInsert(energyGrid, storage, stack, source, Actionable.MODULATE);
                if (newItem != null && newItem.getStackSize() != 0) {
                    if (o == null) o = new ItemList();
                    o.addStorage(newItem);
                }
            }
            cache.resetStatus();
            if (o != null) {
                for (var stack : o) {
                    cache.addStorage(stack);
                }
            }
        } catch (GridAccessException ignored) {

        }
    }
}