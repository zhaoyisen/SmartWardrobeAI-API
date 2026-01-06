package com.smartwardrobeai.app.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体类
 * 对应数据库表: users
 * <p>
 * 使用 Lombok (@Data, @Builder) 自动生成 Getter/Setter 和构建器模式。
 * 使用 MyBatis-Plus 注解定义表映射关系。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users") // MP注解：指定数据库表名
public class User extends BaseEntity {

    /**
     * 主键 ID
     * IdType.AUTO: 利用数据库的自增特性 (AUTO_INCREMENT)
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 邮箱 (登录凭证之一)
     */
    private String email;

    /**
     * 手机号 (登录凭证之二)
     */
    private String phone;

    /**
     * 加密后的密码 (BCrypt)
     *
     * @TableField: 指定数据库字段名，特别是当驼峰命名与下划线命名不一致时
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 头像 URL (存储 OSS 地址)
     */
    private String avatarUrl;

    /**
     * 身高 (cm) - 用于 AI 试穿缩放参考
     */
    private Integer height;

    /**
     * 体重 (kg) - 用于 AI 试穿体型参考
     */
    private Integer weight;

    /**
     * 状态 (1启用 0禁用)
     */
    private Integer status;

}