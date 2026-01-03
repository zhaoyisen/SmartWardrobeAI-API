package com.smartwardrobeai.service;

import com.smartwardrobeai.common.BusinessException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {

    // 正则常量
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    // Redis Key 前缀
    private static final String SMS_PREFIX = "auth:sms:";
    private static final String EMAIL_PREFIX = "auth:email:";
    // 验证码有效期 5 分钟
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender mailSender;
    // 从配置文件读取发送者账号，避免硬编码
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送验证码
     */
    public void sendCode(String target, String type) {
        //校验格式
        validateFormat(target, type);

        // 决定 Redis Key
        String keyPrefix = StringUtils.equalsIgnoreCase(type, "email") ? EMAIL_PREFIX : SMS_PREFIX;
        String key = keyPrefix + target;

        // 2. 频率限制 (防刷)：如果 Key 存在且过期时间大于 4分30秒 (代表刚发了不到30秒)，则拦截
        // 这里设为 60秒 限制更合理
        Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        //todo 5改成配置文件
        if (expire != null && expire > (Duration.ofMinutes(5).getSeconds() - 60)) {
            // 如果离上次发送还没超过 60 秒 (假设有效期5分钟，剩下4分钟以上说明刚发不久)
            throw new BusinessException("请求太频繁，请稍后再试");
        }

        // 3. 生成 6 位随机验证码
        String code = String.valueOf(new Random().nextInt(899999) + 100000);


        // 4. 存入 Redis (有效期 5 分钟)
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
        log.info("====> 验证码存储完毕 Key: {}", key);
//        redisTemplate.opsForValue().set(key, code, CODE_TTL);

        // 4. TODO: 调用第三方 SDK 发送 (此处仅打印模拟)
        // 5. 发送逻辑 (区分渠道)
        if (StringUtils.equalsIgnoreCase(type, "email")) {
            sendRealEmail(target, code);
        } else {
            sendSms(target, code);
        }
    }


    /**
     * 发送真实邮件 (支持 HTML)
     */
    private void sendRealEmail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("【智能衣橱AI】您的登录验证码");

            // HTML 邮件内容
            String content = String.format("""
                    <div style="padding: 20px; background-color: #f5f5f5;">
                        <div style="background-color: white; padding: 20px; border-radius: 5px;">
                            <h2 style="color: #333;">登录验证</h2>
                            <p>您好！您的验证码是：</p>
                            <h1 style="color: #007BFF; font-size: 32px;">%s</h1>
                            <p>验证码 5 分钟内有效，请勿泄露给他人。</p>
                            <p style="color: #999; font-size: 12px;">如果这不是您的操作，请忽略此邮件。</p>
                        </div>
                    </div>
                    """, code);

            helper.setText(content, true); // true 表示支持 HTML
            mailSender.send(message);

            log.info("====> 邮件已发送至: {}", to);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            throw new BusinessException("邮件发送失败，请检查邮箱是否正确");
        }
    }

    /**
     * 发送短信 (目前是模拟，未来接入阿里云)
     */
    private void sendSms(String phone, String code) {
        // TODO: 这里接入阿里云/腾讯云 SMS SDK
        // 目前仅打印到控制台
        log.info("\n\n=========================================");
        log.info("【模拟短信网关】");
        log.info(" 目标手机: {}", phone);
        log.info(" 短信内容: 【智能衣橱】您的验证码是 {}，5分钟内有效。", code);
        log.info("=========================================\n");
    }


    /**
     * 格式校验逻辑
     */
    private void validateFormat(String target, String type) {
        if (StringUtils.equalsIgnoreCase(type, "email")) {
            if (!Pattern.matches(EMAIL_REGEX, target)) {
                throw new BusinessException("邮箱格式不正确");
            }
        } else if (StringUtils.equalsIgnoreCase(type, "sms")) {
            if (!Pattern.matches(PHONE_REGEX, target)) {
                throw new BusinessException("手机号格式不正确");
            }
        } else {
            throw new BusinessException("不支持的验证方式: " + type);
        }
    }


    /**
     * 校验验证码 (校验成功后立即删除，防止重复使用)
     */
    public boolean verifyCode(String target, String type, String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }

        String keyPrefix = StringUtils.equalsIgnoreCase(type, "email") ? EMAIL_PREFIX : SMS_PREFIX;
        String key = keyPrefix + target;

        Object value = redisTemplate.opsForValue().get(key);
        String cachedCode = value != null ? value.toString() : null;

        if (cachedCode == null) {
            throw new BusinessException("验证码已过期或未发送");
        }
        if (!cachedCode.equalsIgnoreCase(code)) {
            throw new BusinessException("验证码错误");
        }

        // 验证通过，删除缓存
        return redisTemplate.delete(key);


    }
}