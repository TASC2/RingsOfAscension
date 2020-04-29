package com.focamacho.ringsofascension.item.rings;

import com.focamacho.ringsofascension.item.ItemRingBase;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;

import java.util.Random;

public class ItemRingGrowth extends ItemRingBase {

    private int timer = 200;

    public ItemRingGrowth(Properties properties, String name, String tooltip) {
        super(properties, name, tooltip);
    }

    @Override
    public void tickCurio(String identifier, int index, LivingEntity livingEntity) {
        if(timer <= 0) {
            timer = 200;

            BlockPos entityPos = livingEntity.getPosition();
            int range = 5;
            int limit = 0;

            for(BlockPos pos : BlockPos.getAllInBoxMutable(entityPos.getX() - range, entityPos.getY() - range, entityPos.getZ() - range, entityPos.getX() + range, entityPos.getY() + range, entityPos.getZ() + range)) {
                if(limit > 3) break;

                BlockState state = livingEntity.world.getBlockState(pos);

                Random rand = new Random();
                if(rand.nextInt(100) < 90) continue;

                if(state.getBlock() instanceof IGrowable && state.getBlock() instanceof IPlantable && state.getBlock() instanceof CropsBlock) {
                    IGrowable igrowable = (IGrowable) state.getBlock();

                    if(igrowable.canGrow(livingEntity.world, pos, state, false)) {
                        if (livingEntity.world instanceof ServerWorld) {
                            igrowable.func_225535_a_((ServerWorld) livingEntity.world, livingEntity.world.rand, pos, state);
                        }

                        if(state != livingEntity.world.getBlockState(pos)) {
                            livingEntity.world.playEvent(2005, pos, 0);
                        }

                        limit++;
                    }
                }
            }

        } else {
            timer--;
        }
    }

}
