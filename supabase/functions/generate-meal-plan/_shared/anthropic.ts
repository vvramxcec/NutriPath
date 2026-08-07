/**
 * Claude API caller — raw fetch() to POST /v1/messages (no official Deno SDK).
 * Structured output via output_config.format.json_schema. Used when
 * LLM_PROVIDER=anthropic; otherwise the default is Gemini (see llm.ts).
 */

import type { LlmCall, LlmResult } from "./llm.ts";

export async function callClaude(call: LlmCall): Promise<LlmResult> {
  const res = await fetch("https://api.anthropic.com/v1/messages", {
    method: "POST",
    headers: {
      "content-type": "application/json",
      "x-api-key": call.apiKey,
      "anthropic-version": "2023-06-01",
    },
    body: JSON.stringify({
      model: call.model,
      max_tokens: call.maxTokens ?? 8000,
      system: call.system,
      messages: [{ role: "user", content: call.userMessage }],
      output_config: {
        format: { type: "json_schema", schema: call.schema },
      },
    }),
  });

  const raw = await res.text();
  if (!res.ok) {
    throw new Error(`Anthropic API ${res.status}: ${raw.slice(0, 500)}`);
  }

  const data = JSON.parse(raw);
  const textBlock = data.content?.find((b: { type: string; text?: string }) => b.type === "text");
  if (!textBlock?.text) {
    throw new Error("Claude response contained no text block (stop_reason: " + (data.stop_reason ?? "unknown") + ")");
  }

  return {
    text: textBlock.text,
    model: data.model ?? call.model,
    stopReason: data.stop_reason ?? null,
    inputTokens: data.usage?.input_tokens ?? 0,
    outputTokens: data.usage?.output_tokens ?? 0,
  };
}
