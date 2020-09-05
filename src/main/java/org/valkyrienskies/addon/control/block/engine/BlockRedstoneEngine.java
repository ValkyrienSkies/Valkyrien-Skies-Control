package org.valkyrienskies.addon.control.block.engine;

import net.minecraft.block.material.Material;
import org.valkyrienskies.addon.control.config.VSControlConfig;
import org.valkyrienskies.mod.common.config.VSConfig;

public class BlockRedstoneEngine extends BlockAirshipEngineLore {

    public BlockRedstoneEngine() {
        super("redstone", Material.REDSTONE_LIGHT, VSControlConfig.ENGINE_POWER.redstoneEnginePower, 7.0F);
    }

    @Override
    public String getEnginePowerTooltip() {
        return enginePower + " * redstone power level";
    }

}
