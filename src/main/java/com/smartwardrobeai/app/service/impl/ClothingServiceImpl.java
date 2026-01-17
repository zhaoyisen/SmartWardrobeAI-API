package com.smartwardrobeai.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartwardrobeai.admin.model.entity.SysCategoryStrategy;
import com.smartwardrobeai.admin.service.SysCategoryStrategyService;
import com.smartwardrobeai.app.ai.AiAnalysisStrategy;
import com.smartwardrobeai.app.ai.AiModelManager;
import com.smartwardrobeai.app.ai.SegmentationService;
import com.smartwardrobeai.app.mapper.ClothingMapper;
import com.smartwardrobeai.app.mapper.SysFileMapper;
import com.smartwardrobeai.app.model.dto.AiExecutionDTO;
import com.smartwardrobeai.app.model.dto.ClothingCreateDTO;
import com.smartwardrobeai.app.model.dto.ClothingQueryDTO;
import com.smartwardrobeai.app.model.entity.Clothing;
import com.smartwardrobeai.app.model.entity.SysFile;
import com.smartwardrobeai.app.model.enums.LayerEnum;
import com.smartwardrobeai.app.model.enums.RegionEnum;
import com.smartwardrobeai.app.model.vo.ClothingAnalysisVO;
import com.smartwardrobeai.app.model.vo.ClothingFilterOptionsVO;
import com.smartwardrobeai.app.model.vo.ClothingVO;
import com.smartwardrobeai.app.service.ClothingService;
import com.smartwardrobeai.app.service.FileStorageService;
import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.common.UserContext;
import com.smartwardrobeai.common.model.entity.PageResult;
import com.smartwardrobeai.utils.QueryGenerator;
import com.smartwardrobeai.utils.TechnicalIndicatorValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

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
    private final TechnicalIndicatorValidator technicalIndicatorValidator;
    private final SegmentationService segmentationService;
    // 文件 Mapper（用于验证文件是否存在）
    private final SysFileMapper sysFileMapper;

    /**
     * Step 1: 上传图片并进行智能分析
     * <p>
     * 完整流程：
     * 1. 保存原始图片到MinIO
     * 2. 对图片进行分割处理
     * 3. 上传分割后的图片到MinIO
     * 4. 使用分割后的图片进行校验和分析（合并为一次AI调用，降低成本）
     * 5. 返回完整结果
     * </p>
     * <p>
     * 注意：
     * - 使用分割后的图片进行AI分析，提高分析准确性
     * - 校验和分析合并为一次调用，降低AI模型调用成本
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
        //对图片进行基础校验
        technicalIndicatorValidator.validate(file);
        String segmentionImagesUrl;
        //对图片做分割
        try {
            segmentionImagesUrl = segmentationService.segmentByStream(file.getInputStream());
        } catch (Exception e) {
            log.error("衣物分割失败:{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
        //校验通过后，上传该衣物和分割后图片
        SysFile originalSysFile = fileStorageService.upload(file);

        log.info("原始图片已保存: ID={}, URL={}", originalSysFile.getId(), originalSysFile.getFileUrl());

        // 上传分割后的图片到MinIO
        String segmentedFileName = "segmented_" + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.png");
        SysFile segmentedSysFile = fileStorageService.uploadFromUrl(segmentionImagesUrl, segmentedFileName);
        log.info("分割图片已上传到MinIO: ID={}, URL={}", segmentedSysFile.getId(), segmentedSysFile.getFileUrl());


        // ========== 步骤2: 创建AI策略 ==========
        AiAnalysisStrategy strategy = aiModelManager.createStrategy(config);

        try {
            // ========== 步骤3: 使用分割后的图片进行校验和分析（合并为一次AI调用） ==========
            // analyzeWithValidation 方法会同时完成校验和分析，如果不是衣物或不符合要求会抛出 BusinessException
            log.info("开始使用分割后的图片进行校验和分析（合并调用）...");
            ClothingAnalysisVO result = strategy.analyzeWithValidation(
                    originalSysFile.getId(), 
                    originalSysFile.getFileUrl(), 
                    segmentedSysFile.getFileUrl()
            );
            log.info("校验和分析完成: category={}, color={}", result.category(), result.color());

            // 如果分割图片上传成功，且返回的VO中maskImageId和maskImageUrl为null，则使用分割图片信息
            if (segmentedSysFile != null && result.maskImageId() == null && result.maskImageUrl() == null) {
                result = new ClothingAnalysisVO(
                        result.imageId(),
                        result.imageUrl(),
                        segmentedSysFile.getId(),
                        segmentedSysFile.getFileUrl(),
                        result.category(),
                        result.region(),
                        result.defaultLayer(),
                        result.color(),
                        result.season(),
                        result.fitType(),
                        result.viewType()
                );
            }

            return result;

        } catch (BusinessException e) {
            // 业务异常直接抛出（如校验失败，analyzeWithValidation 方法已包含详细错误信息）
            throw e;
        } catch (Exception e) {
            log.error("AI 分析流程失败", e);
            // 🛡️ 降级处理：依然返回上传成功的图片，但分类信息留空，让用户手动填
            SysCategoryStrategy unknownStrategy = categoryStrategyService.match("Unknown");
            // 如果分割图片上传成功，在降级处理中也包含分割图片信息
            Long maskImageId = segmentedSysFile != null ? segmentedSysFile.getId() : null;
            String maskImageUrl = segmentedSysFile != null ? segmentedSysFile.getFileUrl() : null;
            return new ClothingAnalysisVO(originalSysFile.getId(), originalSysFile.getFileUrl(), maskImageId, maskImageUrl, unknownStrategy.getCategoryCode(), unknownStrategy.getRegion(), Integer.valueOf(unknownStrategy.getLayer()), "", "", "Regular", "Flat");
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveClothing(ClothingCreateDTO dto) {
        // 判断是新增还是编辑
        boolean isUpdate = dto.id() != null;
        log.info("收到{}衣物请求: ID={}, Category={}, Region={}, ImageId={}", 
                isUpdate ? "编辑" : "新增", dto.id(), dto.category(), dto.region(), dto.imageId());

        // 1. 验证用户登录状态
        Long userId = UserContext.getUserId();
        if (userId == null) {
            log.error("用户未登录，无法保存衣物");
            throw new BusinessException("用户未登录，请先登录");
        }

        // 2. 如果是编辑，验证衣物是否存在且属于当前用户
        if (isUpdate) {
            Clothing existingClothing = this.getById(dto.id());
            if (existingClothing == null) {
                log.error("衣物不存在: ID={}", dto.id());
                throw new BusinessException("衣物不存在");
            }
            if (!existingClothing.getUserId().equals(userId)) {
                log.error("无权编辑该衣物: ID={}, UserId={}, CurrentUserId={}", 
                        dto.id(), existingClothing.getUserId(), userId);
                throw new BusinessException("无权编辑该衣物");
            }
        }

        // 3. 验证图片ID是否存在
        SysFile imageFile = sysFileMapper.selectById(dto.imageId());
        if (imageFile == null) {
            log.error("原始图片不存在: ImageId={}", dto.imageId());
            throw new BusinessException("原始图片不存在，请重新上传");
        }

        // 4. 验证抠图ID（如果提供）
        if (dto.maskImageId() != null) {
            SysFile maskFile = sysFileMapper.selectById(dto.maskImageId());
            if (maskFile == null) {
                log.error("抠图文件不存在: MaskImageId={}", dto.maskImageId());
                throw new BusinessException("抠图文件不存在");
            }
        }

        // 5. 处理 region 和 defaultLayer（如果 DTO 为空，则从品类策略中获取）
        RegionEnum region = dto.region();
        Integer defaultLayer = dto.defaultLayer();

        if (region == null || defaultLayer == null) {
            // 根据品类获取策略
            SysCategoryStrategy strategy = categoryStrategyService.match(dto.category());
            log.info("从品类策略获取信息: Category={}, Strategy.Region={}, Strategy.Layer={}", 
                    dto.category(), strategy.getRegion(), strategy.getLayer());

            // 转换 region（String -> RegionEnum）
            if (region == null && strategy.getRegion() != null) {
                region = convertRegionFromString(strategy.getRegion());
                log.debug("自动填充 region: {}", region);
            }

            // 转换 defaultLayer（String -> Integer）
            if (defaultLayer == null && strategy.getLayer() != null) {
                defaultLayer = convertLayerFromString(strategy.getLayer());
                log.debug("自动填充 defaultLayer: {}", defaultLayer);
            }
        }

        // 6. 处理默认值
        // name: 如果为空，自动生成格式 "{color} {category}"
        String name = dto.name();
        if (name == null || name.trim().isEmpty()) {
            name = String.format("%s %s", dto.color(), dto.category());
            log.debug("自动生成衣物名称: {}", name);
        }

        // status: 如果为 null，默认为 1（在柜）
        Integer status = dto.status() != null ? dto.status() : 1;

        // wearCount: 如果为 null，默认为 0
        Integer wearCount = dto.wearCount() != null ? dto.wearCount() : 0;

        if (isUpdate) {
            // 编辑：使用 LambdaUpdateWrapper 更新
            LambdaUpdateWrapper<Clothing> updateWrapper = new LambdaUpdateWrapper<Clothing>()
                    .eq(Clothing::getId, dto.id())
                    .eq(Clothing::getUserId, userId) // 确保只能更新自己的衣物
                    .set(Clothing::getImageId, dto.imageId())
                    .set(Clothing::getMaskImageId, dto.maskImageId())
                    .set(Clothing::getName, name)
                    .set(Clothing::getRegion, region)
                    .set(Clothing::getCategory, dto.category())
                    .set(Clothing::getDefaultLayer, defaultLayer != null ? defaultLayer : 2)
                    .set(Clothing::getColor, dto.color())
                    .set(Clothing::getSeason, dto.season())
                    .set(Clothing::getFitType, dto.fitType())
                    .set(Clothing::getViewType, dto.viewType())
                    .set(Clothing::getShelfNo, dto.shelfNo())
                    .set(Clothing::getBrand, dto.brand())
                    .set(Clothing::getSize, dto.size())
                    .set(Clothing::getPrice, dto.price())
                    .set(Clothing::getPurchaseDate, dto.purchaseDate())
                    .set(Clothing::getStatus, status)
                    .set(Clothing::getWearCount, wearCount);

            boolean updated = this.update(updateWrapper);
            if (updated) {
                log.info("衣物编辑成功: ID={}, Name={}, UserId={}", dto.id(), name, userId);
            } else {
                log.error("衣物编辑失败: ID={}, Name={}, UserId={}", dto.id(), name, userId);
                throw new BusinessException("衣物编辑失败，请稍后重试");
            }
            return updated;
        } else {
            // 新增：构建实体并保存
            Clothing clothing = Clothing.builder()
                    .userId(userId)
                    .imageId(dto.imageId())
                    .maskImageId(dto.maskImageId())
                    .name(name)
                    .region(region)
                    .category(dto.category())
                    .defaultLayer(defaultLayer != null ? defaultLayer : 2) // 如果还是 null，默认使用 2（MIDDLE）
                    .color(dto.color())
                    .season(dto.season())
                    .fitType(dto.fitType())
                    .viewType(dto.viewType())
                    .shelfNo(dto.shelfNo())
                    .brand(dto.brand())
                    .size(dto.size())
                    .price(dto.price())
                    .purchaseDate(dto.purchaseDate())
                    .status(status)
                    .wearCount(wearCount)
                    .build();

            boolean saved = this.save(clothing);
            if (saved) {
                log.info("衣物创建成功: ID={}, Name={}, UserId={}", clothing.getId(), clothing.getName(), userId);
            } else {
                log.error("衣物创建失败: Name={}, UserId={}", name, userId);
                throw new BusinessException("衣物创建失败，请稍后重试");
            }
            return saved;
        }
    }

    /**
     * 将字符串转换为 RegionEnum
     * 
     * @param regionStr region 字符串（如 "TOP", "BOTTOM"）
     * @return RegionEnum 枚举值
     */
    private RegionEnum convertRegionFromString(String regionStr) {
        if (regionStr == null || regionStr.trim().isEmpty()) {
            return null;
        }
        try {
            // 尝试直接使用 valueOf（要求字符串完全匹配枚举名）
            return RegionEnum.valueOf(regionStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            // 如果 valueOf 失败，尝试通过 code 查找
            for (RegionEnum region : RegionEnum.values()) {
                if (region.getCode().equalsIgnoreCase(regionStr.trim())) {
                    return region;
                }
            }
            log.warn("无法转换 region 字符串: {}, 使用默认值 TOP", regionStr);
            return RegionEnum.TOP; // 默认返回 TOP
        }
    }

    /**
     * 将字符串转换为 Layer 的 Integer 值
     * 
     * @param layerStr layer 字符串（如 "INNER", "MIDDLE", "OUTER", "ACCESSORY"）
     * @return LayerEnum 的 code 值（1, 2, 3, 4）
     */
    private Integer convertLayerFromString(String layerStr) {
        if (layerStr == null || layerStr.trim().isEmpty()) {
            return 2; // 默认返回 MIDDLE
        }
        try {
            // 尝试使用枚举名查找
            LayerEnum layer = LayerEnum.valueOf(layerStr.toUpperCase().trim());
            return layer.getCode();
        } catch (IllegalArgumentException e) {
            log.warn("无法转换 layer 字符串: {}, 使用默认值 2 (MIDDLE)", layerStr);
            return 2; // 默认返回 MIDDLE (2)
        }
    }

    @Override
    public PageResult<ClothingVO> queryClothingList(ClothingQueryDTO queryDTO) {
        log.info("查询衣橱列表: region={}, pageNum={}, pageSize={}", 
                queryDTO.getRegion(), queryDTO.getPageNum(), queryDTO.getPageSize());

        // 1. 获取当前登录用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            log.error("用户未登录，无法查询衣橱");
            throw new BusinessException("用户未登录，请先登录");
        }

        // 2. 构建分页对象
        Page<Clothing> page = queryDTO.toMpPage("create_time", false);

        // 3. 构建查询条件
        QueryWrapper<Clothing> wrapper = QueryGenerator.generate(queryDTO);
        // 必须添加用户ID条件
        wrapper.eq("user_id", userId);

        // 4. 执行分页查询
        Page<Clothing> resultPage = this.page(page, wrapper);

        // 5. 批量查询图片URL
        List<Clothing> clothingList = resultPage.getRecords();
        Map<Long, String> imageUrlMap = new HashMap<>();
        Map<Long, String> maskImageUrlMap = new HashMap<>();

        if (!clothingList.isEmpty()) {
            // 收集所有图片ID
            Set<Long> imageIds = clothingList.stream()
                    .map(Clothing::getImageId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Set<Long> maskImageIds = clothingList.stream()
                    .map(Clothing::getMaskImageId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // 批量查询图片信息
            if (!imageIds.isEmpty()) {
                List<SysFile> imageFiles = sysFileMapper.selectBatchIds(imageIds);
                imageUrlMap = imageFiles.stream()
                        .filter(file -> file.getFileUrl() != null)
                        .collect(Collectors.toMap(SysFile::getId, SysFile::getFileUrl, (v1, v2) -> v1));
            }

            if (!maskImageIds.isEmpty()) {
                List<SysFile> maskImageFiles = sysFileMapper.selectBatchIds(maskImageIds);
                maskImageUrlMap = maskImageFiles.stream()
                        .filter(file -> file.getFileUrl() != null)
                        .collect(Collectors.toMap(SysFile::getId, SysFile::getFileUrl, (v1, v2) -> v1));
            }
        }

        // 6. 转换为VO
        final Map<Long, String> finalImageUrlMap = imageUrlMap;
        final Map<Long, String> finalMaskImageUrlMap = maskImageUrlMap;

        return PageResult.of(resultPage, clothing -> {
            ClothingVO vo = ClothingVO.builder()
                    .id(clothing.getId())
                    .userId(clothing.getUserId())
                    .imageId(clothing.getImageId())
                    .imageUrl(finalImageUrlMap.get(clothing.getImageId()))
                    .maskImageId(clothing.getMaskImageId())
                    .maskImageUrl(clothing.getMaskImageId() != null ? finalMaskImageUrlMap.get(clothing.getMaskImageId()) : null)
                    .name(clothing.getName())
                    .region(clothing.getRegion())
                    .category(clothing.getCategory())
                    .defaultLayer(clothing.getDefaultLayer())
                    .color(clothing.getColor())
                    .season(clothing.getSeason())
                    .fitType(clothing.getFitType())
                    .viewType(clothing.getViewType())
                    .shelfNo(clothing.getShelfNo())
                    .brand(clothing.getBrand())
                    .size(clothing.getSize())
                    .price(clothing.getPrice())
                    .purchaseDate(clothing.getPurchaseDate())
                    .status(clothing.getStatus())
                    .wearCount(clothing.getWearCount())
                    .createTime(clothing.getCreateTime())
                    .updateTime(clothing.getUpdateTime())
                    .build();
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteClothing(Long id) {
        log.info("收到删除衣物请求: ID={}", id);

        // 1. 验证用户登录状态
        Long userId = UserContext.getUserId();
        if (userId == null) {
            log.error("用户未登录，无法删除衣物");
            throw new BusinessException("用户未登录，请先登录");
        }

        // 2. 验证衣物是否存在且属于当前用户
        Clothing clothing = this.getById(id);
        if (clothing == null) {
            log.error("衣物不存在: ID={}", id);
            throw new BusinessException("衣物不存在");
        }
        if (!clothing.getUserId().equals(userId)) {
            log.error("无权删除该衣物: ID={}, UserId={}, CurrentUserId={}", 
                    id, clothing.getUserId(), userId);
            throw new BusinessException("无权删除该衣物");
        }

        // 3. 手动设置 del_flag = 1（逻辑删除）
        LambdaUpdateWrapper<Clothing> updateWrapper = new LambdaUpdateWrapper<Clothing>()
                .eq(Clothing::getId, id)
                .eq(Clothing::getUserId, userId) // 确保只能删除自己的衣物
                .set(Clothing::getDelFlag, 1);

        boolean deleted = this.update(updateWrapper);
        if (deleted) {
            log.info("衣物删除成功: ID={}, UserId={}", id, userId);
        } else {
            log.error("衣物删除失败: ID={}, UserId={}", id, userId);
            throw new BusinessException("衣物删除失败，请稍后重试");
        }

        return deleted;
    }

    @Override
    public ClothingFilterOptionsVO getFilterOptions() {
        log.info("查询用户衣物筛选选项");

        // 1. 获取当前登录用户ID
        Long userId = UserContext.getUserId();
        if (userId == null) {
            log.error("用户未登录，无法查询筛选选项");
            throw new BusinessException("用户未登录，请先登录");
        }

        // 2. 查询该用户的所有衣物（排除逻辑删除）
        LambdaQueryWrapper<Clothing> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Clothing::getUserId, userId)
                .eq(Clothing::getDelFlag, 0);
        List<Clothing> clothingList = this.list(wrapper);

        log.info("用户衣物总数: {}, UserId={}", clothingList.size(), userId);

        // 3. 使用 Stream API 去重并收集各字段的唯一值
        List<RegionEnum> regions = clothingList.stream()
                .map(Clothing::getRegion)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing(RegionEnum::getCode))
                .collect(Collectors.toList());

        List<String> categories = clothingList.stream()
                .map(Clothing::getCategory)
                .filter(Objects::nonNull)
                .filter(cat -> !cat.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<Integer> layers = clothingList.stream()
                .map(Clothing::getDefaultLayer)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> colors = clothingList.stream()
                .map(Clothing::getColor)
                .filter(Objects::nonNull)
                .filter(color -> !color.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> seasons = clothingList.stream()
                .map(Clothing::getSeason)
                .filter(Objects::nonNull)
                .filter(season -> !season.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<String> fitTypes = clothingList.stream()
                .map(Clothing::getFitType)
                .filter(Objects::nonNull)
                .filter(fitType -> !fitType.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // 4. 构建并返回 VO
        ClothingFilterOptionsVO options = ClothingFilterOptionsVO.builder()
                .regions(regions)
                .categories(categories)
                .layers(layers)
                .colors(colors)
                .seasons(seasons)
                .fitTypes(fitTypes)
                .build();

        log.info("筛选选项查询完成: regions={}, categories={}, layers={}, colors={}, seasons={}, fitTypes={}",
                regions.size(), categories.size(), layers.size(), colors.size(), seasons.size(), fitTypes.size());

        return options;
    }
}