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

    private static final Object REFLECTION_LOCK = new Object();
    private static volatile boolean reflectionInitialized = false;
    private static volatile boolean reflectionAvailable = false;

    private static Class<?> apiClass;
    private static java.lang.reflect.Method apiMethod;
    private static java.lang.reflect.Method isManagerLoadedMethod;
    private static java.lang.reflect.Method getManagerMethod;

    private static Class<?> managerClassCache;
    private static java.lang.reflect.Method getTeamForPlayerMethod;

    private static Class<?> teamClassCache;
    private static java.lang.reflect.Method getTeamIdMethod;
    private static java.lang.reflect.Method getTeamNameMethod;

    private static void initReflection() {
        if (reflectionInitialized) return;
        synchronized (REFLECTION_LOCK) {
            if (reflectionInitialized) return;
            try {
                apiClass = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
                apiMethod = apiClass.getMethod("api");
                Object api = apiMethod.invoke(null);
                if (api != null) {
                    isManagerLoadedMethod = api.getClass().getMethod("isManagerLoaded");
                    getManagerMethod = api.getClass().getMethod("getManager");
                    reflectionAvailable = true;
                }
            } catch (Exception ignored) {
                reflectionAvailable = false;
            } finally {
                reflectionInitialized = true;
            }
        }
    }

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
        initReflection();
        if (!reflectionAvailable) return playerUUID;
        try {
            // 使用FTBTeams API
            var api = apiMethod.invoke(null);  // 静态方法，返回API实例

            // 检查Manager是否已加载（在api实例上调用）
            Boolean isLoaded = (Boolean) isManagerLoadedMethod.invoke(api);

            if (!isLoaded) {
                return playerUUID;
            }

            var getManager = getManagerMethod.invoke(api);

            if (getManager == null) {
                return playerUUID;
            }

            var managerClass = getManager.getClass();
            if (managerClassCache != managerClass || getTeamForPlayerMethod == null) {
                getTeamForPlayerMethod = managerClass.getMethod("getTeamForPlayerID", UUID.class);
                managerClassCache = managerClass;
            }
            var teamOptional = getTeamForPlayerMethod.invoke(getManager, playerUUID);

            if (teamOptional != null) {
                if (teamOptional instanceof java.util.Optional<?> optional) {
                    if (optional.isPresent()) {
                        Object team = optional.get();
                        if (team != null) {
                            var teamClass = team.getClass();
                            if (teamClassCache != teamClass || getTeamIdMethod == null) {
                                getTeamIdMethod = teamClass.getMethod("getTeamId");
                                teamClassCache = teamClass;
                            }
                            return (UUID) getTeamIdMethod.invoke(team);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 反射调用失败，回退
        }

        return playerUUID;
    }

    private static Component getTeamName(ServerLevel level, UUID playerUUID) {
        initReflection();
        if (!reflectionAvailable) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerUUID);
            if (player != null) return player.getName();
            return Component.literal(playerUUID.toString());
        }
        try {
            var api = apiMethod.invoke(null);

            // 检查Manager是否已加载
            Boolean isLoaded = (Boolean) isManagerLoadedMethod.invoke(api);
            if (!isLoaded) {
                // Manager未加载，回退到玩家名称
                ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerUUID);
                if (player != null) {
                    return player.getName();
                }
                return Component.literal(playerUUID.toString());
            }

            var getManager = getManagerMethod.invoke(api);

            if (getManager == null) {
                return Component.literal(playerUUID.toString());
            }

            var managerClass = getManager.getClass();
            if (managerClassCache != managerClass || getTeamForPlayerMethod == null) {
                getTeamForPlayerMethod = managerClass.getMethod("getTeamForPlayerID", UUID.class);
                managerClassCache = managerClass;
            }
            var teamOptional = getTeamForPlayerMethod.invoke(getManager, playerUUID);

            if (teamOptional != null) {
                if (teamOptional instanceof java.util.Optional<?> optional) {
                    if (optional.isPresent()) {
                        Object team = optional.get();
                        if (team != null) {
                            var teamClass = team.getClass();
                            if (teamClassCache != teamClass || getTeamNameMethod == null) {
                                getTeamNameMethod = teamClass.getMethod("getName");
                                teamClassCache = teamClass;
                            }
                            return (Component) getTeamNameMethod.invoke(team);
                        }
                    }
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
        initReflection();
        if (!reflectionAvailable) {
            return level.getServer().getPlayerList().getPlayer(playerUUID) != null;
        }
        try {
            var api = apiMethod.invoke(null);

            // 检查Manager是否已加载
            Boolean isLoaded = (Boolean) isManagerLoadedMethod.invoke(api);
            if (!isLoaded) {
                return level.getServer().getPlayerList().getPlayer(playerUUID) != null;
            }

            var getManager = getManagerMethod.invoke(api);

            if (getManager == null) {
                return level.getServer().getPlayerList().getPlayer(playerUUID) != null;
            }

            var managerClass = getManager.getClass();
            if (managerClassCache != managerClass || getTeamForPlayerMethod == null) {
                getTeamForPlayerMethod = managerClass.getMethod("getTeamForPlayerID", UUID.class);
                managerClassCache = managerClass;
            }
            var teamOptional = getTeamForPlayerMethod.invoke(getManager, playerUUID);

            if (teamOptional != null) {
                if (teamOptional instanceof java.util.Optional<?> optional) {
                    return optional.isPresent();
                }
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
