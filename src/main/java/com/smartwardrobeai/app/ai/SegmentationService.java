package com.smartwardrobeai.app.ai;

import com.aliyun.imageseg20191230.models.SegmentClothResponse;
import com.aliyun.imageseg20191230.models.SegmentClothResponseBody;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.smartwardrobeai.common.BusinessException;
import com.smartwardrobeai.config.AliyunConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 抠图服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SegmentationService {
    private final AliyunConfig aliyunConfig;

    /**
     * 根据 文件流 进行衣物去背景
     * <p>
     * //     * @param inputStream 文件流
     *
     * @return 分割后的透明图 URL
     */
    public String segmentByStream(InputStream inputStream) {
        try {
            // 1. 构建请求
            com.aliyun.imageseg20191230.models.SegmentClothAdvanceRequest segmentClothAdvanceRequest = new com.aliyun.imageseg20191230.models.SegmentClothAdvanceRequest().setImageURLObject(inputStream);

            // 2. 运行时配置 (超时设置)
            RuntimeOptions runtime = new RuntimeOptions().setConnectTimeout(aliyunConfig.getImageseg().getConnectTimeout()).setReadTimeout(aliyunConfig.getImageseg().getReadTimeout()).setAutoretry(true)  // 开启自动重试
                    .setMaxAttempts(3);  // 最多重试3次
            log.info("开始调用阿里云分割");

            // 3. 发起调用
            SegmentClothResponse segmentClothAdvanceResponse = aliyunConfig.imageSegClient().segmentClothAdvance(segmentClothAdvanceRequest, runtime);
            if (Objects.isNull(segmentClothAdvanceResponse)) {
                throw new BusinessException("阿里云抠图失败");
            }
            Integer statusCode = segmentClothAdvanceResponse.getStatusCode();
            if (!Objects.equals(statusCode, 200)) {
                throw new BusinessException("阿里云抠图失败");
            }
            SegmentClothResponseBody body = segmentClothAdvanceResponse.getBody();
            SegmentClothResponseBody.SegmentClothResponseBodyData data = body.getData();
            List<SegmentClothResponseBody.SegmentClothResponseBodyDataElements> elements = data.getElements();
            SegmentClothResponseBody.SegmentClothResponseBodyDataElements first = elements.getFirst();
            String imageURL = first.getImageURL();
            return imageURL;
        } catch (TeaException teaException) {
//            String jsonString = Common.toJSONString(teaException);
            if (Objects.nonNull(teaException.getData())) {
                Map<String, Object> data = teaException.getData();
                String message = data.get("Message") == null ? teaException.getMessage() : data.get("Message").toString();
                log.error("抠图失败：{}", message, teaException);
                throw new BusinessException(message);
            } else {
                String message = teaException.getMessage();
                log.error("抠图失败：{}", message, teaException);
                throw new BusinessException(teaException.getMessage());
            }
        } catch (Exception e) {
            log.error("抠图失败：{}", e.getMessage(), e);
            throw new BusinessException(e.getMessage());
        }
    }
}
