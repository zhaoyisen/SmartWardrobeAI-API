package com.smartwardrobeai.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartwardrobeai.app.model.dto.AiExecutionDTO;
import com.smartwardrobeai.app.model.dto.ClothingCreateDTO;
import com.smartwardrobeai.app.model.dto.ClothingQueryDTO;
import com.smartwardrobeai.app.model.entity.Clothing;
import com.smartwardrobeai.app.model.vo.ClothingAnalysisVO;
import com.smartwardrobeai.app.model.vo.ClothingFilterOptionsVO;
import com.smartwardrobeai.app.model.vo.ClothingVO;
import com.smartwardrobeai.common.model.entity.PageResult;
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
     * 保存衣物（新增或编辑）
     * 当 dto.id 为 null 时表示新增，不为 null 时表示编辑
     *
     * @param dto 用户确认后的全量数据
     * @return 是否成功
     */
    boolean saveClothing(ClothingCreateDTO dto);

    /**
     * 查询衣橱列表
     *
     * @param queryDTO 查询参数（包含分页和筛选条件）
     * @return 分页结果
     */
    PageResult<ClothingVO> queryClothingList(ClothingQueryDTO queryDTO);

    /**
     * 删除衣物（逻辑删除）
     *
     * @param id 衣物ID
     * @return 是否成功
     */
    boolean deleteClothing(Long id);

    /**
     * 获取用户衣物的筛选选项
     * 用于前端展示查询条件
     *
     * @return 筛选选项VO，包含用户当前衣物已有的所有筛选条件选项
     */
    ClothingFilterOptionsVO getFilterOptions();
}