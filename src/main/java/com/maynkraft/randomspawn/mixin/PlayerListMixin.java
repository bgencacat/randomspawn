package com.maynkraft.randomspawn.mixin;

import com.maynkraft.randomspawn.RandomSpawn;
import com.maynkraft.randomspawn.util.IInitialSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(PlayerList.class)
public class PlayerListMixin {
	@Inject(at = @At("HEAD"), method = "placeNewPlayer")
	private void onBeforePlayerSpawn(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		if(RandomSpawn.isFirstJoin(player)) {
			RandomSpawn.setupRandomInitialSpawn(player);
		}
	}

	@ModifyVariable(method = "respawn", at = @At("STORE"), ordinal = 0)
	private TeleportTransition onPlayerRespawn(TeleportTransition originalTransition, ServerPlayer serverPlayer, boolean keepAllPlayerData, Entity.RemovalReason removalReason) {
		if (originalTransition.missingRespawnBlock()) {
			// 2. Senin belirlediğin o rastgele lokasyonu alıyoruz
			ServerLevel overworld = serverPlayer.level().getServer().overworld();
			Vec3 customSpawnPos = getPlayerInitialRandomSpawn(serverPlayer);

			if (customSpawnPos != null && overworld != null) {
				// 3. Oyunun varsayılan merkezini silip, oyuncuyu kendi noktamıza yönlendiriyoruz
				return new TeleportTransition(
						overworld,                           // Doğacağı dünya (Overworld)
						customSpawnPos,                      // Senin belirlediğin X, Y, Z
						Vec3.ZERO,                           // Doğduğu anki hızı (hareketsiz)
						originalTransition.yRot(),           // Bakış açısı
						originalTransition.xRot(),
						Set.of(),                                // missingRespawnBlock'u true bırakıyoruz ki "Yatağın yok" mesajı gitsin
						TeleportTransition.DO_NOTHING        // PostState (Ekstra işlem yok)
				);
			}
		}

		// Eğer oyuncunun yatağı varsa veya özel lokasyon bulunamadıysa,
		// orijinal sistemi bozmadan geri döndür.
		return originalTransition;
	}

	@Unique
	private Vec3 getPlayerInitialRandomSpawn(ServerPlayer player) {
		// Oyuncunun içine mühürlediğimiz o ilk doğma noktasını okuyoruz
		BlockPos initialPos = ((IInitialSpawn) player).getInitialSpawn();

		if (initialPos != null) {
			// BlockPos'u Vec3'e çevirip ortalıyoruz (+0.5)
			return new Vec3(initialPos.getX() + 0.5, initialPos.getY(), initialPos.getZ() + 0.5);
		}

		// Eğer bir hata olursa ve veri bulunamazsa yedek (fallback) bir nokta ver
		return new Vec3(0, 100, 0);
	}
}