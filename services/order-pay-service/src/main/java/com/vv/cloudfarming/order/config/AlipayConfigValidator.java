package com.vv.cloudfarming.order.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝配置校验，避免把应用公钥误填到支付宝公钥字段。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayConfigValidator implements InitializingBean {

    private final AlipayTemplate alipayTemplate;

    @Override
    public void afterPropertiesSet() {
        boolean hasAnyConfig = hasText(alipayTemplate.getAppId())
                || hasText(alipayTemplate.getPrivateKey())
                || hasText(alipayTemplate.getAlipayPublicKey())
                || hasText(alipayTemplate.getGatewayUrl())
                || hasText(alipayTemplate.getNotifyUrl());
        if (!hasAnyConfig) {
            log.warn("未检测到支付宝配置，支付能力当前不可用");
            return;
        }

        Map<String, String> missingFields = new LinkedHashMap<>();
        collectMissing(missingFields, "appId", alipayTemplate.getAppId());
        collectMissing(missingFields, "privateKey", alipayTemplate.getPrivateKey());
        collectMissing(missingFields, "alipayPublicKey", alipayTemplate.getAlipayPublicKey());
        collectMissing(missingFields, "gatewayUrl", alipayTemplate.getGatewayUrl());
        collectMissing(missingFields, "notifyUrl", alipayTemplate.getNotifyUrl());
        if (!missingFields.isEmpty()) {
            throw new IllegalStateException("支付宝配置不完整，缺少字段：" + String.join(", ", missingFields.keySet()));
        }

        String derivedAppPublicKey = deriveAppPublicKey(alipayTemplate.getPrivateKey());
        String configuredAlipayPublicKey = normalizeKey(alipayTemplate.getAlipayPublicKey());
        if (configuredAlipayPublicKey.equals(derivedAppPublicKey)) {
            throw new IllegalStateException("支付宝配置错误：alipayPublicKey 当前填的是应用公钥，请替换为支付宝平台公钥");
        }

        log.info("支付宝配置校验通过，appId={}, notifyUrl={}", alipayTemplate.getAppId(), alipayTemplate.getNotifyUrl());
    }

    private void collectMissing(Map<String, String> missingFields, String fieldName, String value) {
        if (!hasText(value)) {
            missingFields.put(fieldName, fieldName);
        }
    }

    private boolean hasText(String value) {
        return StringUtils.hasText(value);
    }

    private String deriveAppPublicKey(String privateKey) {
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(normalizeKey(privateKey));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey generatedPrivateKey = keyFactory.generatePrivate(keySpec);
            if (!(generatedPrivateKey instanceof RSAPrivateCrtKey rsaPrivateKey)) {
                throw new IllegalStateException("商户私钥格式非法，无法推导应用公钥");
            }
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
                    rsaPrivateKey.getModulus(),
                    rsaPrivateKey.getPublicExponent()
            );
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            return Base64.getEncoder().encodeToString(publicKey.getEncoded());
        } catch (Exception ex) {
            throw new IllegalStateException("支付宝私钥解析失败，请检查 privateKey 配置是否正确", ex);
        }
    }

    private String normalizeKey(String key) {
        return key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
    }
}
