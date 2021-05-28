package com.vehiclecontacting.utils;

//关键词工具类（给搜索加点料）
public class KeywordUtils {

    //删除目标字符串的空格
    public static String deleteSpace(String keyword){
        return keyword.replaceAll("\\s","");
    }

}
