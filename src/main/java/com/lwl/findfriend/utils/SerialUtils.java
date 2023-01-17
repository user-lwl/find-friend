package com.lwl.findfriend.utils;

import java.util.UUID;

/**
 * 序列工具类
 * @author SpartaEN
 */
public class SerialUtils {
    public static String getUUID()
    {
        return UUID.randomUUID().toString();
    }
}
