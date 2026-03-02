package com.aewireless.compat.gtceu;

public class GTCeuPacketUtil {
    public static final String MetaMachineBlockEntity = "com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity";
    public static Class<?> aClass ;


    public static <T> T castIfInstance(Object object, String className) {
        try {

            if (aClass == null){
                aClass = Class.forName(className);
            }

            if (aClass.isInstance(object)) {
                return (T) aClass.cast(object);
            }
        } catch (ClassNotFoundException e) {
            // 类不存在，返回 null
        }
        return null;
    }


}
