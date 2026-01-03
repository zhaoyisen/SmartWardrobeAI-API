package com.smartwardrobeai.app.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_file") // MP注解：指定数据库表名
@Schema(description = "系统文件信息实体")
public class SysFile extends BaseEntity {


    @TableId(type = IdType.AUTO)
    @Schema(description = "文件ID", example = "1")
    private Long id;

    /**
     * 原始文件名
     */
    @Schema(description = "原始文件名", example = "my_photo.jpg")
    private String fileName;

    /**
     * 文件存储路径 (OSS Key)
     */
    @Schema(description = "云存储路径/Key (用于删除或管理)", example = "2026/01/02/a1b2c3d4.jpg")
    private String filePath;

    /**
     * 完整访问 URL (快照)
     */
    @Schema(description = "完整访问URL (前端直接使用)", example = "http://oss.example.com/2026/01/02/a1b2c3d4.jpg")
    private String fileUrl;

    /**
     * 文件大小 (字节)
     */
    @Schema(description = "文件大小 (字节)", example = "1048576")
    private Long fileSize;

    /**
     * 文件扩展名 (例如 .jpg)
     */
    @Schema(description = "文件后缀类型", example = ".jpg")
    private String fileType;

    /**
     * 存储平台 (qiniu, aliyun, local)
     */
    @Schema(description = "存储平台 (qiniu/aliyun/local)", example = "qiniu")
    private String platform;

    /**
     * 上传人 (可存用户名或ID，暂时留空或存 'SYSTEM')
     */
    @Schema(description = "上传人标识", example = "user_1001")
    private String createBy;


    /**
     * 文件内容哈希 (MD5)
     * 用于防止重复上传
     */
    @Schema(description = "文件内容哈希(MD5)", example = "xxxxx")
    @TableField("file_hash")
    private String fileHash;


}
