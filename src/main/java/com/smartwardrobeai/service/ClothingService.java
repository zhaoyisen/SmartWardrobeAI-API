package com.smartwardrobeai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartwardrobeai.model.dto.AiExecutionDTO;
import com.smartwardrobeai.model.dto.ClothingCreateDTO;
import com.smartwardrobeai.model.entity.Clothing;
import com.smartwardrobeai.model.vo.ClothingAnalysisVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 衣物业务逻辑接口
 */
public interface ClothingService extends IService<Clothing> {

    /**
     * Step 1: 上传图片并进行智能分析
     *
     * @param file   前端上传的文件
     * @param config 前端选择的模型配置 (包含 modelKey, 思考模式开关等)
     * @return AI 分析结果 VO (包含预填信息)
     */
    ClothingAnalysisVO uploadAndAnalyze(MultipartFile file, AiExecutionDTO config);

    /**
     * Step 3: 创建/保存衣物
     *
     * @param dto 用户确认后的全量数据
     * @return 是否成功
     */
    boolean createClothing(ClothingCreateDTO dto);
}