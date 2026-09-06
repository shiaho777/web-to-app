# AI Settings

Configures the AI backend used by [Agent](/guide/more-features/agent). Open it from [⋮ → AI Settings](/guide/main-screen/more).

## Provider catalog

The built-in catalog keeps well-known providers only, at most three per category:

- **Recommended** — Google Gemini, OpenRouter.
- **International** — OpenAI, Anthropic, Grok.
- **Aggregator** — Together, Perplexity, Fireworks.
- **Chinese** — DeepSeek, Qwen, GLM.
- **Self-hosted** — Ollama, LM Studio, vLLM.

Anything else fits **Custom**: any provider not in the catalog (or removed by a past update) is handled as a custom endpoint, and legacy configs referencing a removed provider migrate to Custom automatically with their base URL preserved.

## API keys

- **Add** provider API keys, each with an optional **alias**.
- Custom endpoints choose an **API format** — Chat Completions (`/chat/completions`), Anthropic Messages (`/v1/messages`), OpenAI Responses (`/responses`), or Google Gemini — and the gateway routes the request by the declared format instead of assuming OpenAI-compat. The **chat endpoint** can be overridden per key.
- **Connection test** — verify a key works (connection OK / fail).
- Keys are stored securely on-device.

## Saved models

- **Add models** individually or **batch-select** from the available list.
- Configure **model capabilities** and what each model is **available for**.
- A pricing reference helps you pick models.

## Advanced

- **Context capacity** — the context window size used for Agent sessions.

## Notes

Agent won't work until at least one valid API key and model are configured here.
