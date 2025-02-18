package gtPlusPlus.xmod.gregtech.common.tileentities.machines.multi.production.turbines;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Utility;
import gtPlusPlus.core.util.math.MathUtils;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class GT_MTE_LargeTurbine_SCSteam extends GregtechMetaTileEntity_LargerTurbineBase {

    public GT_MTE_LargeTurbine_SCSteam(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MTE_LargeTurbine_SCSteam(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MTE_LargeTurbine_SCSteam(mName);
    }

    @Override
    public int getCasingMeta() {
        return 15;
    }

    @Override
    public int getCasingTextureIndex() {
        return 1538;
    }

    @Override
    protected boolean requiresOutputHatch() {
        return true;
    }

    @Override
    public int getPollutionPerSecond(ItemStack aStack) {
        return 0;
    }

    @Override
    public int getFuelValue(FluidStack aLiquid) {
        return 0;
    }

    @Override
    int fluidIntoPower(ArrayList<FluidStack> aFluids, long aOptFlow, int aBaseEff, float[] flowMultipliers) {
        int tEU = 0;
        int totalFlow = 0; // Byproducts are based on actual flow
        int flow = 0;
        int remainingFlow = MathUtils.safeInt((long) (aOptFlow
                * 1.25f)); // Allowed to use up to 125% of optimal flow.  Variable required outside of loop for
        // multi-hatch scenarios.
        this.realOptFlow = (double) aOptFlow * (double) flowMultipliers[0];

        storedFluid = 0;
        FluidStack tSCSteam = FluidRegistry.getFluidStack("supercriticalsteam", 1);
        for (int i = 0; i < aFluids.size() && remainingFlow > 0; i++) {
            if (GT_Utility.areFluidsEqual(aFluids.get(i), tSCSteam, true)) {
                flow = Math.min(aFluids.get(i).amount, remainingFlow); // try to use up w/o exceeding remainingFlow
                depleteInput(new FluidStack(aFluids.get(i), flow)); // deplete that amount
                this.storedFluid += aFluids.get(i).amount;
                remainingFlow -= flow; // track amount we're allowed to continue depleting from hatches
                totalFlow += flow; // track total input used
            }
        }
        if (totalFlow <= 0) return 0;
        tEU = totalFlow;
        addOutput(GT_ModHandler.getSteam(totalFlow));
        if (totalFlow != aOptFlow) {
            float efficiency = 1.0f - Math.abs((totalFlow - aOptFlow) / (float) aOptFlow);
            // if(totalFlow>aOptFlow){efficiency = 1.0f;}
            tEU *= efficiency;
            tEU = Math.max(1, MathUtils.safeInt((long) tEU * (long) aBaseEff / 10000L));
        } else {
            tEU = MathUtils.safeInt((long) tEU * (long) aBaseEff / 10000L);
        }

        return (int) Math.min(tEU * 100L, Integer.MAX_VALUE);
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 8;
    }

    @Override
    public String getMachineType() {
        return "Large Supercritical Steam Turbine";
    }

    @Override
    protected String getTurbineType() {
        return "Supercritical Steam";
    }

    @Override
    protected String getCasingName() {
        return "Reinforced SC Turbine Casing";
    }

    @Override
    protected ITexture getTextureFrontFace() {
        return TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.LARGETURBINE_TI5);
    }

    @Override
    protected ITexture getTextureFrontFaceActive() {
        return TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.LARGETURBINE_TI_ACTIVE5);
    }
}
