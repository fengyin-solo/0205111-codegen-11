package com.redtourism.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("hotel")
public class Hotel implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private String location;
    private String coverImage;
    private BigDecimal price;
    private Integer hasBreakfast;
    private Integer hasRoomService;
    private String phone;
    private Integer status;
    private Double rating;
    private Double longitude;
    private Double latitude;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
