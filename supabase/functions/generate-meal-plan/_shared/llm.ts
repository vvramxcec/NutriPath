/**
 * Provider-agnostic LLM dispatch for the edge function.
 *
 * Default provider is Gemini (LLM_PROVIDER=gemini, GEMINI_API_KEY). Set
 * LLM_PROVIDER=anthropic to use Claude instead (ANTHROPIC_API_KEY). Both
 * return the same shape, so the orchestration is provider-independent.
 */

import { callGemini } from "./gemini.ts";
import { callClaude } from "./anthropic.ts";

export interface LlmCall {
  apiKey: string;
  model: string;
  system: string;
  userMessage: string;
  schema: Record<string, unknown>;
  maxTokens?: number;
}

export interface LlmResult {
  text: string;
  model: string;
  stopReason: string | null;
  inputTokens: number;
  outputTokens: number;
}

export type LlmProvider = "gemini" | "anthropic";

const PROVIDER_MODELS: Record<LlmProvider, string> = {
  gemini: "gemini-2.5-flash",
  anthropic: "claude-opus-5",
};

export function resolveProvider(): LlmProvider {
  const p = (Deno.env.get("LLM_PROVIDER") ?? "gemini").toLowerCase();
  if (p === "anthropic" || p === "claude") return "anthropic";
  return "gemini";
}

/** Resolve the provider's API key + model and call the LLM. */
export async function callLlm(
  call: Omit<LlmCall, "apiKey" | "model">,
): Promise<LlmResult> {
  const provider = resolveProvider();
  const keyName = provider === "anthropic" ? "ANTHROPIC_API_KEY" : "GEMINI_API_KEY";
  const modelName = provider === "anthropic" ? "ANTHROPIC_MODEL" : "GEMINI_MODEL";

  // Trim: keys pasted via shell frequently carry a trailing newline, which the
  // API rejects as invalid.
  const apiKey = (Deno.env.get(keyName) ?? "").trim();
  if (!apiKey) throw new Error(`${keyName} not configured`);

  const model = Deno.env.get(modelName) ?? PROVIDER_MODELS[provider];
  const full: LlmCall = { ...call, apiKey, model };

  return provider === "anthropic" ? await callClaude(full) : await callGemini(full);
}
