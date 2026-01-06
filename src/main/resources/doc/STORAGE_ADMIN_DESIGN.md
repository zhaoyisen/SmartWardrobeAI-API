# 后台存储管理模块详细设计文档

## 1. 概述

本文档描述后台管理端的MinIO存储管理模块的详细设计，包括数据库表结构、接口定义、DTO/VO设计、业务逻辑等。

## 2. 数据库设计

### 2.1 表结构说明

**表名：** `sys_file`（已存在，无需新建）

该表用于记录系统上传的文件信息，包括文件元数据和存储位置。

**表结构：**

```sql
CREATE TABLE `sys_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `file_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_path` varchar(500) DEFAULT NULL COMMENT '文件存储路径(OSS Key)',
  `file_url` varchar(500) DEFAULT NULL COMMENT '完整访问URL(快照)',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(10) DEFAULT NULL COMMENT '扩展名(.jpg/.png等)',
  `platform` varchar(20) DEFAULT 'minio' COMMENT '存储平台(minio/qiniu/aliyun/local)',
  `create_by` varchar(64) DEFAULT NULL COMMENT '上传人标识',
  `file_hash` varchar(64) DEFAULT NULL COMMENT '文件内容哈希(MD5)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_file_hash` (`file_hash`) COMMENT '文件哈希索引，用于秒传功能'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';
