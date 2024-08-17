package dev.callumherr.modding.ducky_lib.events;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.callumherr.modding.ducky_lib.DuckyLib;
import dev.callumherr.modding.ducky_lib.block.impl.Multiblock;
import dev.callumherr.modding.ducky_lib.fluids.DkyFluidType;
import dev.callumherr.modding.ducky_lib.gson.JsonLoader;
import dev.callumherr.modding.ducky_lib.utils.debug.MultiBlockDebugRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.fluids.FluidType;

@EventBusSubscriber(modid = DuckyLib.MODID, bus = EventBusSubscriber.Bus.GAME)
public class Event {
    private static final Minecraft client = Minecraft.getInstance();
    private static final MultiBlockDebugRenderer debugRenderer = new MultiBlockDebugRenderer(client);
    @SubscribeEvent
    public static void playerTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!entity.isInFluidType() || entity.level().isClientSide) return;
        FluidType fluid = entity.level().getFluidState(entity.blockPosition()).getFluidType();
        if (!(fluid instanceof DkyFluidType fluidType)) return;

        if (entity instanceof Player player && fluidType.getEntityEffect() != null) {
            player.addEffect(fluidType.getEntityEffect());

        } else if (entity instanceof ItemEntity item) {
            replaceItems(item,fluidType,item.getItem());
        }
    }

    /**
     *
     * @param entity gets the current item entity.
     * @param type the custom fluid type of our library.
     * @param originalStack gets the original stack that needs to be replaced.
     *
     */
    public static void replaceItems(ItemEntity entity, DkyFluidType type, ItemStack originalStack)
    {
        ItemStack replacementStack = type.getReplacementItem(entity.getItem().getItem());

        if(replacementStack == null) return;

        int stackSize = originalStack.getCount();

        replacementStack.setCount(stackSize);
        originalStack.setCount(0);  //umm yeah will work
        entity.spawnAtLocation(replacementStack);

    }

    @SubscribeEvent
    public static void onPlayerPlaceBlock(BlockEvent.EntityPlaceEvent event) {
        Level world = event.getEntity().level();
        BlockPos pos = event.getPos();
        ResourceManager resourceManager = client.getResourceManager();

        for (Multiblock multiblock : JsonLoader.loadMultiblocks(world, resourceManager, DuckyLib.MODID)) {
            if (multiblock.checkMultiblock(world, pos)) {
                multiblock.onMultiblockFormed(world, pos);
            }
        }
    }
}
