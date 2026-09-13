package com.redtourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("spot_suggestion")
public class SpotSuggestion implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long spotId;
    private String spotName;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String reason;
    private String status;
    private String rejectReason;
    private Date createTime;

    @TableField(exist = false)
    private String username;
}
