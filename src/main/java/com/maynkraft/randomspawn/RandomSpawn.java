package com.maynkraft.randomspawn;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class RandomSpawn implements ModInitializer {
	public static final String MOD_ID = "randomspawn";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static int minX = -5000;
	public static int maxX = 5000;
	public static int minZ = -5000;
	public static int maxZ = 5000;
	private static final Random random = new Random();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("RandomSpawn has been initialized.");
	}

	public static boolean isFirstJoin(ServerPlayer player) {
		return player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) <= 10;
	}

	public static void setupRandomInitialSpawn(ServerPlayer player) {
		ServerLevel level = player.level();
		boolean foundLocation = false;

		while(!foundLocation) {
			int x = minX + random.nextInt(maxX - minX +1);
			int z = minZ + random.nextInt(maxZ - minZ +1);

			BlockPos pos = findSolidGround(level, x, z);

			if(pos != null && level.getBlockState(pos).isAir()) {
				foundLocation = true;

				player.setPos(pos.getX() + .5d, pos.getY(), pos.getZ() + .5d);

				LevelData.RespawnData respawnData = new LevelData.RespawnData(GlobalPos.of(level.dimension(), pos), player.getYRot(), player.getXRot());
				ServerPlayer.RespawnConfig respawnConfig = new ServerPlayer.RespawnConfig(respawnData, true);
				player.setRespawnPosition(respawnConfig, false);
			}
		}
	}

	private static BlockPos findSolidGround(ServerLevel level, int x, int z) {
		for (int y = level.getMaxY() -1; y > level.getMinY(); y--) {
			BlockPos pos = new BlockPos(x, y, z);
			if(level.getBlockState(pos).isSolidRender()) {
				return pos.above(1);
			}
		}
		return null;
	}
}