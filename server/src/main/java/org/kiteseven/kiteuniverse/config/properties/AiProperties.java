package org.kiteseven.kiteuniverse.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 绑定 AI 服务配置（兼容 OpenAI 协议的 DeepSeek 端点）。
 */
@ConfigurationProperties(prefix = "kite-universe.ai.openai")
public class AiProperties {

    /**
     * API 密钥。
     */
    private String apiKey;

    /**
     * 服务端点。
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * 聊天选项（包含模型名称）。
     */
    private ChatConfig chat = new ChatConfig();

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ChatConfig getChat() {
        return chat;
    }

    public void setChat(ChatConfig chat) {
        this.chat = chat;
    }

    /**
     * 返回实际使用的模型名称（从 chat.options.model 读取，默认 deepseek-chat）。
     */
    public String getModel() {
        if (chat != null && chat.getOptions() != null && chat.getOptions().getModel() != null) {
            return chat.getOptions().getModel();
        }
        return "deepseek-chat";
    }

    public static class ChatConfig {
        private ChatOptions options = new ChatOptions();

        public ChatOptions getOptions() {
            return options;
        }

        public void setOptions(ChatOptions options) {
            this.options = options;
        }
    }

    public static class ChatOptions {
        private String model = "deepseek-chat";

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }
}
