package com.smartwardrobeai.app.service;

import com.aliyun.credentials.Client;
import com.aliyun.teautil.models.RuntimeOptions;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
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
    @Value("${aliyun.sms.access-key-id:}")
    private String aliyunAccessKeyId;

    @Value("${aliyun.sms.access-key-secret:}")
    private String aliyunAccessKeySecret;

    @Value("${aliyun.sms.sign-name:}")
    private String aliyunSignName;

    @Value("${aliyun.sms.template-code:}")
    private String aliyunTemplateCode;

    @Value("${aliyun.sms.scheme-name:}")
    private String aliyunSchemeName;

    @Value("${aliyun.sms.country-code:86}")
    private String aliyunCountryCode;

    @Value("${aliyun.sms.endpoint:dypnsapi.aliyuncs.com}")
    private String aliyunEndpoint;

    // 阿里云客户端缓存（懒加载单例，使用volatile确保线程安全）
    private volatile com.aliyun.dypnsapi20170525.Client aliyunSmsClient;

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
     * 客户端使用懒加载单例模式，第一次使用时创建并缓存
     */
    private void sendByAliyun(String phone, String code) {
        if (aliyunSchemeName == null || aliyunSchemeName.isEmpty()) {
            throw new BusinessException("阿里云方案名称(scheme-name)未配置");
        }

        try {
            // 获取客户端（懒加载单例，第一次使用时创建）
            com.aliyun.dypnsapi20170525.Client client = getAliyunClient();

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
                client.sendSmsVerifyCodeWithOptions(request, runtime);

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
     * 获取阿里云号码认证服务客户端（懒加载单例模式）
     * 使用双重检查锁定（Double-Checked Locking）确保线程安全
     * 参考用户提供的SDK示例实现
     */
    private com.aliyun.dypnsapi20170525.Client getAliyunClient() throws Exception {
        // 第一次检查（避免不必要的同步）
        if (aliyunSmsClient == null) {
            synchronized (this) {
                // 第二次检查（确保只创建一个实例）
                if (aliyunSmsClient == null) {
                    // 使用凭据初始化（支持环境变量、配置文件等多种方式）
                    Client credential = new Client();
                    
                    // 创建Config对象
                    Config config = new Config()
                            .setCredential(credential);
                    
                    // 如果提供了AccessKeyId和AccessKeySecret，直接在Config上设置
                    if (aliyunAccessKeyId != null && !aliyunAccessKeyId.isEmpty() && 
                        aliyunAccessKeySecret != null && !aliyunAccessKeySecret.isEmpty()) {
                        config.setAccessKeyId(aliyunAccessKeyId)
                              .setAccessKeySecret(aliyunAccessKeySecret);
                    }
                    
                    // Endpoint 设置为号码认证服务端点，请参考 https://api.aliyun.com/product/Dypnsapi
                    config.setEndpoint(aliyunEndpoint);
                    
                    // 创建客户端实例并赋值给volatile字段（保证可见性）
                    aliyunSmsClient = new com.aliyun.dypnsapi20170525.Client(config);
                    log.info("阿里云号码认证服务客户端已创建");
                }
            }
        }
        return aliyunSmsClient;
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

