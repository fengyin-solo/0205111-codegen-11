package com.redtourism.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.redtourism.entity.Comment;
import com.redtourism.entity.Favorite;
import com.redtourism.entity.LikeRecord;
import java.util.List;

public interface InteractionService {
    boolean addComment(Comment comment);
    IPage<Comment> listComments(int page, int size, String targetType, Long targetId);
    IPage<Comment> listAllComments(int page, int size, String keyword);
    Comment getCommentById(Long id);
    boolean replyComment(Long commentId, String replyContent, Long adminId);
    boolean deleteComment(Long id);

    boolean addFavorite(Long userId, String targetType, Long targetId);
    boolean removeFavorite(Long userId, String targetType, Long targetId);
    boolean isFavorited(Long userId, String targetType, Long targetId);
    List<Favorite> listUserFavorites(Long userId, String targetType);

    boolean addLike(Long userId, String targetType, Long targetId);
    boolean removeLike(Long userId, String targetType, Long targetId);
    boolean isLiked(Long userId, String targetType, Long targetId);
    long countLikes(String targetType, Long targetId);
}
