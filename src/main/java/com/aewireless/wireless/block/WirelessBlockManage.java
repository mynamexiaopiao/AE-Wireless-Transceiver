package com.aewireless.wireless.block;

import net.minecraft.core.BlockPos;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class WirelessBlockManage {

    public WirelessBlockManage(){}

    private static  HashMap<BlockPos, WirelessBlockLink> blockPosList = new HashMap<>();

    public synchronized static  void addBlockPos(BlockPos pos , WirelessBlockLink link){
        blockPosList.put(pos, link);
    }



    public synchronized static  void removeBlockPos(BlockPos pos){
        blockPosList.remove(pos);
    }

    public synchronized static HashMap<BlockPos, WirelessBlockLink> getBlockPosList(){
        return blockPosList;
    }

    public synchronized static void setBlockPosList(HashMap<BlockPos, WirelessBlockLink> blockPosList){
        WirelessBlockManage.blockPosList = blockPosList;
    }

    public synchronized static void clearBlockPosList(){
        blockPosList.clear();
    }



}