```

**字段说明：**

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| id | bigint | 主键ID | 1 |
| file_name | varchar(255) | 原始文件名 | "my_photo.jpg" |
| file_path | varchar(500) | MinIO存储路径 | "2026/01/02/a1b2c3d4.jpg" |
| file_url | varchar(500) | 完整访问URL | "http://localhost:9000/smart-wardrobe-ai/2026/01/02/a1b2c3d4.jpg" |
| file_size | bigint | 文件大小（字节） | 1048576 |
| file_type | varchar(10) | 文件扩展名 | ".jpg" |
| platform | varchar(20) | 存储平台 | "minio" |
| create_by | varchar(64) | 上传人标识 | "1001" |
| file_hash | varchar(64) | MD5哈希值 | "d41d8cd98f00b204e9800998ecf8427e" |
| create_time | datetime | 创建时间 | 2026-01-15 10:30:00 |
| update_time | datetime | 更新时间 | 2026-01-15 10:30:00 |

**索引说明：**
- 主键索引：`id`
- 哈希索引：`idx_file_hash`（用于快速查找重复文件，实现秒传功能）

## 3. 接口设计

### 3.1 接口概览

所有接口统一使用 `/api/admin/storage` 作为基础路径。

| 功能 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 分页查询文件 | GET | `/api/admin/storage/files/page` | 支持多条件筛选 |
| 获取文件详情 | GET | `/api/admin/storage/files/{id}` | 根据ID获取文件信息 |
| 获取存储统计 | GET | `/api/admin/storage/statistics` | 获取存储桶统计信息 |
| 删除单个文件 | DELETE | `/api/admin/storage/files/{id}` | 删除数据库记录和MinIO文件 |
| 批量删除文件 | DELETE | `/api/admin/storage/files/batch` | 批量删除文件 |
| 下载文件 | GET | `/api/admin/storage/files/{id}/download` | 下载文件流 |
| 获取预览URL | GET | `/api/admin/storage/files/{id}/preview` | 获取临时预览URL |

### 3.2 接口详细定义

#### 3.2.1 分页查询文件列表

**接口路径：** `GET /api/admin/storage/files/page`

**接口描述：** 分页查询文件列表，支持多条件筛选。可以查看数据库记录或MinIO实时文件。

**请求参数：**

| 参数名 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认1 | 1 |
| pageSize | Integer | 否 | 每页条数，默认10 | 10 |
| sortField | String | 否 | 排序字段 | "create_time" |
| isAsc | Boolean | 否 | 是否升序，默认false | false |
| fileName | String | 否 | 文件名（模糊查询） | "photo" |
| fileType | String | 否 | 文件类型（精确匹配） | ".jpg" |
| platform | String | 否 | 存储平台 | "minio" |
| createBy | String | 否 | 上传人标识 | "1001" |
| startTime | String | 否 | 开始时间（格式：yyyy-MM-dd HH:mm:ss） | "2026-01-01 00:00:00" |
| endTime | String | 否 | 结束时间（格式：yyyy-MM-dd HH:mm:ss） | "2026-01-31 23:59:59" |
| viewMode | String | 否 | 查看模式：db（数据库记录）、minio（MinIO实时文件）、both（两者都显示），默认db | "db" |

**请求示例：**

```http
GET /api/admin/storage/files/page?pageNum=1&pageSize=10&fileName=photo&fileType=.jpg&viewMode=db
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "fileName": "my_photo.jpg",
        "filePath": "2026/01/02/a1b2c3d4.jpg",
        "fileUrl": "http://localhost:9000/smart-wardrobe-ai/2026/01/02/a1b2c3d4.jpg",
        "fileSize": 1048576,
        "fileSizeFormatted": "1.00 MB",
        "fileType": ".jpg",
        "platform": "minio",
        "createBy": "1001",
        "fileHash": "d41d8cd98f00b204e9800998ecf8427e",
        "createTime": "2026-01-15T10:30:00",
        "updateTime": "2026-01-15T10:30:00",
        "existsInMinio": true,
        "accessible": true
      }
    ],
    "total": 100,
    "pages": 10,
    "current": 1,
    "size": 10
  }
}
```

**响应字段说明：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| records | Array | 文件列表 |
| total | Long | 总记录数 |
| pages | Long | 总页数 |
| current | Long | 当前页码 |
| size | Long | 每页条数 |

#### 3.2.2 获取文件详情

**接口路径：** `GET /api/admin/storage/files/{id}`

**接口描述：** 根据文件ID获取文件详细信息。

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 文件ID |

**请求示例：**

```http
GET /api/admin/storage/files/1
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "fileName": "my_photo.jpg",
    "filePath": "2026/01/02/a1b2c3d4.jpg",
    "fileUrl": "http://localhost:9000/smart-wardrobe-ai/2026/01/02/a1b2c3d4.jpg",
    "fileSize": 1048576,
    "fileSizeFormatted": "1.00 MB",
    "fileType": ".jpg",
    "platform": "minio",
    "createBy": "1001",
    "fileHash": "d41d8cd98f00b204e9800998ecf8427e",
    "createTime": "2026-01-15T10:30:00",
    "updateTime": "2026-01-15T10:30:00",
    "existsInMinio": true,
    "accessible": true
  }
}
```

#### 3.2.3 获取存储桶统计信息

**接口路径：** `GET /api/admin/storage/statistics`

**接口描述：** 获取MinIO存储桶的统计信息，包括总文件数、总大小等。

**请求参数：** 无

**请求示例：**

```http
GET /api/admin/storage/statistics
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalFiles": 1000,
    "totalSize": 1073741824,
    "totalSizeFormatted": "1.00 GB",
    "bucketName": "smart-wardrobe-ai"
  }
}
```

**响应字段说明：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| totalFiles | Long | 总文件数 |
| totalSize | Long | 总大小（字节） |
| totalSizeFormatted | String | 格式化后的总大小（如：1.00 GB） |
| bucketName | String | 存储桶名称 |

#### 3.2.4 删除单个文件

**接口路径：** `DELETE /api/admin/storage/files/{id}`

**接口描述：** 删除指定文件，同时删除数据库记录和MinIO中的文件。

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 文件ID |

**请求示例：**

```http
DELETE /api/admin/storage/files/1
```

**响应示例：**

```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

**错误响应示例：**

```json
{
  "code": 500,
  "message": "文件不存在或删除失败",
  "data": null
}
```

#### 3.2.5 批量删除文件

**接口路径：** `DELETE /api/admin/storage/files/batch`

**接口描述：** 批量删除文件，同时删除数据库记录和MinIO中的文件。

**请求体：**

