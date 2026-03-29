package com.aewireless.wireless.block.link;

import net.minecraft.core.Direction;

import java.util.UUID;

public record WirelessData(String frequency , UUID uuid ,  Direction direction){
}
