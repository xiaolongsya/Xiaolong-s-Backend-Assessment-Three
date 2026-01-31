package org.xiaolong.openapisever.utils;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;



public class JsonUtil
{
    //对象转json字符串
    public static String toJson(Object obj)
    {
        return JSON.toJSONString(obj);
    }

    //json字符串转对象
    public static <T> T parseObject(String json, Class<T> clazz)
    {
        return JSON.parseObject(json, clazz);
    }

    //json字符串转复杂对象
    public static <T> T parseObject(String json, TypeReference<T> typeReference)
    {
        return JSON.parseObject(json, typeReference);
    }
}
