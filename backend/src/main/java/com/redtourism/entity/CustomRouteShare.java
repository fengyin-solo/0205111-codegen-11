package com.redtourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("custom_route_share")
public class CustomRouteShare implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long routeId;
    /** 创建者用户ID */
    private Long userId;
    /** 分享码（只读链接凭证） */
    private String shareCode;
    /** ACTIVE=有效, CLOSED=已关闭, DELETED=行程已删除 */
    private String status;
    private Date expireTime;
    private Date createTime;
}
