package dev.callumherr.modding.ducky_lib;

import com.mojang.logging.LogUtils;
import dev.callumherr.modding.ducky_lib.entity.example.ModEntities;
import dev.callumherr.modding.ducky_lib.entity.example.client.GolemRenderer;
import dev.callumherr.modding.ducky_lib.entity.example.client.ModModelLayerThing;
import dev.callumherr.modding.ducky_lib.entity.example.client.model.GolemModel;
import dev.callumherr.modding.ducky_lib.fluids.DkyFluidType;
import dev.callumherr.modding.ducky_lib.fluids.test.FluidTypes;
import dev.callumherr.modding.ducky_lib.fluids.test.Fluids;
import dev.callumherr.modding.ducky_lib.fluids.test.ModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(DuckyLib.MODID)
public class DuckyLib
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "ducky_lib";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public DuckyLib(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        ModEntities.register(modEventBus);
        Fluids.register(modEventBus);
        FluidTypes.register(modEventBus);
        ModBlocks.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("HELLO from server starting");
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
            for (DeferredHolder<FluidType, DkyFluidType> fluid : DkyFluidType.registeredFluids) {
                event.registerFluidType(fluid.get().register(), fluid.get());
            };
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.GOLEM.get(), GolemRenderer::new);

            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(Fluids.GOOFLOWING.get(), RenderType.TRANSLUCENT);
                ItemBlockRenderTypes.setRenderLayer(Fluids.GOOSTILL.get(), RenderType.TRANSLUCENT);
            });
        }

        @SubscribeEvent
        public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(ModModelLayerThing.GOLEM, GolemModel::createBodyLayer);
        }
    }
}
