package org.xiaolong.openapisever.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.security.core.userdetails.User;

@Mapper
public interface UserMapper extends BaseMapper<User>
{
    //通过apikey查询用户
    @Select("select * from user where api_key = #{apiKey}")
    User selectByApiKey(String apiKey);
}
