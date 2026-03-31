package com.maynkraft.randomspawn;

import com.maynkraft.randomspawn.util.IInitialSpawn;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.storage.LevelData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
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

	private static final Map<String, BlockPos> LEGACY_SPAWNS =  new HashMap<>();

	static {
		LEGACY_SPAWNS.put("benbatikan", new BlockPos(-1501, 88, 2910));
		LEGACY_SPAWNS.put("Konusabilen_At", new BlockPos(730, 96, -605));
		LEGACY_SPAWNS.put("RagnarokDeLight", new BlockPos(-77, 92, 2818));
		LEGACY_SPAWNS.put("aqatan", new BlockPos(3246, 70, 3374));
		LEGACY_SPAWNS.put("lilynaofc", new BlockPos(4403, 71, 2545));
		LEGACY_SPAWNS.put("Bahobey49", new BlockPos(-1318, 71, 3417));
		LEGACY_SPAWNS.put("MrCalisaL", new BlockPos(-3925, 100, 4620));
		LEGACY_SPAWNS.put("Alpercgf223", new BlockPos(1209, 65, 1458));
		LEGACY_SPAWNS.put("kaanshn", new BlockPos(-3279, 88, 699));
		LEGACY_SPAWNS.put("TATAMBO", new BlockPos(-4953, 64, -361));
		LEGACY_SPAWNS.put("MrSemih_K", new BlockPos(-4678, 140, -4539));
		LEGACY_SPAWNS.put("xyy902", new BlockPos(-3478, 63, 3041));
		LEGACY_SPAWNS.put("Riwero", new BlockPos(4230, 70, -312));
		LEGACY_SPAWNS.put("ArtinZZ", new BlockPos(-3487, 71, -2384));
		LEGACY_SPAWNS.put("Ninjamuz", new BlockPos(252, 74, 4458));
		LEGACY_SPAWNS.put("laserat", new BlockPos(590, 110, 2170));
		LEGACY_SPAWNS.put("Talhacilik961", new BlockPos(-4746, 100, -3014));
		LEGACY_SPAWNS.put("kulaege", new BlockPos(-3294, 71, 3677));
		LEGACY_SPAWNS.put("Elcakles", new BlockPos(2457, 63, 1285));
		LEGACY_SPAWNS.put("MertQWK", new BlockPos(4441, 68, 3159));
		LEGACY_SPAWNS.put("Aligma", new BlockPos(2769, 77, 2836));
		LEGACY_SPAWNS.put("can", new BlockPos(1675, 80, 4926));
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("RandomSpawn has been initialized.");

		// temporary initial spawn recovery
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayer player = handler.getPlayer();
			IInitialSpawn spawnData = (IInitialSpawn) player;

			if (spawnData.getInitialSpawn() == null) {
				String playerName = player.getGameProfile().name();
				if (LEGACY_SPAWNS.containsKey(playerName)) {
					BlockPos recoveredPos = LEGACY_SPAWNS.get(playerName);
					spawnData.setInitialSpawn(recoveredPos);
					LOGGER.info("✅ VERİ KURTARILDI: {} adlı eski oyuncunun ilk spawn noktası {} olarak mühürlendi!", playerName, recoveredPos.toShortString());
				}
			}
		});
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
				((IInitialSpawn) player).setInitialSpawn(pos);

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