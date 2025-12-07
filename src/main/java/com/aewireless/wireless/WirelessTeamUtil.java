package com.aewireless.wireless;

import com.aewireless.AeWireless;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 参考exap模组的代码，用于获取玩家所属的队伍名称
 */
public class WirelessTeamUtil {


    /**
     * 获取用于无线网络隔离的UUID
     * - 如果安装了FTBTeams且玩家在队伍中，返回队伍UUID（同队玩家共享）
     * - 否则返回玩家自己的UUID（独立网络）
     *
     * @param playerUUID 玩家UUID
     * @return 网络所有者UUID
     */
    public static UUID getNetworkOwnerUUID(UUID playerUUID) {
        if (playerUUID == null) {
            return null;
        }

        if (!AeWireless.IS_FTB_TEAMS_LOADED) {
            return playerUUID;
        }

        try {
            return getTeamUUID( playerUUID);
        } catch (Exception e) {
            // 如果FTBTeams API调用失败，回退到玩家UUID
            return playerUUID;
        }
    }

    private static UUID getTeamUUID(UUID playerUUID) {
        try {
            // 使用FTBTeams API
            var apiClass = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
            var api = apiClass.getMethod("api").invoke(null);  // 静态方法，返回API实例

            // 检查Manager是否已加载（在api实例上调用）
            Boolean isLoaded = (Boolean) api.getClass().getMethod("isManagerLoaded").invoke(api);

            if (!isLoaded) {
                return playerUUID;
            }

            var getManager = api.getClass().getMethod("getManager").invoke(api);

            if (getManager == null) {
                return playerUUID;
            }

            var managerClass = getManager.getClass();
            var getTeamForPlayer = managerClass.getMethod("getTeamForPlayerID", UUID.class);
            var teamOptional = getTeamForPlayer.invoke(getManager, playerUUID);

            if (teamOptional != null) {
                var optionalClass = teamOptional.getClass();
                var isPresent = (boolean) optionalClass.getMethod("isPresent").invoke(teamOptional);

                if (isPresent) {
                    var team = optionalClass.getMethod("get").invoke(teamOptional);
                    var teamClass = team.getClass();
                    return (UUID) teamClass.getMethod("getTeamId").invoke(team);
                }
            }
        } catch (Exception e) {
            // 反射调用失败，回退
        }

        return playerUUID;
    }

    private static Component getTeamName(ServerLevel level, UUID playerUUID) {
        try {
            var apiClass = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
            var api = apiClass.getMethod("api").invoke(null);

            // 检查Manager是否已加载
            Boolean isLoaded = (Boolean) api.getClass().getMethod("isManagerLoaded").invoke(api);
            if (!isLoaded) {
                // Manager未加载，回退到玩家名称
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerUUID);
                if (player != null) {
                    return player.getName();
                }
                return Component.literal(playerUUID.toString());
            }

            var getManager = api.getClass().getMethod("getManager").invoke(api);

            if (getManager == null) {
                return Component.literal(playerUUID.toString());
            }

            var managerClass = getManager.getClass();
            var getTeamForPlayer = managerClass.getMethod("getTeamForPlayerID", UUID.class);
            var teamOptional = getTeamForPlayer.invoke(getManager, playerUUID);

            if (teamOptional != null) {
                var optionalClass = teamOptional.getClass();
                var isPresent = (boolean) optionalClass.getMethod("isPresent").invoke(teamOptional);

                if (isPresent) {
                    var team = optionalClass.getMethod("get").invoke(teamOptional);
                    var teamClass = team.getClass();
                    return (Component) teamClass.getMethod("getName").invoke(team);
                }
            }
        } catch (Exception e) {
            // 反射调用失败，回退
        }

        // 回退到玩家名称
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerUUID);
        if (player != null) {
            return player.getName();
        }

        return Component.literal(playerUUID.toString());
    }


    private static boolean hasTeamOwner(ServerLevel level, UUID playerUUID) {
        try {
            var apiClass = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
            var api = apiClass.getMethod("api").invoke(null);

            // 检查Manager是否已加载
            Boolean isLoaded = (Boolean) api.getClass().getMethod("isManagerLoaded").invoke(api);
            if (!isLoaded) {
                return level.getServer().getPlayerList().getPlayer(playerUUID) != null;
            }

            var getManager = api.getClass().getMethod("getManager").invoke(api);

            if (getManager == null) {
                return level.getServer().getPlayerList().getPlayer(playerUUID) != null;
            }

            var managerClass = getManager.getClass();
            var getTeamForPlayer = managerClass.getMethod("getTeamForPlayerID", UUID.class);
            var teamOptional = getTeamForPlayer.invoke(getManager, playerUUID);

            if (teamOptional != null) {
                var optionalClass = teamOptional.getClass();
                return (boolean) optionalClass.getMethod("isPresent").invoke(teamOptional);
            }
        } catch (Exception e) {
            // 反射调用失败，回退
        }

        return level.getServer().getPlayerList().getPlayer(playerUUID) != null;
    }

    /**
     * 获取网络所有者的显示名称（用于UI显示）
     *
     * @param level      服务端世界
     * @param playerUUID 玩家UUID
     * @return 显示名称
     */
    public static Component getNetworkOwnerName(@Nullable ServerLevel level, UUID playerUUID) {
        if (AeWireless.IS_FTB_TEAMS_LOADED ) {
            try {
                return getTeamName(level, playerUUID);
            } catch (Exception ignored) {
            }
        }

        // 尝试获取玩家名称
        if (level != null) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerUUID);
            if (player != null) {
                return player.getName();
            }
        }

        return Component.literal(playerUUID.toString());
    }
}
