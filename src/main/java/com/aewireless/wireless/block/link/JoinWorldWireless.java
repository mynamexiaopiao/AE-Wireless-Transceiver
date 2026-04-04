package com.aewireless.wireless.block.link;

import appeng.blockentity.networking.CableBusBlockEntity;
import com.aewireless.api.IWirelessBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber
public class JoinWorldWireless {
    private static final Set<WirelessContext> list = new LinkedHashSet<>();

    @SubscribeEvent
    public static void a(TickEvent.LevelTickEvent event){
        if (event.phase != TickEvent.Phase.END)return;

        Level level1 = event.level;

        if (!(level1 instanceof ServerLevel))return;
        if ((level1.getGameTime() % 20L) != 0L) return;

        if (list.isEmpty())return;

        List<WirelessContext> snapshot;
        synchronized (list) {
            if (list.isEmpty()) return;
            snapshot = new ArrayList<>(list);
        }

        List<WirelessContext> list1 = new ArrayList<>();
        for (WirelessContext posAndLevel : snapshot) {
            Level level = posAndLevel.level;
            BlockPos pos = posAndLevel.pos;
            BlockEntity be = level.getBlockEntity(pos);
            if (be == null ){
                list1.add(posAndLevel);
                continue;
            }

            if (be instanceof CableBusBlockEntity){
                    boolean invoke = ((IWirelessBlockEntity) be).updatePart();
                    if (invoke) {list1.add(posAndLevel);}
            }else {
                boolean invoke = ((IWirelessBlockEntity) be).updateHost();
                if (invoke) {list1.add(posAndLevel);}
            }

        }
        synchronized (list) {
            list.removeAll(list1);
        }
    }

    public static void add(Level level, BlockPos pos ) {
        if (level == null || pos == null) return;
        synchronized (list) {
            list.add(new WirelessContext(level, pos.immutable()));
        }
    }

    record WirelessContext(Level level , BlockPos pos) {}
}
