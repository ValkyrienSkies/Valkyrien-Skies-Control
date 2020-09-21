package org.valkyrienskies.addon.control;

import net.minecraft.block.Block;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.valkyrienskies.addon.control.block.multiblocks.*;
import org.valkyrienskies.addon.control.block.torque.TileEntityRotationAxle;
import org.valkyrienskies.addon.control.capability.ICapabilityLastRelay;
import org.valkyrienskies.addon.control.capability.ImplCapabilityLastRelay;
import org.valkyrienskies.addon.control.capability.StorageLastRelay;
import org.valkyrienskies.addon.control.config.VSControlConfig;
import org.valkyrienskies.addon.control.item.ItemPhysicsCore;
import org.valkyrienskies.addon.control.item.ItemRelayWire;
import org.valkyrienskies.addon.control.item.ItemVSWrench;
import org.valkyrienskies.addon.control.item.ItemVanishingWire;
import org.valkyrienskies.addon.control.network.VSGuiButtonHandler;
import org.valkyrienskies.addon.control.network.VSGuiButtonMessage;
import org.valkyrienskies.addon.control.proxy.CommonProxyControl;
import org.valkyrienskies.addon.control.tileentity.*;
import org.valkyrienskies.addon.world.ValkyrienSkiesWorld;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.command.config.VSConfigCommandBase;

import java.util.ArrayList;
import java.util.List;

@Mod(
    modid = ValkyrienSkiesControl.MOD_ID,
    dependencies = "required-after:" + ValkyrienSkiesMod.MOD_ID + ";required-after:" + ValkyrienSkiesWorld.MOD_ID + ";"
)
@Mod.EventBusSubscriber(modid = ValkyrienSkiesControl.MOD_ID)
public class ValkyrienSkiesControl {
    // Used for registering stuff
    public static final List<Block> BLOCKS = new ArrayList<>();
    public static final List<Item> ITEMS = new ArrayList<>();

    // MOD INFO CONSTANTS
    public static final String MOD_ID = "vs_control";

    public static SimpleNetworkWrapper controlGuiNetwork;

    // MOD INSTANCE
    @Instance(MOD_ID)
    public static ValkyrienSkiesControl INSTANCE;

    @SidedProxy(
        clientSide = "org.valkyrienskies.addon.control.proxy.ClientProxyControl",
        serverSide = "org.valkyrienskies.addon.control.proxy.CommonProxyControl")
    private static CommonProxyControl proxy;

    @CapabilityInject(ICapabilityLastRelay.class)
    public static final Capability<ICapabilityLastRelay> lastRelayCapability = null;

