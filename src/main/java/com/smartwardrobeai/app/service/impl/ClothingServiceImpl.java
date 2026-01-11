package com.smartwardrobeai.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartwardrobeai.admin.model.entity.SysCategoryStrategy;
import com.smartwardrobeai.admin.service.SysCategoryStrategyService;
import com.smartwardrobeai.app.ai.AiAnalysisStrategy;
import com.smartwardrobeai.app.ai.AiModelManager;
import com.smartwardrobeai.app.mapper.ClothingMapper;
import com.smartwardrobeai.app.model.dto.AiExecutionDTO;
import com.smartwardrobeai.app.model.dto.ClothingCreateDTO;
import com.smartwardrobeai.app.model.entity.Clothing;
import com.smartwardrobeai.app.model.entity.SysFile;
import com.smartwardrobeai.app.model.vo.ClothingAnalysisVO;
import com.smartwardrobeai.app.service.ClothingService;
import com.smartwardrobeai.app.service.FileStorageService;
import com.smartwardrobeai.common.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothingServiceImpl extends ServiceImpl<ClothingMapper, Clothing> implements ClothingService {

    // 文件服务 (MinIO)
    private final FileStorageService fileStorageService;
    //  AI 管理器
    private final AiModelManager aiModelManager;
    // 品类策略服务
    private final SysCategoryStrategyService categoryStrategyService;

    /**
     * Step 1: 上传图片并进行智能分析
     * <p>
     * 完整流程：
     * 1. 保存原始图片到MinIO
     * 2. 检测是否是衣物（如果不是，strategy 会抛出 BusinessException）
     * 3. 使用原始图片进行AI分析，strategy 内部会组装完整的 ClothingAnalysisVO
     * 4. 返回完整结果
     * </p>
     * <p>
     * 注意：
     * - 已暂时取消去除背景步骤，可能需要更换专门的模型来处理背景去除
     * - 业务逻辑（品类匹配、VO组装）已移到 strategy 层，service 层只负责流程编排
     * </p>
     *
     * @param file   前端上传的文件
     * @param config 前端选择的模型配置 (包含 modelKey, 思考模式开关等)
     * @return AI 分析结果 VO (包含预填信息)
     */
    @Override
    public ClothingAnalysisVO uploadAndAnalyze(MultipartFile file, AiExecutionDTO config) {
        log.info("收到图片分析请求: {}", file.getOriginalFilename());

        // ========== 步骤1: 保存原始图片到MinIO ==========
        SysFile originalSysFile = fileStorageService.upload(file);
        log.info("原始图片已保存: ID={}, URL={}", originalSysFile.getId(), originalSysFile.getFileUrl());

        // ========== 步骤2: 创建AI策略 ==========
        AiAnalysisStrategy strategy = aiModelManager.createStrategy(config);

        try {
            // ========== 步骤3: 检测是否是衣物 ==========
            // 使用图片URL进行检测（性能优化：避免Base64编码，减少请求体大小）
            // detect 方法返回 boolean，如果不是衣物会抛出 BusinessException（包含详细错误信息）
            log.info("开始检测图片中是否包含衣物（使用URL方式）...");
            boolean isClothing = strategy.detect(originalSysFile.getFileUrl());
            log.info("衣物检测通过: isClothing={}", isClothing);

            // ========== 步骤4: 使用原始图片进行AI分析 ==========
            // analyze 方法内部会处理品类匹配和VO组装，直接返回完整的 ClothingAnalysisVO
            log.info("开始执行AI分析...");
            ClothingAnalysisVO result = strategy.analyze(file, originalSysFile.getId(), originalSysFile.getFileUrl());
            
            log.info("AI分析完成: category={}, color={}", result.category(), result.color());
            return result;

        } catch (BusinessException e) {
            // 业务异常直接抛出（如检测到非衣物，detect 方法已包含详细错误信息）
            throw e;
        } catch (Exception e) {
            log.error("AI 分析流程失败", e);
            // 🛡️ 降级处理：依然返回上传成功的图片，但分类信息留空，让用户手动填
            SysCategoryStrategy unknownStrategy = categoryStrategyService.match("Unknown");
            return new ClothingAnalysisVO(
                    originalSysFile.getId(),
                    originalSysFile.getFileUrl(),
                    null,
                    null,
                    unknownStrategy.getCategoryCode(),
                    unknownStrategy.getRegion(),
                    Integer.valueOf(unknownStrategy.getLayer()),
                    "", "", "Regular", "Flat"
            );
        }
    }


    @Override
    public boolean createClothing(ClothingCreateDTO dto) {
        log.info("收到新增衣物请求: Category={}, Region={}", dto.category(), dto.region());

        // TODO: 1. Copy 属性到 Entity
        // TODO: 2. 填充 UserID
        // TODO: 3. 处理默认 Region 和 Layer (如果 DTO 为空)
        // TODO: 4. 处理默认 Name 和 Status
        // TODO: 5. save(entity)

        return false; // 占位返回
    }
}