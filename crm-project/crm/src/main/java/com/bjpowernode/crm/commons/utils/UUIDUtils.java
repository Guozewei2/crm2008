package com.bjpowernode.crm.commons.utils;

import java.util.UUID;

public class UUIDUtils {
    public static String getUUID(){
        /**
         * 获取UUID的值
         */
        return UUID.randomUUID().toString().replaceAll("-","");
    }
}
