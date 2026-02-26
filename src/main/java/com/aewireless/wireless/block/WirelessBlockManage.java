package com.aewireless.wireless.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    private static  HashMap<PosAndDirection, WirelessBlockLink> blockPosList = new HashMap<>();

    public synchronized static  void addBlockPos(PosAndDirection pos , WirelessBlockLink link){
        if (blockPosList.containsKey(pos)){
            WirelessBlockLink wirelessBlockLink = blockPosList.get(pos);
            if (wirelessBlockLink != null){
                wirelessBlockLink.destroyConnection();
            }
        }
        blockPosList.put(pos, link);
    }

    public synchronized static  void removeBlockPos(BlockPos pos , Direction direction){
        WirelessBlockLink wirelessBlockLink = blockPosList.get(new PosAndDirection(pos, direction));
        if (wirelessBlockLink != null){
            wirelessBlockLink.destroyConnection();
        }
        blockPosList.remove(new PosAndDirection(pos, direction));
    }

    public synchronized static HashMap<PosAndDirection, WirelessBlockLink> getBlockPosList(){
        return blockPosList;
    }

    public synchronized static void setBlockPosList(HashMap<PosAndDirection, WirelessBlockLink> blockPosList){
        WirelessBlockManage.blockPosList = blockPosList;
    }

    public synchronized static void clearBlockPosList(){
        blockPosList.clear();
    }

    public record PosAndDirection(BlockPos pos , Direction direction){

    }


}
