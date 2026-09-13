package com.redtourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("feedback")
public class Feedback implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String contact;
    /** BUG | SUGGESTION | COMPLAINT | GENERAL */
    private String category;
    private String title;
    private String content;
    /** PENDING | PROCESSING | RESOLVED */
    private String status;
    private String reply;
    private Date replyTime;
    private Date createTime;

    @TableField(exist = false)
    private String username;
}
