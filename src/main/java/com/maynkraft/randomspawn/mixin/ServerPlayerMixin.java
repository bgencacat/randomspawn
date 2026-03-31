package com.maynkraft.randomspawn.mixin;

import com.maynkraft.randomspawn.util.IInitialSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements IInitialSpawn {

    // Oyuncuya eklediğimiz yeni, gizli değişken
    @Unique
    private BlockPos initialSpawn = null;

    @Override
    public void setInitialSpawn(BlockPos pos) {
        this.initialSpawn = pos;
    }

    @Override
    public BlockPos getInitialSpawn() {
        return this.initialSpawn;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void onReadData(ValueInput input, CallbackInfo ci) {
        this.initialSpawn = input.read("initial_random_spawn", BlockPos.CODEC).orElse(null);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void onWriteData(ValueOutput output, CallbackInfo ci) {
        output.storeNullable("initial_random_spawn", BlockPos.CODEC, this.initialSpawn);
    }

    @Inject(method = "restoreFrom", at = @At("RETURN"))
    private void onRestoreFrom(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        this.initialSpawn = ((IInitialSpawn) oldPlayer).getInitialSpawn();
    }
}