    private final Logger log = LogManager.getLogger(ValkyrienSkiesControl.class);
    public BlocksValkyrienSkiesControl vsControlBlocks;
    public Item relayWire;
    public Item vanishingWire;
    public Item vsWrench;
    public Item physicsCore;

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
        Block[] blockArray = BLOCKS.toArray(new Block[0]);
        event.getRegistry().registerAll(blockArray);
	}

	public void addBlocks() {
		INSTANCE.vsControlBlocks = new BlocksValkyrienSkiesControl();
	}

	public void registerMultiblocks() {
        MultiblockRegistry
        .registerAllPossibleSchematicVariants(ValkyriumEngineMultiblockSchematic.class);
        MultiblockRegistry
        .registerAllPossibleSchematicVariants(ValkyriumCompressorMultiblockSchematic.class);
        MultiblockRegistry
        .registerAllPossibleSchematicVariants(RudderAxleMultiblockSchematic.class);
        MultiblockRegistry
        .registerAllPossibleSchematicVariants(GiantPropellerMultiblockSchematic.class);
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(ITEMS.toArray(new Item[0]));
    }

    public void addItems() {
		INSTANCE.relayWire = new ItemRelayWire();
		INSTANCE.vanishingWire = new ItemVanishingWire();
		INSTANCE.vsWrench = new ItemVSWrench();
		INSTANCE.physicsCore = new ItemPhysicsCore();
	}

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        log.debug("Initializing configuration.");
        runConfiguration();

		addItems();
		addBlocks();
        registerNetworks();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
	    ServerCommandManager manager = (ServerCommandManager) event.getServer().getCommandManager();
	    manager.registerCommand(new VSConfigCommandBase("vscontrolconfig", VSControlConfig.class));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
		registerMultiblocks();
        registerTileEntities();
        registerCapabilities();
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    private void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityNetworkRelay.class,
            new ResourceLocation(MOD_ID, "tile_network_relay"));
        GameRegistry.registerTileEntity(TileEntityShipHelm.class,
            new ResourceLocation(MOD_ID, "tile_ship_helm"));
        GameRegistry.registerTileEntity(TileEntitySpeedTelegraph.class,
            new ResourceLocation(MOD_ID, "tile_speed_telegraph"));
        GameRegistry.registerTileEntity(TileEntityPropellerEngine.class,
            new ResourceLocation(MOD_ID, "tile_propeller_engine"));
        GameRegistry.registerTileEntity(TileEntityGyroscopeStabilizer.class,
            new ResourceLocation(MOD_ID, "tile_gyroscope_stabilizer"));
        GameRegistry.registerTileEntity(TileEntityLiftValve.class,
            new ResourceLocation(MOD_ID, "tile_lift_valve"));
        GameRegistry.registerTileEntity(TileEntityNetworkDisplay.class,
            new ResourceLocation(MOD_ID, "tile_network_display"));
        GameRegistry.registerTileEntity(TileEntityLiftLever.class,
            new ResourceLocation(MOD_ID, "tile_lift_lever"));

        GameRegistry.registerTileEntity(TileEntityGyroscopeDampener.class,
            new ResourceLocation(MOD_ID, "tile_gyroscope_dampener"));
        GameRegistry.registerTileEntity(TileEntityValkyriumEnginePart.class,
            new ResourceLocation(MOD_ID, "tile_valkyrium_engine_part"));
        GameRegistry.registerTileEntity(TileEntityGearbox.class,
            new ResourceLocation(MOD_ID, "tile_gearbox"));
        GameRegistry.registerTileEntity(TileEntityValkyriumCompressorPart.class,
            new ResourceLocation(MOD_ID, "tile_valkyrium_compressor_part"));
        GameRegistry.registerTileEntity(TileEntityRudderPart.class,
            new ResourceLocation(MOD_ID, "tile_rudder_part"));
        GameRegistry.registerTileEntity(TileEntityGiantPropellerPart.class,
            new ResourceLocation(MOD_ID, "tile_giant_propeller_part"));
        GameRegistry.registerTileEntity(TileEntityRotationAxle.class,
            new ResourceLocation(MOD_ID, "tile_rotation_axle"));

        GameRegistry.registerTileEntity(TileEntityPhysicsInfuser.class,
                new ResourceLocation(MOD_ID, "tile_physics_infuser"));
    }

    private void registerCapabilities() {
        CapabilityManager.INSTANCE.register(ICapabilityLastRelay.class, new StorageLastRelay(),
            ImplCapabilityLastRelay::new);
    }



	private static ResourceLocation getNameForRecipe(ItemStack output) {
		ResourceLocation baseLoc = new ResourceLocation(MOD_ID, output.getItem().getRegistryName().getPath());
		ResourceLocation recipeLoc = baseLoc;
		int index = 0;
		while (CraftingManager.REGISTRY.containsKey(recipeLoc)) {
			index++;
			recipeLoc = new ResourceLocation(MOD_ID, baseLoc.getPath() + "_" + index);
		}
		return recipeLoc;
	}

	private void registerNetworks() {
        controlGuiNetwork = NetworkRegistry.INSTANCE.newSimpleChannel("vs-control");
        controlGuiNetwork.registerMessage(VSGuiButtonHandler.class,
                VSGuiButtonMessage.class, 1, Side.SERVER);
    }

    /**
     * Initializes the configuration - [VSControlConfig]
     */
    private void runConfiguration() {
        VSControlConfig.sync();
    }
}
