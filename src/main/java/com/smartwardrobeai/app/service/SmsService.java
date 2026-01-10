package com.smartwardrobeai.app.service;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.teautil.models.RuntimeOptions;
import com.aliyun.tea.TeaException;
import com.smartwardrobeai.common.BusinessException;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Autowired(required = false)
    @Qualifier("smsClient")
    private SmsClient tencentSmsClient;

    @Autowired(required = false)
    @Qualifier("aliyunSmsClient")
    private com.aliyun.dypnsapi20170525.Client aliyunSmsClient;

    @Value("${sms.provider:aliyun}")
    private String provider; // 短信服务商：aliyun 或 tencent

    // 腾讯云配置
    @Value("${tencent.sms.app-id:}")
    private String tencentAppId;

    @Value("${tencent.sms.sign-name:}")
    private String tencentSignName;

    @Value("${tencent.sms.template-id:}")
    private String tencentTemplateId;

    // 阿里云号码认证服务配置
    @Value("${aliyun.sms.sign-name:}")
    private String aliyunSignName;

    @Value("${aliyun.sms.template-code:}")
    private String aliyunTemplateCode;

    @Value("${aliyun.sms.scheme-name:}")
    private String aliyunSchemeName;

    @Value("${aliyun.sms.country-code:86}")
    private String aliyunCountryCode;

    /**
     * 发送验证码短信
     *
     * @param phone 手机号
     * @param code  验证码
     */
    public void sendVerificationCode(String phone, String code) {
        if ("aliyun".equalsIgnoreCase(provider)) {
            sendByAliyun(phone, code);
        } else if ("tencent".equalsIgnoreCase(provider)) {
            sendByTencent(phone, code);
        } else {
            throw new BusinessException("不支持的短信服务商: " + provider);
        }
    }

    /**
     * 使用阿里云号码认证服务发送验证码短信
     */
    private void sendByAliyun(String phone, String code) {
        if (aliyunSmsClient == null) {
            throw new BusinessException("阿里云号码认证服务客户端未配置");
        }

        if (aliyunSchemeName == null || aliyunSchemeName.isEmpty()) {
            throw new BusinessException("阿里云方案名称(scheme-name)未配置");
        }

        try {
            // 构建发送短信验证码请求
            com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest request = 
                new com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest()
                    .setSchemeName(aliyunSchemeName)
                    .setCountryCode(aliyunCountryCode)
                    .setPhoneNumber(phone)
                    .setSignName(aliyunSignName)
                    .setTemplateCode(aliyunTemplateCode)
                    .setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"5\"}");

            RuntimeOptions runtime = new RuntimeOptions();
            com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse response = 
                aliyunSmsClient.sendSmsVerifyCodeWithOptions(request, runtime);

            if (response != null && response.getBody() != null) {
                com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody body = response.getBody();
                if (body.getCode() != null && "OK".equals(body.getCode())) {
                    log.info("阿里云号码认证服务短信发送成功 - 手机号: {}, 验证码: {}", phone, code);
                } else {
                    String errorMsg = body.getMessage() != null ? body.getMessage() : "未知错误";
                    log.error("阿里云号码认证服务短信发送失败 - 手机号: {}, 错误码: {}, 错误信息: {}", 
                        phone, body.getCode(), errorMsg);
                    throw new BusinessException("短信发送失败: " + errorMsg);
                }
            } else {
                log.error("阿里云号码认证服务响应为空 - 手机号: {}", phone);
                throw new BusinessException("短信发送失败，响应为空");
            }
        } catch (TeaException error) {
            // 处理 TeaException 异常
            String errorMsg = error.getMessage() != null ? error.getMessage() : "未知错误";
            log.error("阿里云号码认证服务异常 - 手机号: {}, 错误: {}", phone, errorMsg, error);
            
            // 获取诊断地址（如果有）
            Object recommend = error.getData() != null ? error.getData().get("Recommend") : null;
            if (recommend != null) {
                log.error("诊断地址: {}", recommend);
            }
            
            throw new BusinessException("短信发送失败: " + errorMsg);
        } catch (Exception e) {
            log.error("阿里云号码认证服务异常 - 手机号: {}, 错误: {}", phone, e.getMessage(), e);
            TeaException error = new TeaException(e.getMessage(), e);
            Object recommend = error.getData() != null ? error.getData().get("Recommend") : null;
            if (recommend != null) {
                log.error("诊断地址: {}", recommend);
            }
            throw new BusinessException("短信发送失败: " + e.getMessage());
        }
    }

    /**
     * 使用腾讯云发送短信
     */
    private void sendByTencent(String phone, String code) {
        if (tencentSmsClient == null) {
            throw new BusinessException("腾讯云短信客户端未配置");
        }

        try {
            // 实例化一个请求对象
            SendSmsRequest req = new SendSmsRequest();

            // 设置短信应用 ID
            req.setSmsSdkAppId(tencentAppId);

            // 设置签名内容
            req.setSignName(tencentSignName);

            // 设置模板 ID
            req.setTemplateId(tencentTemplateId);

            // 设置模板参数，验证码模板通常只有一个参数 {1} 或 {code}
            // 腾讯云短信模板参数格式为数组，第一个参数对应模板中的 {1}
            String[] templateParamSet = {code};
            req.setTemplateParamSet(templateParamSet);

            // 设置手机号数组，支持批量发送
            String[] phoneNumberSet = {"+86" + phone};
            req.setPhoneNumberSet(phoneNumberSet);

            // 发送短信
            SendSmsResponse resp = tencentSmsClient.SendSms(req);

            // 检查发送结果
            if (resp.getSendStatusSet() != null && resp.getSendStatusSet().length > 0) {
                String sendStatus = resp.getSendStatusSet()[0].getCode();
                String message = resp.getSendStatusSet()[0].getMessage();

                if ("Ok".equals(sendStatus)) {
                    log.info("腾讯云短信发送成功 - 手机号: {}, 验证码: {}", phone, code);
                } else {
                    log.error("腾讯云短信发送失败 - 手机号: {}, 错误码: {}, 错误信息: {}", phone, sendStatus, message);
                    throw new BusinessException("短信发送失败: " + message);
                }
            } else {
                log.error("腾讯云短信发送失败 - 手机号: {}, 响应为空", phone);
                throw new BusinessException("短信发送失败，请稍后重试");
            }

        } catch (TencentCloudSDKException e) {
            log.error("腾讯云短信服务异常 - 手机号: {}, 错误: {}", phone, e.getMessage(), e);
            throw new BusinessException("短信发送失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("腾讯云短信发送异常 - 手机号: {}", phone, e);
            throw new BusinessException("短信发送失败，请稍后重试");
        }
    }
}

