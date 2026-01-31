package org.xiaolong.openapisever.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChatCompletion extends BaseMapper<org.xiaolong.openapisever.entity.ChatCompletion>
{
    //根据completionId查询
    @Select("select * from cat_completion where completion_id = #{completionId}")
    org.xiaolong.openapisever.entity.ChatCompletion selectByCompletionId(String completionId);
}
