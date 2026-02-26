package com.aewireless.compat.gtceu;

public class GTCeuPacketUtil {
    public static final String MetaMachineBlockEntity = "com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity";


    public static <T> T castIfInstance(Object object, String className) {
        try {
            Class<?> aClass = Class.forName(className);

            if (aClass.isInstance(object)) {
                return (T) aClass.cast(object);
            }
        } catch (ClassNotFoundException e) {
            // 类不存在，返回 null
        }
        return null;
    }


}
