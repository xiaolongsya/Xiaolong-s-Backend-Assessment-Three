package org.xiaolong.openapisever.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.xiaolong.openapisever.entity.ChatCompletion;

@Mapper
public interface chatCompletion extends BaseMapper<ChatCompletion>
{
    //根据completionId查询
    @Select("select * from chat_completion where completion_id = #{completionId}")
    ChatCompletion selectByCompletionId(String completionId);
}