```json
{
  "ids": [1, 2, 3, 4, 5]
}
```

**请求示例：**

```http
DELETE /api/admin/storage/files/batch
Content-Type: application/json

{
  "ids": [1, 2, 3]
}
```

**响应示例：**

```json
{
  "code": 200,
  "message": "批量删除成功，共删除3个文件",
  "data": {
    "successCount": 3,
    "failCount": 0,
    "failIds": []
  }
}
```

**响应字段说明：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| successCount | Integer | 成功删除的数量 |
| failCount | Integer | 失败的数量 |
| failIds | Array<Long> | 删除失败的文件ID列表 |

#### 3.2.6 下载文件

**接口路径：** `GET /api/admin/storage/files/{id}/download`

**接口描述：** 下载指定文件，返回文件流。

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 文件ID |

**请求示例：**

```http
GET /api/admin/storage/files/1/download
```

**响应：**
- Content-Type: 根据文件类型设置（如：image/jpeg, application/octet-stream）
- Content-Disposition: attachment; filename="my_photo.jpg"
- 响应体：文件二进制流

#### 3.2.7 获取文件预览URL

**接口路径：** `GET /api/admin/storage/files/{id}/preview`

**接口描述：** 获取文件的临时预览URL（带过期时间）。

**路径参数：**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 文件ID |

**查询参数：**

| 参数名 | 类型 | 必填 | 说明 | 默认值 |
|--------|------|------|------|--------|
| expiresIn | Integer | 否 | URL过期时间（秒） | 3600 |

**请求示例：**

```http
GET /api/admin/storage/files/1/preview?expiresIn=7200
```

**响应示例：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "previewUrl": "http://localhost:9000/smart-wardrobe-ai/2026/01/02/a1b2c3d4.jpg?X-Amz-Algorithm=...",
    "expiresAt": "2026-01-15T12:30:00"
  }
}
```

**响应字段说明：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| previewUrl | String | 临时预览URL（带签名） |
| expiresAt | String | URL过期时间（ISO 8601格式） |

## 4. DTO/VO设计

### 4.1 DTO（数据传输对象）

#### 4.1.1 StorageFileQueryDTO

**文件路径：** `src/main/java/com/smartwardrobeai/admin/model/dto/StorageFileQueryDTO.java`

**说明：** 文件查询DTO，继承 `BasePageQuery`，用于分页查询参数封装。

**字段定义：**

| 字段名 | 类型 | 说明 | 注解 |
|--------|------|------|------|
| pageNum | Integer | 页码 | 继承自BasePageQuery |
| pageSize | Integer | 每页条数 | 继承自BasePageQuery |
| sortField | String | 排序字段 | 继承自BasePageQuery |
| isAsc | Boolean | 是否升序 | 继承自BasePageQuery |
| fileName | String | 文件名（模糊查询） | @Query(type = QueryType.LIKE) |
| fileType | String | 文件类型（精确匹配） | @Query(type = QueryType.EQ) |
| platform | String | 存储平台 | @Query(type = QueryType.EQ) |
| createBy | String | 上传人标识 | @Query(type = QueryType.EQ) |
| startTime | String | 开始时间 | @Query(type = QueryType.GE, column = "create_time") |
| endTime | String | 结束时间 | @Query(type = QueryType.LE, column = "create_time") |
| viewMode | String | 查看模式：db/minio/both | 不参与数据库查询，用于业务逻辑判断 |

**代码示例：**

```java
@Data
@Schema(description = "【后台】存储文件分页查询参数")
public class StorageFileQueryDTO extends BasePageQuery {
    
    @Schema(description = "文件名（模糊查询）")
    @Query(type = QueryType.LIKE)
    private String fileName;
    
    @Schema(description = "文件类型（精确匹配）", example = ".jpg")
    @Query(type = QueryType.EQ)
    private String fileType;
    
    @Schema(description = "存储平台", example = "minio")
    @Query(type = QueryType.EQ)
    private String platform;
    
    @Schema(description = "上传人标识")
    @Query(type = QueryType.EQ)
    private String createBy;
    
