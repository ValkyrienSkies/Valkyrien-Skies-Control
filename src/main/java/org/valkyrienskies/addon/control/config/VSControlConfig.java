package org.valkyrienskies.addon.control.config;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.valkyrienskies.addon.control.ValkyrienSkiesControl;
import org.valkyrienskies.mod.common.command.config.ShortName;
import org.valkyrienskies.mod.common.config.VSConfig;
import org.valkyrienskies.mod.common.config.VSConfigTemplate;

@SuppressWarnings("WeakerAccess") // NOTE: Any forge config option MUST be "public"
@Config(modid = ValkyrienSkiesControl.MOD_ID)
public class VSControlConfig extends VSConfigTemplate {

    @Config.Name("Compacted Valkyrium lift")
    public static double compactedValkyriumLift = 200000;

    @Config.Comment("Makes wrench toggle a multiblock's constructed state, removes modes.")
    public static boolean wrenchModeless = false;

    @Config.Comment({
            "Network Relay connections limit.",
            "How many components or relays can be connected.",
            "Default is 8."
    })
    public static int networkRelayLimit = 8;

    @Config.Comment({
            "Relay Wire Length",
            "How long, in metres, a single relay wire can extend.",
            "Default is 8m."
    })
    public static double relayWireLength = 8D;

    @Config.Name("Engine strength settings")
    @ShortName("enginePower")
    @Config.Comment("Legacy engine power. Engines must be replaced after changes are made.")
    public static final VSControlConfig.EnginePower ENGINE_POWER = new VSControlConfig.EnginePower();

    public static class EnginePower {

        @Config.RequiresMcRestart
        public double basicEnginePower = 2000;

        @Config.RequiresMcRestart
        public double advancedEnginePower = 5000;

        @Config.RequiresMcRestart
        public double eliteEnginePower = 10000;

        @Config.RequiresMcRestart
        public double ultimateEnginePower = 20000;

        @Config.RequiresMcRestart
        public double redstoneEnginePower = 50000;

    }


    /**
     * Synchronizes the data in this class and the data in the forge configuration
     */
    public static void sync() {
        ConfigManager.sync(ValkyrienSkiesControl.MOD_ID, Config.Type.INSTANCE);

        VSConfig.onSync();
    }

    @Mod.EventBusSubscriber(modid = ValkyrienSkiesControl.MOD_ID)
    @SuppressWarnings("unused")
    private static class EventHandler {

        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(ValkyrienSkiesControl.MOD_ID)) {
                sync();
            }
        }
    }
}
