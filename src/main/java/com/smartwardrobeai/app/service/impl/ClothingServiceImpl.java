package com.smartwardrobeai.app.service.impl;

import com.alibaba.fastjson2.JSONObject;
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
     *
     * @param file   前端上传的文件
     * @param config 前端选择的模型配置 (包含 modelKey, 思考模式开关等)
     * @return AI 分析结果 VO (包含预填信息)
     */
    @Override
    public ClothingAnalysisVO uploadAndAnalyze(MultipartFile file, AiExecutionDTO config) {
        log.info("收到图片分析请求: {}", file.getOriginalFilename());
        log.info("tt");
        // 1. MinIO 存底 (图片必须先存下来)
        SysFile sysFile = fileStorageService.upload(file);
        try {
            // 2. 向工厂申请一个策略 (传入前端参数)
            // 工厂会自动查库、合并参数、处理 API Key，我们不用管
            AiAnalysisStrategy strategy = aiModelManager.createStrategy(config);
            // 3. 执行分析 (直接传入文件)
            // 策略内部会自动处理 Base64、HTTP 请求、JSON 解析
            JSONObject aiData = strategy.analyze(file);

            // 4. 匹配本地分类策略（从数据库查询，带缓存）
            SysCategoryStrategy catStrategy = categoryStrategyService.match(aiData.getString("category"));


            //返回vo
            return new ClothingAnalysisVO(
                    sysFile.getId(),
                    sysFile.getFileUrl(),
                    null, null,
                    catStrategy.getCategoryCode(),
                    catStrategy.getRegion(),
                    Integer.valueOf(catStrategy.getLayer()),
                    aiData.getString("color"),
                    aiData.getString("season"),
                    aiData.getString("fitType"),
                    aiData.getString("viewType")
            );
        } catch (Exception e) {
            log.error("AI 分析流程失败", e);
            // 🛡️ 降级处理：依然返回上传成功的图片，但分类信息留空，让用户手动填
            SysCategoryStrategy unknownStrategy = categoryStrategyService.match("Unknown");
            return new ClothingAnalysisVO(
                    sysFile.getId(), sysFile.getFileUrl(), null, null,
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