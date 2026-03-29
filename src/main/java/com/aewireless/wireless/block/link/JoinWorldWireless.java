package com.aewireless.wireless.block.link;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPart;
import appeng.api.parts.PartHelper;
import appeng.blockentity.networking.CableBusBlockEntity;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Mod.EventBusSubscriber
public class JoinWorldWireless {
    private static List<WirelessContext> list = new CopyOnWriteArrayList<>();

    @SubscribeEvent
    public static void a(TickEvent.LevelTickEvent event){
        if (event.phase != TickEvent.Phase.END)return;

        Level level1 = event.level;

        if (!(level1 instanceof ServerLevel))return;

        if (list.isEmpty())return;

        List<WirelessContext> list1 = new ArrayList<>();
        for (WirelessContext posAndLevel : list) {
            Level level = posAndLevel.level;
            BlockPos pos = posAndLevel.pos;
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null ){
                list1.add(posAndLevel);
                continue;
            }

            try {
                if (be instanceof CableBusBlockEntity){
                    Method aewireless$updateWireless = be.getClass().getMethod("updatePart");
                    boolean invoke = (boolean)aewireless$updateWireless.invoke(be);
                    if (invoke) {list1.add(posAndLevel);}
                }else {
                    Method aewireless$updateWireless = be.getClass().getMethod("updateHost");
                    boolean invoke = (boolean)aewireless$updateWireless.invoke(be);
                    if (invoke) {list1.add(posAndLevel);}
                }

            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        list.removeAll(list1);
    }

    public static void add(Level level, BlockPos pos ) {
        list.add(new WirelessContext(level, pos ));
    }

    @Unique
    private static boolean isPart(Level level, BlockPos pos) {
        if (level == null || pos == null) {
            return false;
        }

        for (Direction direction : Direction.values()) {
            IPart part = PartHelper.getPart(level, pos, direction);
            if (part != null) {
                return true;
            }
        }

        return false;
    }

    record WirelessContext(Level level , BlockPos pos) {}
}
