package dev.callumherr.modding.ducky_lib.events;

import dev.callumherr.modding.ducky_lib.DuckyLib;
import dev.callumherr.modding.ducky_lib.fluids.DkyFluidType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.fluids.FluidType;

@EventBusSubscriber(modid = DuckyLib.MODID, bus = EventBusSubscriber.Bus.GAME)
public class Event {

    @SubscribeEvent
    public static void playerTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!entity.isInFluidType() || entity.level().isClientSide) return;
        FluidType fluid = entity.level().getFluidState(entity.blockPosition()).getFluidType();
        if (!(fluid instanceof DkyFluidType fluidType)) return;

        if (entity instanceof Player player && fluidType.getEntityEffect() != null) {
            player.addEffect(fluidType.getEntityEffect());

        } else if (entity instanceof ItemEntity item) {
            ItemStack replacement = fluidType.getReplacementItem(item.getItem().getItem());
            if (replacement == null) return;

            int stackSize = item.getItem().getCount();
            int replacementSize = replacement.getCount();

            int countUsed = Math.min(stackSize, Math.floorDiv(64, replacementSize));
            entity.spawnAtLocation(new ItemStack(replacement.getItem(), countUsed * replacementSize));
            item.getItem().setCount(stackSize - countUsed);
        }
    }
}
