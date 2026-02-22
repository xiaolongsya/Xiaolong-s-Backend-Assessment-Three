package org.xiaolong.openapisever.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaolong.openapisever.entity.ChatCompletion;
import org.xiaolong.openapisever.mapper.ChatCompletionMapper;
import org.xiaolong.openapisever.model.dto.ChatCompletionRequestDTO;
import org.xiaolong.openapisever.service.ChatService;

/**
 * 对话生成服务实现类
 * 负责生成记录的完整生命周期管理 [cite: 21, 62]
 */
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatCompletionMapper chatCompletionMapper;

    //根据id移除记录
    public boolean removeById(String id)
    {
        chatCompletionMapper.deleteById(id);
        return true;
    }

    //根据id查询记录
    public ChatCompletion getById(String completionId)
    {
        return chatCompletionMapper.selectById(completionId);
    }
    /**
     * 持久化初始请求
     */
    @Override
    public String saveRequest(String userId, ChatCompletionRequestDTO request) {
        // 生成符合 OpenAI 风格的 ID [cite: 51, 169]
        String completionId = "chatcmpl-" + IdUtil.simpleUUID();

        ChatCompletion entity = new ChatCompletion();
        entity.setId(completionId);
        entity.setUserId(userId);
        entity.setModel(request.getModel());
        // 将请求的消息列表序列化为 JSON 存储 [cite: 157-163]
        entity.setRequestMessages(JSON.toJSONString(request.getMessages()));
        entity.setStatus("processing");
        entity.setCreatedAt(System.currentTimeMillis() / 1000);

        chatCompletionMapper.insert(entity);
        return completionId;
    }

    /**
     * 更新最终生成结果 [cite: 62]
     */
    @Override
    public void completeRequest(String id, String content) {
        ChatCompletion entity = new ChatCompletion();
        entity.setId(id);
        entity.setResponseContent(content);
        entity.setStatus("completed");

        chatCompletionMapper.updateById(entity);
    }
}