# AI 设置

配置 [Agent](/zh/guide/more-features/agent)所用的 AI 后端。从 [⋮ → AI 设置](/zh/guide/main-screen/more) 打开。

## 提供商目录

内置目录只保留知名提供商,每类至多三个:

- **推荐** —— Google Gemini、OpenRouter。
- **国际** —— OpenAI、Anthropic、Grok。
- **聚合** —— Together、Perplexity、Fireworks。
- **中国** —— DeepSeek、Qwen、GLM。
- **自托管** —— Ollama、LM Studio、vLLM。

其他一律走 **自定义**:目录之外的提供商(或历史版本移除的提供商)都按自定义端点处理;引用已移除提供商的旧配置会自动迁移为自定义,并保留原 base URL。

## API 密钥

- **添加** 提供商 API 密钥,每个可带一个 **别名**。
- 自定义端点可选 **API 格式** —— Chat Completions(`/chat/completions`)、Anthropic Messages(`/v1/messages`)、OpenAI Responses(`/responses`)或 Google Gemini —— 网关按声明的格式路由请求,而不是一律走 OpenAI 兼容路径。**聊天端点**可按密钥覆盖。
- **连接测试** —— 验证密钥是否可用(连接成功 / 失败)。
- 密钥在设备上安全存储。

## 已保存模型

- **添加模型**,可单个添加或从可用列表中 **批量选择**。
- 配置 **模型能力** 和每个模型的 **适用范围**。
- 价格参考帮助你挑选模型。

## 高级

- **上下文容量** —— Agent会话使用的上下文窗口大小。

## 说明

在配置好至少一个有效的 API 密钥和模型之前,Agent无法工作。
