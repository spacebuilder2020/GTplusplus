package gtPlusPlus.xmod.gregtech.common.tileentities.machines.multi.processing.advanced;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.GT_HatchElement.*;
import static gregtech.api.util.GT_StructureUtility.buildHatchAdder;
import static gregtech.api.util.GT_StructureUtility.ofCoil;
import static gtPlusPlus.core.util.data.ArrayUtils.removeNulls;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.enums.TAE;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import gtPlusPlus.api.objects.Logger;
import gtPlusPlus.core.block.ModBlocks;
import gtPlusPlus.core.lib.CORE;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.GT_MetaTileEntity_Hatch_CustomFluidBase;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.GregtechMeta_MultiBlockBase;
import gtPlusPlus.xmod.gregtech.common.blocks.textures.TexturesGtBlock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.ArrayUtils;

public class GregtechMetaTileEntity_Adv_EBF extends GregtechMeta_MultiBlockBase<GregtechMetaTileEntity_Adv_EBF>
        implements ISurvivalConstructable {

    public static int CASING_TEXTURE_ID;
    public static String mHotFuelName = "Blazing Pyrotheum";
    public static String mCasingName = "Volcanus Casing";
    public static String mHatchName = "Pyrotheum Hatch";
    private static IStructureDefinition<GregtechMetaTileEntity_Adv_EBF> STRUCTURE_DEFINITION = null;
    private int mCasing;
    private final ArrayList<GT_MetaTileEntity_Hatch_CustomFluidBase> mPyrotheumHatches = new ArrayList<>();

    private HeatingCoilLevel mHeatingCapacity;
    private boolean isBussesSeparate = false;

    public GregtechMetaTileEntity_Adv_EBF(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        CASING_TEXTURE_ID = TAE.getIndexFromPage(2, 11);
    }

    public GregtechMetaTileEntity_Adv_EBF(String aName) {
        super(aName);
        CASING_TEXTURE_ID = TAE.getIndexFromPage(2, 11);
    }

    @Override
    public String getMachineType() {
        return "Blast Furnace";
    }

    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GregtechMetaTileEntity_Adv_EBF(this.mName);
    }

    @Override
    protected GT_Multiblock_Tooltip_Builder createTooltip() {
        GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.addMachineType(getMachineType())
                .addInfo("Factory Grade Advanced Blast Furnace")
                .addInfo("Speed: +120% | EU Usage: 90% | Parallel: 8")
                .addInfo("Consumes 10L of " + mHotFuelName + " per second during operation")
                .addInfo("Constructed exactly the same as a normal EBF")
                .addPollutionAmount(getPollutionPerSecond(null))
                .addSeparator()
                .addController("Bottom center")
                .addCasingInfo(mCasingName, 9)
                .addInputHatch("Any Casing", 1)
                .addInputBus("Any Casing", 1)
                .addOutputBus("Any Casing", 1)
                .addOutputHatch("Any Casing", 1)
                .addStructureHint(mHatchName, 1)
                .addEnergyHatch("Any Casing", 1)
                .addMufflerHatch("Any Casing", 1)
                .addMaintenanceHatch("Any Casing", 1)
                .toolTipFinisher(CORE.GT_Tooltip_Builder);
        return tt;
    }

    @Override
    public IStructureDefinition<GregtechMetaTileEntity_Adv_EBF> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<GregtechMetaTileEntity_Adv_EBF>builder()
                    .addShape(mName, transpose(new String[][] {
                        {"CCC", "CCC", "CCC"},
                        {"HHH", "H-H", "HHH"},
                        {"HHH", "H-H", "HHH"},
                        {"C~C", "CCC", "CCC"},
                    }))
                    .addElement(
                            'C',
                            ofChain(
                                    buildHatchAdder(GregtechMetaTileEntity_Adv_EBF.class)
                                            .adder(GregtechMetaTileEntity_Adv_EBF::addPyrotheumHatch)
                                            .hatchId(968)
                                            .casingIndex(CASING_TEXTURE_ID)
                                            .dot(1)
                                            .build(),
                                    buildHatchAdder(GregtechMetaTileEntity_Adv_EBF.class)
                                            .atLeast(
                                                    InputBus,
                                                    OutputBus,
                                                    Maintenance,
                                                    Energy,
                                                    Muffler,
                                                    InputHatch,
                                                    OutputHatch)
                                            .casingIndex(CASING_TEXTURE_ID)
                                            .dot(1)
                                            .build(),
                                    onElementPass(x -> ++x.mCasing, ofBlock(ModBlocks.blockCasings3Misc, 11))))
                    .addElement(
                            'H',
                            ofCoil(
                                    GregtechMetaTileEntity_Adv_EBF::setCoilLevel,
                                    GregtechMetaTileEntity_Adv_EBF::getCoilLevel))
                    .build();
        }
        return STRUCTURE_DEFINITION;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(mName, stackSize, hintsOnly, 1, 3, 0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivialBuildPiece(mName, stackSize, 1, 3, 0, elementBudget, env, false, true);
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        mCasing = 0;
        mPyrotheumHatches.clear();
        setCoilLevel(HeatingCoilLevel.None);
        return checkPiece(mName, 1, 3, 0) && mCasing >= 9 && getCoilLevel() != HeatingCoilLevel.None && checkHatch();
    }

    @Override
    public boolean checkHatch() {
        return super.checkHatch() || !mPyrotheumHatches.isEmpty();
    }

    private boolean addPyrotheumHatch(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (aTileEntity == null) {
            return false;
        } else {
            IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
            if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_CustomFluidBase
                    && aMetaTileEntity.getBaseMetaTileEntity().getMetaTileID() == 968) {
                return addToMachineListInternal(mPyrotheumHatches, aTileEntity, aBaseCasingIndex);
            }
        }
        return false;
    }

    @Override
    public void updateSlots() {
        for (GT_MetaTileEntity_Hatch_CustomFluidBase tHatch : mPyrotheumHatches)
            if (isValidMetaTileEntity(tHatch)) tHatch.updateSlots();
        super.updateSlots();
    }

    private boolean depleteFuel(int aAmount) {
        for (final GT_MetaTileEntity_Hatch_CustomFluidBase tHatch : this.mPyrotheumHatches) {
            if (isValidMetaTileEntity(tHatch)) {
                FluidStack tLiquid = tHatch.getFluid();
                if (tLiquid == null || tLiquid.amount < aAmount) {
                    continue;
                }
                tLiquid = tHatch.drain(aAmount, false);
                if (tLiquid != null && tLiquid.amount >= aAmount) {
                    tLiquid = tHatch.drain(aAmount, true);
                    return tLiquid != null && tLiquid.amount >= aAmount;
                }
            }
        }
        return false;
    }

    @Override
    protected IIconContainer getActiveOverlay() {
        return TexturesGtBlock.Overlay_Machine_Controller_Advanced_Active;
    }

    @Override
    protected IIconContainer getInactiveOverlay() {
        return TexturesGtBlock.Overlay_Machine_Controller_Advanced;
    }

    @Override
    protected int getCasingTextureId() {
        return CASING_TEXTURE_ID;
    }

    public GT_Recipe.GT_Recipe_Map getRecipeMap() {
        return GT_Recipe.GT_Recipe_Map.sBlastRecipes;
    }

    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public boolean checkRecipe(ItemStack aStack) {
        if (isBussesSeparate) {
            FluidStack[] tFluids = getStoredFluids().toArray(new FluidStack[0]);
            for (GT_MetaTileEntity_Hatch_InputBus tBus : mInputBusses) {
                ArrayList<ItemStack> tInputs = new ArrayList<>();
                if (isValidMetaTileEntity(tBus)) {
                    for (int i = tBus.getBaseMetaTileEntity().getSizeInventory() - 1; i >= 0; i--) {
                        if (tBus.getBaseMetaTileEntity().getStackInSlot(i) != null) {
                            tInputs.add(tBus.getBaseMetaTileEntity().getStackInSlot(i));
                        }
                    }
                }
                if (tInputs.size() > 0) {
                    if (checkRecipeGeneric(tInputs.toArray(new ItemStack[0]), tFluids, 8, 90, 120, 10000)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return checkRecipeGeneric(8, 90, 120);
        }
    }

    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    public int getPollutionPerSecond(ItemStack aStack) {
        return CORE.ConfigSwitches.pollutionPerSecondMultiAdvEBF;
    }

    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    @Override
    public boolean checkRecipeGeneric(
            ItemStack[] aItemInputs,
            FluidStack[] aFluidInputs,
            int aMaxParallelRecipes,
            long aEUPercent,
            int aSpeedBonusPercent,
            int aOutputChanceRoll) {
        // Based on the Processing Array. A bit overkill, but very flexible.

        // Reset outputs and progress stats
        this.mEUt = 0;
        this.mMaxProgresstime = 0;
        this.mOutputItems = new ItemStack[] {};
        this.mOutputFluids = new FluidStack[] {};

        long tVoltage = getMaxInputVoltage();
        byte tTier = (byte) Math.max(1, GT_Utility.getTier(tVoltage));
        long tEnergy = getMaxInputEnergy();
        Logger.WARNING("Running checkRecipeGeneric(0)");

        GT_Recipe tRecipe = this.getRecipeMap()
                .findRecipe(
                        getBaseMetaTileEntity(),
                        mLastRecipe,
                        false,
                        gregtech.api.enums.GT_Values.V[tTier],
                        aFluidInputs,
                        aItemInputs);

        Logger.WARNING("Running checkRecipeGeneric(1)");
        // Remember last recipe - an optimization for findRecipe()
        this.mLastRecipe = tRecipe;

        if (tRecipe == null || this.mHeatingCapacity.getHeat() < tRecipe.mSpecialValue) {
            Logger.WARNING("BAD RETURN - 1");
            return false;
        }

        aMaxParallelRecipes = this.canBufferOutputs(tRecipe, aMaxParallelRecipes);
        if (aMaxParallelRecipes == 0) {
            Logger.WARNING("BAD RETURN - 2");
            return false;
        }

        // EU discount
        float tRecipeEUt = (tRecipe.mEUt * aEUPercent) / 100.0f;
        int tHeatCapacityDivTiers = (int) (mHeatingCapacity.getHeat() - tRecipe.mSpecialValue) / 900;
        if (tHeatCapacityDivTiers > 0) tRecipeEUt = (int) (tRecipeEUt * (Math.pow(0.95, tHeatCapacityDivTiers)));
        float tTotalEUt = 0.0f;

        int parallelRecipes = 0;
        // Count recipes to do in parallel, consuming input items and fluids and
        // considering input voltage limits
        for (; parallelRecipes < aMaxParallelRecipes && tTotalEUt < (tEnergy - tRecipeEUt); parallelRecipes++) {
            if (!tRecipe.isRecipeInputEqual(true, aFluidInputs, aItemInputs)) {
                Logger.WARNING("Broke at " + parallelRecipes + ".");
                break;
            }
            Logger.WARNING("Bumped EU from " + tTotalEUt + " to " + (tTotalEUt + tRecipeEUt) + ".");
            tTotalEUt += tRecipeEUt;
        }

        if (parallelRecipes == 0) {
            Logger.WARNING("BAD RETURN - 3");
            return false;
        }

        // -- Try not to fail after this point - inputs have already been consumed! --

        // Convert speed bonus to duration multiplier
        // e.g. 100% speed bonus = 200% speed = 100%/200% = 50% recipe duration.
        aSpeedBonusPercent = Math.max(-99, aSpeedBonusPercent);
        float tTimeFactor = 100.0f / (100.0f + aSpeedBonusPercent);
        this.mMaxProgresstime = (int) (tRecipe.mDuration * tTimeFactor);
        int tHalfHeatCapacityDivTiers = tHeatCapacityDivTiers / 2;

        this.mEUt = (int) Math.ceil(tTotalEUt);

        this.mEfficiency = (10000 - (getIdealStatus() - getRepairStatus()) * 1000);
        this.mEfficiencyIncrease = 10000;

        // Overclock
        if (this.mEUt <= 16) {
            this.mEUt = (this.mEUt * (1 << tTier - 1) * (1 << tTier - 1));
            this.mMaxProgresstime = (this.mMaxProgresstime / (1 << tTier - 1));
        } else {
            while (this.mEUt <= gregtech.api.enums.GT_Values.V[(tTier - 1)]) {
                this.mEUt *= 4;
                if (tHalfHeatCapacityDivTiers > 0) {
                    this.mMaxProgresstime = mMaxProgresstime / 4;
                    tHalfHeatCapacityDivTiers--;
                } else {
                    this.mMaxProgresstime = mMaxProgresstime / 2;
                }
                if (this.mMaxProgresstime <= 1) {
                    break;
                }
            }
        }
        if (this.mEUt > 0) {
            this.mEUt = (-this.mEUt);
        }

        this.mMaxProgresstime = Math.max(1, this.mMaxProgresstime);

        // Collect fluid outputs
        FluidStack[] tOutputFluids = new FluidStack[tRecipe.mFluidOutputs.length];
        for (int h = 0; h < tRecipe.mFluidOutputs.length; h++) {
            if (tRecipe.getFluidOutput(h) != null) {
                tOutputFluids[h] = tRecipe.getFluidOutput(h).copy();
                tOutputFluids[h].amount *= parallelRecipes;
            }
        }

        // Collect output item types
        ItemStack[] tOutputItems = new ItemStack[tRecipe.mOutputs.length];
        for (int h = 0; h < tRecipe.mOutputs.length; h++) {
            if (tRecipe.getOutput(h) != null) {
                tOutputItems[h] = tRecipe.getOutput(h).copy();
                tOutputItems[h].stackSize = 0;
            }
        }

        // Set output item stack sizes (taking output chance into account)
        for (int f = 0; f < tOutputItems.length; f++) {
            if (tRecipe.mOutputs[f] != null && tOutputItems[f] != null) {
                for (int g = 0; g < parallelRecipes; g++) {
                    if (getBaseMetaTileEntity().getRandomNumber(aOutputChanceRoll) < tRecipe.getOutputChance(f))
                        tOutputItems[f].stackSize += tRecipe.mOutputs[f].stackSize;
                }
            }
        }

        tOutputItems = removeNulls(tOutputItems);

        // Sanitize item stack size, splitting any stacks greater than max stack size
        List<ItemStack> splitStacks = new ArrayList<ItemStack>();
        for (ItemStack tItem : tOutputItems) {
            while (tItem.getMaxStackSize() < tItem.stackSize) {
                ItemStack tmp = tItem.copy();
                tmp.stackSize = tmp.getMaxStackSize();
                tItem.stackSize = tItem.stackSize - tItem.getMaxStackSize();
                splitStacks.add(tmp);
            }
        }

        if (splitStacks.size() > 0) {
            ItemStack[] tmp = new ItemStack[splitStacks.size()];
            tmp = splitStacks.toArray(tmp);
            tOutputItems = ArrayUtils.addAll(tOutputItems, tmp);
        }

        // Strip empty stacks
        List<ItemStack> tSList = new ArrayList<ItemStack>();
        for (ItemStack tS : tOutputItems) {
            if (tS.stackSize > 0) tSList.add(tS);
        }
        tOutputItems = tSList.toArray(new ItemStack[tSList.size()]);

        // Commit outputs
        this.mOutputItems = tOutputItems;
        this.mOutputFluids = tOutputFluids;
        updateSlots();

        // Play sounds (GT++ addition - GT multiblocks play no sounds)
        startProcess();

        Logger.WARNING("GOOD RETURN - 1");
        return true;
    }

    private int mGraceTimer = 2;

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        super.onPostTick(aBaseMetaTileEntity, aTick);
        // Try dry Pyrotheum after all other logic
        if (this.mStartUpCheck < 0) {
            if (this.mMaxProgresstime > 0 && this.mProgresstime != 0
                    || this.getBaseMetaTileEntity().hasWorkJustBeenEnabled()) {
                if (aTick % 10 == 0 || this.getBaseMetaTileEntity().hasWorkJustBeenEnabled()) {
                    if (!this.depleteInputFromRestrictedHatches(this.mPyrotheumHatches, 5)) {
                        if (mGraceTimer-- == 0) {
                            this.causeMaintenanceIssue();
                            this.stopMachine();
                            mGraceTimer = 2;
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getMaxParallelRecipes() {
        return 8;
    }

    @Override
    public int getEuDiscountForParallelism() {
        return 90;
    }

    @Override
    public void onModeChangeByScrewdriver(byte aSide, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        isBussesSeparate = !isBussesSeparate;
        aPlayer.addChatMessage(new ChatComponentTranslation(
                isBussesSeparate ? "interaction.separateBusses.enabled" : "interaction.separateBusses.disabled"));
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setBoolean("isBussesSeparate", isBussesSeparate);
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        isBussesSeparate = aNBT.getBoolean("isBussesSeparate");
        super.loadNBTData(aNBT);
    }

    public HeatingCoilLevel getCoilLevel() {
        return mHeatingCapacity;
    }

    public void setCoilLevel(HeatingCoilLevel aCoilLevel) {
        mHeatingCapacity = aCoilLevel;
    }
}
