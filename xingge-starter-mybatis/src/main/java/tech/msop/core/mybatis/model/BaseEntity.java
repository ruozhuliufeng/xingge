package tech.msop.core.mybatis.model;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础实体类
 */
@Data
public class BaseEntity implements Serializable {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 创建人
     */
    private Long createUser;
    /**
     * 创建部门
     */
    private Long createDept;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改人
     */
    private Long updateUser;
    /**
     * 创建时间
     */
    private Date updateTime;

    /**
     * 业务状态 [1:正常]
     */
    private Integer status;

    /**
     * 状态[0:未删除 1：已删除]
     */
    private Integer isDeleted;

}
