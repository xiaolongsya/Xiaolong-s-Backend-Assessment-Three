package org.xiaolong.openapisever.utils;

// 生成唯一标识符
public class CompletionIdGenerator
{
     // 生成格式：chatcmpl-随机UUID（去除横线）
    public static String generateCompletionId()
    {
        return "chatcmpl-" + java.util.UUID.randomUUID().toString().replace("-", "");
    }
}
