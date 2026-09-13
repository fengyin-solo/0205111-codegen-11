package com.redtourism.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.redtourism.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