    @Schema(description = "开始时间（格式：yyyy-MM-dd HH:mm:ss）")
    @Query(type = QueryType.GE, column = "create_time")
    private String startTime;
    
    @Schema(description = "结束时间（格式：yyyy-MM-dd HH:mm:ss）")
    @Query(type = QueryType.LE, column = "create_time")
    private String endTime;
    
    @Schema(description = "查看模式：db（数据库记录）、minio（MinIO实时文件）、both（两者都显示）", example = "db")
    private String viewMode = "db";
}
```

#### 4.1.2 BatchDeleteDTO

**文件路径：** `src/main/java/com/smartwardrobeai/admin/model/dto/BatchDeleteDTO.java`

**说明：** 批量删除DTO。

**字段定义：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| ids | List<Long> | 文件ID列表 |

**代码示例：**

```java
@Data
@Schema(description = "批量删除参数")
public class BatchDeleteDTO {
    
    @Schema(description = "文件ID列表", required = true)
    @NotEmpty(message = "文件ID列表不能为空")
    private List<Long> ids;
}
```

### 4.2 VO（视图对象）

#### 4.2.1 StorageFileVO

**文件路径：** `src/main/java/com/smartwardrobeai/admin/model/vo/StorageFileVO.java`

**说明：** 文件信息VO，用于返回给前端。

**字段定义：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | Long | 文件ID |
| fileName | String | 原始文件名 |
| filePath | String | 文件存储路径 |
| fileUrl | String | 完整访问URL |
| fileSize | Long | 文件大小（字节） |
| fileSizeFormatted | String | 格式化后的文件大小（如：1.00 MB） |
| fileType | String | 文件扩展名 |
| platform | String | 存储平台 |
| createBy | String | 上传人标识 |
| fileHash | String | 文件MD5哈希值 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |
| existsInMinio | Boolean | 是否存在于MinIO（仅viewMode为both或minio时返回） |
| accessible | Boolean | 文件是否可访问（仅viewMode为both或minio时返回） |

**代码示例：**

```java
@Data
@Schema(description = "【后台】存储文件信息")
public class StorageFileVO {
    
    @Schema(description = "文件ID")
    private Long id;
    
    @Schema(description = "原始文件名")
    private String fileName;
    
    @Schema(description = "文件存储路径")
    private String filePath;
    
    @Schema(description = "完整访问URL")
    private String fileUrl;
    
    @Schema(description = "文件大小（字节）")
    private Long fileSize;
    
    @Schema(description = "格式化后的文件大小")
    private String fileSizeFormatted;
    
    @Schema(description = "文件扩展名")
    private String fileType;
    
    @Schema(description = "存储平台")
    private String platform;
    
    @Schema(description = "上传人标识")
    private String createBy;
    
    @Schema(description = "文件MD5哈希值")
    private String fileHash;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "是否存在于MinIO")
    private Boolean existsInMinio;
    
    @Schema(description = "文件是否可访问")
    private Boolean accessible;
}
```

#### 4.2.2 StorageStatisticsVO

**文件路径：** `src/main/java/com/smartwardrobeai/admin/model/vo/StorageStatisticsVO.java`

**说明：** 存储统计信息VO。

**字段定义：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| totalFiles | Long | 总文件数 |
| totalSize | Long | 总大小（字节） |
| totalSizeFormatted | String | 格式化后的总大小 |
| bucketName | String | 存储桶名称 |

**代码示例：**

```java
@Data
@Schema(description = "【后台】存储统计信息")
public class StorageStatisticsVO {
    
    @Schema(description = "总文件数")
    private Long totalFiles;
    
    @Schema(description = "总大小（字节）")
    private Long totalSize;
    
    @Schema(description = "格式化后的总大小（如：1.00 GB）")
    private String totalSizeFormatted;
    
