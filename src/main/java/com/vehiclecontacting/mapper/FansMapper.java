package com.vehiclecontacting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.pojo.Fans;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FansMapper extends BaseMapper<Fans> {


   @Select("SELECT fans.from_id FROM fans , user WHERE fans.to_id = #{toId} AND user.id = fans.from_id AND user.username LIKE concat('%',#{keyword},'%') ORDER BY fans.create_time DESC")
   List<Fans> getFansByKeyword(Page<Fans> page,String keyword,Long toId);

   @Select("SELECT fans.to_id FROM fans , user WHERE fans.from_id = #{fromId} AND user.id = fans.to_id AND user.username LIKE concat('%',#{keyword},'%') ORDER BY fans.create_time DESC")
   List<Fans> getFollowByKeyword(Page<Fans> page,String keyword,Long fromId);

   @Select("SELECT fans.from_id FROM fans , user WHERE fans.to_id = #{toId} AND user.id = fans.from_id ORDER BY fans.create_time DESC")
   List<Fans> getFans(Page<Fans> page,Long toId);

   @Select("SELECT fans.to_id FROM fans , user WHERE fans.from_id = #{fromId} AND user.id = fans.to_id ORDER BY fans.create_time DESC")
   List<Fans> getFollow(Page<Fans> page,Long fromId);



}
