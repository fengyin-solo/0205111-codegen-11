package com.redtourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("user_custom_route")
public class UserCustomRoute implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private Integer days;
    /** JSON 数组，元素格式：{type: SPOT/HOTEL/FOOD, refId, name, day, order, duration, note} */
    private String spotData;
    /** DRAFT=草稿, SUBMITTED=已提交审核, APPROVED=已纳入推荐, REJECTED=驳回 */
    private String status;
    private String rejectReason;
    /** 只读分享令牌（null 表示尚未生成分享链接） */
    private String shareToken;
    private Date shareExpireTime;
    /** 0=正常 1=创建者已删除（软删除，分享详情页需要提示同伴） */
    @TableLogic
    private Integer deleted;
    private Date createTime;
    private Date updateTime;

    @TableField(exist = false)
    private String username;
}
