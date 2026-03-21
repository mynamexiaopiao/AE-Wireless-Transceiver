package com.aewireless.wireless.block;

import com.aewireless.wireless.block.link.WirelessBlockLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashMap;

public class WirelessBlockManage {

    private static boolean dirty = false;

    private static  HashMap<PosAndDirection, WirelessBlockLink> blockPosList = new HashMap<>();

    public synchronized static  void addBlockPos(PosAndDirection pos , WirelessBlockLink link){
        for (Direction value : Direction.values()) {
            if (blockPosList.containsKey(new PosAndDirection(pos.pos , value))){
                WirelessBlockLink wirelessBlockLink = blockPosList.get(new PosAndDirection(pos.pos(), value));;
                if (wirelessBlockLink != null){
                    wirelessBlockLink.destroyConnection();
                    blockPosList.remove(new PosAndDirection(pos.pos, value));
                }
            }
        }
        blockPosList.put(pos, link);
        setDirty();
    }

    public synchronized static  void removeBlockPos(BlockPos pos ){

        for (Direction value : Direction.values()) {
            WirelessBlockLink wirelessBlockLink = blockPosList.get(new PosAndDirection(pos, value));
            if (wirelessBlockLink != null){
                wirelessBlockLink.destroyConnection();
            }
            blockPosList.remove(new PosAndDirection(pos, value));
        }
        setDirty();

    }


    public synchronized static boolean contains(PosAndDirection pos){
        return blockPosList.containsKey(pos);
    }

    public synchronized static HashMap<PosAndDirection, WirelessBlockLink> getBlockPosList(){
        return blockPosList;
    }

    public synchronized static void setBlockPosList(HashMap<PosAndDirection, WirelessBlockLink> blockPosList){
        WirelessBlockManage.blockPosList = blockPosList;
        setDirty();
    }

    public synchronized static void clearBlockPosList(){
        blockPosList.clear();
        setDirty();
    }


    public static void setDirty() {
        WirelessBlockManage.dirty = true;
    }

    public static void setUndirty() {
        WirelessBlockManage.dirty = false;
    }

    public static boolean isDirty() {
        return dirty;
    }

    public record PosAndDirection(BlockPos pos , Direction direction){

    }


}