    @Schema(description = "存储桶名称")
    private String bucketName;
}
```

#### 4.2.3 FilePreviewVO

**文件路径：** `src/main/java/com/smartwardrobeai/admin/model/vo/FilePreviewVO.java`

**说明：** 文件预览URL VO。

**字段定义：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| previewUrl | String | 临时预览URL |
| expiresAt | LocalDateTime | URL过期时间 |

**代码示例：**

```java
@Data
@Schema(description = "文件预览URL信息")
public class FilePreviewVO {
    
    @Schema(description = "临时预览URL（带签名）")
    private String previewUrl;
    
    @Schema(description = "URL过期时间")
    private LocalDateTime expiresAt;
}
```

#### 4.2.4 BatchDeleteResultVO

**文件路径：** `src/main/java/com/smartwardrobeai/admin/model/vo/BatchDeleteResultVO.java`

**说明：** 批量删除结果VO。

**字段定义：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| successCount | Integer | 成功删除的数量 |
| failCount | Integer | 失败的数量 |
| failIds | List<Long> | 删除失败的文件ID列表 |

**代码示例：**

```java
@Data
@Schema(description = "批量删除结果")
public class BatchDeleteResultVO {
    
    @Schema(description = "成功删除的数量")
    private Integer successCount;
    
    @Schema(description = "失败的数量")
    private Integer failCount;
    
