package org.kiteseven.kiteuniverse.support.ai;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * 轻量级 DeepSeek 客户端，通过 OpenAI 兼容的 /v1/chat/completions 端点发起单轮对话请求。
 * 不依赖 Spring AI，直接使用 RestTemplate 避免版本兼容问题。
 */
public class DeepSeekClient {

    private final RestTemplate restTemplate;
    private final String chatUrl;
    private final String apiKey;
    private final String model;

    public DeepSeekClient(String baseUrl, String apiKey, String model) {
        this.restTemplate = new RestTemplate();
        this.chatUrl = baseUrl.replaceAll("/+$", "") + "/v1/chat/completions";
        this.apiKey = apiKey;
        this.model = model;
    }

    /**
     * 发送单条用户消息，返回模型的文本回复。
     *
     * @param userMessage 用户消息内容
     * @return 模型回复文本
     */
    @SuppressWarnings("unchecked")
    public String chat(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", userMessage)),
                "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        Map<String, Object> response = restTemplate.postForObject(chatUrl, request, Map.class);

        if (response == null) {
            throw new RuntimeException("AI 服务无响应");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("AI 响应格式异常：choices 为空");
        }

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) {
            throw new RuntimeException("AI 响应格式异常：message 为空");
        }

        Object content = message.get("content");
        return content != null ? content.toString() : "";
    }
}
