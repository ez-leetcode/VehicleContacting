package com.vehiclecontacting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vehiclecontacting.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
