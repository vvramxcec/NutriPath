/**
 * Gemini API caller — raw fetch() to the generativelanguage v1beta endpoint.
 * Structured output via responseMimeType: application/json + responseSchema.
 */

import type { LlmCall, LlmResult } from "./llm.ts";

export async function callGemini(call: LlmCall): Promise<LlmResult> {
  const res = await fetch(
    `https://generativelanguage.googleapis.com/v1beta/models/${call.model}:generateContent`,
    {
      method: "POST",
      headers: {
        "content-type": "application/json",
        "x-goog-api-key": call.apiKey,
      },
      body: JSON.stringify({
        system_instruction: { parts: [{ text: call.system }] },
        contents: [{ role: "user", parts: [{ text: call.userMessage }] }],
        generationConfig: {
          maxOutputTokens: call.maxTokens ?? 8000,
          responseMimeType: "application/json",
          responseSchema: call.schema,
        },
      }),
    },
  );

  const raw = await res.text();
  if (!res.ok) {
    throw new Error(`Gemini API ${res.status}: ${raw.slice(0, 500)}`);
  }

  const data = JSON.parse(raw);
  const parts: { text?: string }[] = data.candidates?.[0]?.content?.parts ?? [];
  const text = parts.find((p) => typeof p.text === "string")?.text;
  if (!text) {
    const reason = data.candidates?.[0]?.finishReason ?? "unknown";
    throw new Error(`Gemini response contained no text (finish_reason: ${reason})`);
  }

  return {
    // Defensively strip any markdown code fences Gemini might wrap the JSON in.
    text: text.trim().replace(/^```(?:json)?\s*|\s*```$/g, ""),
    model: call.model,
    stopReason: data.candidates?.[0]?.finishReason ?? null,
    inputTokens: data.usageMetadata?.promptTokenCount ?? 0,
    outputTokens: data.usageMetadata?.candidatesTokenCount ?? 0,
  };
}
