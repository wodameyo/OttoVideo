package com.otto.user.service.impl;

import com.otto.common.exception.BusinessException;
import com.otto.common.utils.StringTools;
import com.otto.user.redis.UserRedisComponent;
import com.otto.user.service.EmailCodeService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailCodeServiceImpl implements EmailCodeService {

    private static final Logger logger = LoggerFactory.getLogger(EmailCodeServiceImpl.class);

    @Resource
    private UserRedisComponent redisComponent;

    /**
     * 未配置 spring.mail.host 时为 null（开发环境验证码只打印日志）
     */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Override
    public void sendEmailCode(String email, Integer type, String checkCodeKey, String checkCode) {
        if (!StringTools.isEmail(email)) {
            throw new BusinessException("邮箱格式不正确");
        }
        if (!redisComponent.checkCheckCode(checkCodeKey, checkCode)) {
            throw new BusinessException("图形验证码错误");
        }
        String code = StringTools.getRandomNumber(6);
        redisComponent.saveEmailCode(email, type == null ? 0 : type, code);

        if (mailSender == null || StringTools.isEmpty(mailFrom)) {
            // 本地开发：未配置邮件服务器，验证码直接打印到日志
            logger.warn("【OttoVideo】未配置邮件服务器，邮箱 {} 的验证码为：{}", email, code);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("OttoVideo 邮箱验证码");
            message.setText("您的验证码是：" + code + "，15 分钟内有效。");
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("发送邮件失败，邮箱 {} 验证码：{}", email, code, e);
            throw new BusinessException("邮件发送失败，请稍后重试");
        }
    }
}
