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
    /** JSON 数组，元素格式：{type:SPOT|HOTEL|FOOD, id, name, day, duration, note}（兼容旧格式 {spotId, spotName, day, order}） */
    private String spotData;
    /** DRAFT=草稿, SUBMITTED=已提交审核, APPROVED=已纳入推荐, REJECTED=驳回 */
    private String status;
    private String rejectReason;
    private Date createTime;
    private Date updateTime;

    @TableField(exist = false)
    private String username;
    /** 当前有效分享码（非数据库字段） */
    @TableField(exist = false)
    private String shareCode;
}
