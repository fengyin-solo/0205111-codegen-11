package com.redtourism.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.redtourism.entity.*;
import com.redtourism.mapper.*;
import com.redtourism.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

@Service
public class InteractionServiceImpl implements InteractionService {

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private LikeRecordMapper likeRecordMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean addComment(Comment comment) {
        return commentMapper.insert(comment) > 0;
    }

    @Override
    public IPage<Comment> listComments(int page, int size, String targetType, Long targetId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(targetType)) {
            wrapper.eq(Comment::getTargetType, targetType);
        }
        if (targetId != null) {
            wrapper.eq(Comment::getTargetId, targetId);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        IPage<Comment> result = commentMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(c -> {
            User user = userMapper.selectById(c.getUserId());
            if (user != null) {
                c.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
                c.setUserAvatar(user.getAvatar());
            }
        });
        return result;
    }

    @Override
    public IPage<Comment> listAllComments(int page, int size, String keyword) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Comment::getContent, keyword);
        }
        wrapper.orderByDesc(Comment::getCreateTime);
        IPage<Comment> result = commentMapper.selectPage(new Page<>(page, size), wrapper);
        result.getRecords().forEach(c -> {
            User user = userMapper.selectById(c.getUserId());
            if (user != null) {
                c.setUsername(user.getNickname() != null ? user.getNickname() : user.getUsername());
                c.setUserAvatar(user.getAvatar());
            }
        });
        return result;
    }

    @Override
    public Comment getCommentById(Long id) {
        return commentMapper.selectById(id);
    }

    @Override
    public boolean replyComment(Long commentId, String replyContent, Long adminId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("留言不存在");
        }
        comment.setReplyContent(replyContent);
        comment.setReplyTime(new Date());
        return commentMapper.updateById(comment) > 0;
    }

    @Override
    public boolean deleteComment(Long id) {
        return commentMapper.deleteById(id) > 0;
    }

    @Override
    public boolean addFavorite(Long userId, String targetType, Long targetId) {
        if (isFavorited(userId, targetType, targetId)) {
            throw new RuntimeException("已收藏");
        }
        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setTargetType(targetType);
        fav.setTargetId(targetId);
        return favoriteMapper.insert(fav) > 0;
    }

    @Override
    public boolean removeFavorite(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId);
        return favoriteMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<Favorite> listUserFavorites(Long userId, String targetType) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        if (StringUtils.hasText(targetType)) {
            wrapper.eq(Favorite::getTargetType, targetType);
        }
        wrapper.orderByDesc(Favorite::getCreateTime);
        return favoriteMapper.selectList(wrapper);
    }

    @Override
    public boolean addLike(Long userId, String targetType, Long targetId) {
        if (isLiked(userId, targetType, targetId)) {
            throw new RuntimeException("已点赞");
        }
        LikeRecord like = new LikeRecord();
        like.setUserId(userId);
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        return likeRecordMapper.insert(like) > 0;
    }

    @Override
    public boolean removeLike(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, targetType)
                .eq(LikeRecord::getTargetId, targetId);
        return likeRecordMapper.delete(wrapper) > 0;
    }

    @Override
    public boolean isLiked(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, targetType)
                .eq(LikeRecord::getTargetId, targetId);
        return likeRecordMapper.selectCount(wrapper) > 0;
    }

    @Override
    public long countLikes(String targetType, Long targetId) {
        LambdaQueryWrapper<LikeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LikeRecord::getTargetType, targetType)
                .eq(LikeRecord::getTargetId, targetId);
        return likeRecordMapper.selectCount(wrapper);
    }
}
