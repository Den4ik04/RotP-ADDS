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
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@Mod("rotp_adds")
public class RotpAdds {

    public RotpAdds() {
        // Регистрируем наш класс в шине событий Forge
        MinecraftForge.EVENT_BUS.register(this);
    }

    // --- МЕХАНИКА КРАФТА НА ЗЕМЛЕ ---
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isClientSide) {
            // Раз в полсекунды (10 тиков) проверяем предметы на земле для оптимизации
            if (event.world.getGameTime() % 10 == 0) {
                // Ищем все Meteoric Ingot (из мода jojo) на земле
                List<ItemEntity> items = event.world.getEntitiesOfClass(ItemEntity.class, new AxisAlignedBB(0, 0, 0, 0, 0, 0).inflate(256)); // В идеале искать рядом с игроками
                
                for (ItemEntity ingot : items) {
                    if (isItem(ingot, "jojo:meteoric_ingot")) {
                        checkGroundCraft(ingot);
                    }
                }
            }
        }
    }

    private void checkGroundCraft(ItemEntity ingot) {
        double r = 1.5;
        List<ItemEntity> nearby = ingot.level.getEntitiesOfClass(ItemEntity.class, ingot.getBoundingBox().inflate(r));
        
        ItemEntity stick = null;
        ItemEntity feather = null;

        for (ItemEntity e : nearby) {
            if (e.getItem().getItem() == Items.STICK) stick = e;
            if (e.getItem().getItem() == Items.FEATHER) feather = e;
        }

        if (stick != null && feather != null) {
            // Удаляем ингредиенты
            ingot.remove();
            stick.getItem().shrink(1);
            feather.getItem().shrink(1);

            // Создаем эффект метеорита
            ingot.level.playSound(null, ingot.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundCategory.AMBIENT, 1.0f, 1.0f);
            ingot.level.playSound(null, ingot.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.0f, 0.8f);

            // Спавним стрелу (jojo:stand_arrow)
            ItemStack arrowStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation("jojo:stand_arrow")));
            ItemEntity result = new ItemEntity(ingot.level, ingot.getX(), ingot.getY() + 0.5, ingot.getZ(), arrowStack);
            ingot.level.addFreshEntity(result);
            
            // Эффекты для игрока
            PlayerEntity closest = ingot.level.getNearestPlayer(ingot, 5.0);
            if (closest != null) {
                closest.displayClientMessage(new StringTextComponent("НЕБЕСНЫЙ ДАР").withStyle(TextFormatting.GOLD, TextFormatting.BOLD), true);
            }
        }
    }

    // --- РАДАР СТЕНДЮЗЕРОВ ---
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level.isClientSide) {
            PlayerEntity player = event.player;
            
            // Проверяем наличие тега (который выдает мод или ты сам)
            if (player.getTags().contains("has_stand") && player.level.getGameTime() % 40 == 0) { 
                List<PlayerEntity> victims = player.level.getEntitiesOfClass(PlayerEntity.class, player.getBoundingBox().inflate(100));
                
                for (PlayerEntity other : victims) {
                    if (other != player && other.getTags().contains("has_stand")) {
                        double dist = player.distanceTo(other);
                        if (dist > 15) {
                            player.displayClientMessage(new StringTextComponent("Чувствуется присутствие другого Стенда...").withStyle(TextFormatting.RED), true);
                            // Здесь можно добавить проигрывание звука
                        }
                    }
                }
            }
        }
    }

    // Хелпер для проверки ID предмета
    private boolean isItem(ItemEntity entity, String registryName) {
        return entity.getItem().getItem().getRegistryName().toString().equals(registryName);
    }
}
