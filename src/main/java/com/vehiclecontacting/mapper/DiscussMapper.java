package com.vehiclecontacting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.pojo.Discuss;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DiscussMapper extends BaseMapper<Discuss> {

    @Select("SELECT * FROM discuss , fans WHERE discuss.from_id = fans.to_id AND fans.from_id = #{fromId} ORDER BY discuss.update_time DESC")
    List<Discuss> getFollowDiscuss(Long fromId, Page<Discuss> page);

    @Select("SELECT * FROM discuss WHERE number = #{number}")
    Discuss selectDiscussWhenDeleted(Long number);

}
