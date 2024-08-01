package org.cneko.toneko.fabric.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.cneko.toneko.fabric.items.ToNekoItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PistonBlockEntity.class)
public abstract class PistonBlockEntityMixin {
    @Inject(method = "pushEntities", at = @At("HEAD"))
    private static void onPushEntities(World world, BlockPos pos, float f, PistonBlockEntity blockEntity, CallbackInfo ci) {
        Direction direction = blockEntity.getMovementDirection();
        BlockPos startPos = pos.offset(direction, -2); // 活塞推前两个位置开始
        BlockPos endPos = pos.offset(direction, 2);   // 活塞推后两个位置结束

        // 遍历范围内所有方块位置
        for (BlockPos checkPos : BlockPos.iterate(startPos, endPos)) {
            BlockState blockState = world.getBlockState(checkPos);
            if (blockState.isOf(Blocks.MOVING_PISTON)) {
                // 获取关联的方块实体
                BlockEntity blockEntityAtPos = world.getBlockEntity(checkPos);
                if (blockEntityAtPos instanceof PistonBlockEntity) {
                    BlockState movedBlockState = ((PistonBlockEntity) blockEntityAtPos).getPushedBlock();
                    Box box = new Box(checkPos);
                    List<Entity> entities = world.getOtherEntities(null, box);
                    if (movedBlockState.isOf(Blocks.LIGHTNING_ROD) || movedBlockState.isOf(Blocks.END_ROD)) {
                        for (Entity entity : entities) {
                            if (entity instanceof LivingEntity) {
                                ItemStack helmet = ((LivingEntity) entity).getEquippedStack(EquipmentSlot.HEAD);
                                ItemStack chest = ((LivingEntity) entity).getEquippedStack(EquipmentSlot.CHEST);
                                if (helmet.getItem() == ToNekoItems.NEKO_EARS || chest.getItem() == ToNekoItems.NEKO_TAIL) {
                                    float healthBoost = 0.5F; // Amount of health to increase
                                    ((LivingEntity) entity).setHealth(Math.min(((LivingEntity) entity).getMaxHealth(), ((LivingEntity) entity).getHealth() + healthBoost));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}