    @Schema(description = "删除失败的文件ID列表")
    private List<Long> failIds;
}
```

## 5. Service层设计

### 5.1 StorageAdminService接口

**文件路径：** `src/main/java/com/smartwardrobeai/admin/service/StorageAdminService.java`

**方法定义：**

| 方法名 | 返回类型 | 说明 |
|--------|----------|------|
| pageQuery | PageResult<StorageFileVO> | 分页查询文件列表 |
| getDetail | StorageFileVO | 获取文件详情 |
| getStatistics | StorageStatisticsVO | 获取存储统计信息 |
| deleteFile | void | 删除单个文件 |
| batchDelete | BatchDeleteResultVO | 批量删除文件 |
| downloadFile | InputStream | 获取文件下载流 |
| getPreviewUrl | FilePreviewVO | 获取文件预览URL |

### 5.2 StorageAdminServiceImpl实现类

**文件路径：** `src/main/java/com/smartwardrobeai/admin/service/impl/StorageAdminServiceImpl.java`

**核心实现逻辑：**

1. **分页查询：**
    - 根据viewMode判断查询模式
    - db模式：查询数据库记录
    - minio模式：查询MinIO存储桶中的文件
    - both模式：合并数据库记录和MinIO文件，去重后返回

2. **统计功能：**
    - 查询数据库中的总文件数和总大小
    - 格式化文件大小（B、KB、MB、GB）

3. **删除功能：**
    - 先查询数据库记录获取filePath
    - 删除MinIO中的文件
    - 删除数据库记录
    - 如果MinIO删除失败，记录日志但不影响数据库删除

4. **下载功能：**
    - 根据ID查询数据库获取filePath
    - 使用MinioClient.getObject获取文件流
    - 设置响应头（Content-Type、Content-Disposition）

5. **预览功能：**
    - 使用MinioClient.getPresignedObjectUrl生成临时URL
    - 设置过期时间（默认1小时）

## 6. Controller层设计

### 6.1 StorageAdminController

**文件路径：** `src/main/java/com/smartwardrobeai/admin/controller/StorageAdminController.java`

**接口映射：**

| 方法 | 路径 | HTTP方法 | 说明 |
|------|------|----------|------|
| page | /api/admin/storage/files/page | GET | 分页查询 |
| getDetail | /api/admin/storage/files/{id} | GET | 获取详情 |
| getStatistics | /api/admin/storage/statistics | GET | 获取统计 |
| delete | /api/admin/storage/files/{id} | DELETE | 删除单个 |
| batchDelete | /api/admin/storage/files/batch | DELETE | 批量删除 |
| download | /api/admin/storage/files/{id}/download | GET | 下载文件 |
| getPreview | /api/admin/storage/files/{id}/preview | GET | 获取预览URL |

## 7. 技术实现细节

### 7.1 MinIO操作

**依赖：** 使用 `io.minio.MinioClient`（已在项目中配置）

**主要API：**

1. **列出文件：**
   ```java
   Iterable<Result<Item>> results = minioClient.listObjects(
       ListObjectsArgs.builder()
           .bucket(bucketName)
           .prefix(prefix)
           .recursive(true)
           .build()
   );
   ```

2. **获取文件：**
   ```java
   InputStream stream = minioClient.getObject(
       GetObjectArgs.builder()
           .bucket(bucketName)
           .object(objectName)
           .build()
   );
   ```

3. **删除文件：**
   ```java
   minioClient.removeObject(
       RemoveObjectArgs.builder()
           .bucket(bucketName)
           .object(objectName)
           .build()
   );
   ```

4. **生成预览URL：**
   ```java
   String url = minioClient.getPresignedObjectUrl(
       GetPresignedObjectUrlArgs.builder()
           .method(Method.GET)
           .bucket(bucketName)
           .object(objectName)
           .expiry(expiresIn, TimeUnit.SECONDS)
           .build()
   );
   ```

### 7.2 文件大小格式化

**工具方法：**

```java
public static String formatFileSize(long size) {
    if (size < 1024) {
        return size + " B";
    } else if (size < 1024 * 1024) {
        return String.format("%.2f KB", size / 1024.0);
    } else if (size < 1024 * 1024 * 1024) {
        return String.format("%.2f MB", size / (1024.0 * 1024.0));
    } else {
        return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
}
```

### 7.3 数据一致性处理

1. **查询时检查：**
    - 当viewMode为both或minio时，需要检查数据库记录对应的MinIO文件是否存在
    - 使用 `minioClient.statObject()` 检查文件是否存在

2. **删除时同步：**
    - 先删除MinIO文件，再删除数据库记录
    - 如果MinIO删除失败，记录错误日志，但继续删除数据库记录（避免脏数据）

### 7.4 错误处理

**异常场景：**

1. **文件不存在：**
    - 数据库记录存在但MinIO文件不存在：标记 `existsInMinio = false`
    - 查询不存在的ID：返回404错误

2. **MinIO连接失败：**
    - 捕获异常，返回友好的错误提示
    - 记录详细错误日志

3. **删除失败：**
    - MinIO删除失败：记录日志，继续删除数据库记录
    - 数据库删除失败：抛出异常，回滚事务

## 8. 开发顺序建议

1. **第一阶段：基础功能**
    - 创建DTO和VO类
    - 实现Service接口和基础方法
    - 实现Controller接口

2. **第二阶段：查询功能**
    - 实现数据库查询（viewMode=db）
    - 实现MinIO文件列表查询（viewMode=minio）
    - 实现合并查询（viewMode=both）

3. **第三阶段：统计功能**
    - 实现存储桶统计
    - 实现文件大小格式化

4. **第四阶段：管理功能**
    - 实现单个文件删除
    - 实现批量删除
    - 实现文件下载
    - 实现预览URL生成

5. **第五阶段：测试和优化**
    - 单元测试
    - 集成测试
    - 性能优化

## 9. 注意事项

1. **权限控制：** 所有接口需要后台管理员权限，确保已配置拦截器
2. **文件大小限制：** 下载大文件时注意内存占用，使用流式传输
3. **预览URL过期：** 预览URL默认1小时过期，可根据需要调整
4. **数据一致性：** 定期检查数据库记录与MinIO文件的一致性
5. **日志记录：** 重要操作（删除、下载）需要记录操作日志
6. **性能优化：** 大量文件查询时考虑分页和索引优化

## 10. 扩展功能（可选）

1. **文件同步：** 提供接口同步MinIO文件到数据库
2. **文件迁移：** 支持文件在不同存储平台间迁移
3. **文件压缩：** 支持批量下载时压缩为ZIP
4. **文件预览：** 支持图片、PDF等文件的在线预览
5. **操作日志：** 记录文件操作的详细日志
6. **存储分析：** 按文件类型、时间等维度分析存储使用情况

