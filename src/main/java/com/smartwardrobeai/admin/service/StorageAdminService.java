package com.smartwardrobeai.admin.service;

import com.smartwardrobeai.admin.model.dto.BatchDeleteDTO;
import com.smartwardrobeai.admin.model.dto.StorageFileQueryDTO;
import com.smartwardrobeai.admin.model.vo.BatchDeleteResultVO;
import com.smartwardrobeai.admin.model.vo.FilePreviewVO;
import com.smartwardrobeai.admin.model.vo.StorageFileVO;
import com.smartwardrobeai.admin.model.vo.StorageStatisticsVO;
import com.smartwardrobeai.common.model.entity.PageResult;

import java.io.InputStream;

public interface StorageAdminService {

    /**
     * 分页查询文件列表
     *
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    PageResult<StorageFileVO> pageQuery(StorageFileQueryDTO queryDTO);

    /**
     * 获取文件详情
     *
     * @param id 文件ID
     * @return 文件信息
     */
    StorageFileVO getDetail(Long id);

    /**
     * 获取存储统计信息
     *
     * @return 统计信息
     */
    StorageStatisticsVO getStatistics();

    /**
     * 删除单个文件
     *
     * @param id 文件ID
     */
    void deleteFile(Long id);

    /**
     * 批量删除文件
     *
     * @param batchDeleteDTO 批量删除参数
     * @return 删除结果
     */
    BatchDeleteResultVO batchDelete(BatchDeleteDTO batchDeleteDTO);

    /**
     * 下载文件
     *
     * @param id 文件ID
     * @return 文件输入流
     */
    InputStream downloadFile(Long id);

    /**
     * 获取文件预览URL
     *
     * @param id 文件ID
     * @param expiresIn 过期时间（秒），默认3600
     * @return 预览URL信息
     */
    FilePreviewVO getPreviewUrl(Long id, Integer expiresIn);
}

