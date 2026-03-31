package com.maynkraft.randomspawn.util;

import net.minecraft.core.BlockPos;

public interface IInitialSpawn {
    void setInitialSpawn(BlockPos pos);
    BlockPos getInitialSpawn();
}
