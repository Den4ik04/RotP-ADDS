package com.rotp.adds;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;

@Mod("rotp_adds")
public class RotpAdds {

    private final Random random = new Random();

    public RotpAdds() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isClientSide) {
            // КРАФТ НА ЗЕМЛЕ (без изменений, проверяем предметы раз в 0.5 сек)
            if (event.world.getGameTime() % 10 == 0) {
                List<ItemEntity> items = event.world.getEntitiesOfClass(ItemEntity.class, new AxisAlignedBB(0,0,0,0,0,0).inflate(256));
                for (ItemEntity ingot : items) {
                    if (isItem(ingot, "jojo:meteoric_ingot")) {
                        processGroundCraft(ingot);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level.isClientSide) {
            PlayerEntity player = event.player;
            long time = player.level.getGameTime();

            // 1. РАДАР (каждые 2 сек)
            if (time % 40 == 0 && player.getTags().contains("has_stand")) {
                checkProximity(player);
            }

            // 2. РАНДОМИЗАТОР СУДЬБЫ
            // Делаем проверку раз в 5 минут (6000 тиков), чтобы не нагружать сервер
            if (time % 6000 == 0) {
                // Шанс 10% (0.1), что именно в эти 5 минут игроку выпадет стрела.
                // В среднем это будет происходить раз в 50 минут игры, но абсолютно рандомно.
                if (random.nextFloat() < 0.10f) {
                    spawnDestinyArrow(player);
                }
            }
        }
    }

    private void checkProximity(PlayerEntity player) {
        List<PlayerEntity> players = player.level.getEntitiesOfClass(PlayerEntity.class, player.getBoundingBox().inflate(100));
        for (PlayerEntity other : players) {
            if (other != player && other.getTags().contains("has_stand")) {
                double dist = player.distanceTo(other);
                if (dist > 15) {
                    player.displayClientMessage(new StringTextComponent("Чувствуется присутствие другого Стенда...").withStyle(TextFormatting.RED), true);
                    player.level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN, SoundCategory.AMBIENT, 0.5f, 1.0f);
                }
            }
        }
    }

    private void processGroundCraft(ItemEntity ingot) {
        List<ItemEntity> nearby = ingot.level.getEntitiesOfClass(ItemEntity.class, ingot.getBoundingBox().inflate(1.5));
        ItemEntity stick = null; 
        ItemEntity feather = null;

        for (ItemEntity e : nearby) {
            if (e.getItem().getItem() == Items.STICK) stick = e;
            if (e.getItem().getItem() == Items.FEATHER) feather = e;
        }

        if (stick != null && feather != null) {
            ingot.remove();
            stick.getItem().shrink(1);
            feather.getItem().shrink(1);

            ingot.level.playSound(null, ingot.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.AMBIENT, 1.0f, 1.0f);
            spawnItem(ingot, "jojo:stand_arrow");
            
            PlayerEntity p = ingot.level.getNearestPlayer(ingot, 5);
            if (p != null) p.displayClientMessage(new StringTextComponent("НЕБЕСНЫЙ ДАР").withStyle(TextFormatting.GOLD, TextFormatting.BOLD), true);
        }
    }

    private void spawnDestinyArrow(PlayerEntity player) {
        player.displayClientMessage(new StringTextComponent("Судьба настигла тебя!").withStyle(TextFormatting.DARK_RED, TextFormatting.BOLD), false);
        player.level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundCategory.AMBIENT, 1.0f, 1.0f);
        spawnItem(player, "jojo:stand_arrow");
    }

    private void spawnItem(net.minecraft.entity.Entity anchor, String id) {
        ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(id)));
        ItemEntity entity = new ItemEntity(anchor.level, anchor.getX(), anchor.getY() + 1, anchor.getZ(), stack);
        anchor.level.addFreshEntity(entity);
    }

    private boolean isItem(ItemEntity entity, String id) {
        ResourceLocation res = entity.getItem().getItem().getRegistryName();
        return res != null && res.toString().equals(id);
    }
}
