/**
 * Minimal PostgREST client using the service-role key (bypasses RLS).
 * Raw fetch — avoids a supabase-js dependency in the Deno edge runtime.
 */

const SUPABASE_URL = Deno.env.get("SUPABASE_URL") ?? "";
const SERVICE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";

if (!SUPABASE_URL || !SERVICE_KEY) {
  console.warn("SUPABASE_URL / SUPABASE_SERVICE_ROLE_KEY not set — DB calls will fail.");
}

function headers(json = false): Record<string, string> {
  const h: Record<string, string> = {
    apikey: SERVICE_KEY,
    Authorization: `Bearer ${SERVICE_KEY}`,
  };
  if (json) h["Content-Type"] = "application/json";
  return h;
}

async function resBody(res: Response): Promise<string> {
  const text = await res.text();
  if (!res.ok) throw new Error(`POSTGREST ${res.status}: ${text.slice(0, 500)}`);
  return text;
}

export async function getJson<T>(path: string): Promise<T> {
  const res = await fetch(`${SUPABASE_URL}${path}`, { headers: headers() });
  const text = await resBody(res);
  return text ? (JSON.parse(text) as T) : ([] as unknown as T);
}

export async function postJson<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${SUPABASE_URL}${path}`, {
    method: "POST",
    headers: { ...headers(true), Prefer: "return=representation" },
    body: JSON.stringify(body),
  });
  const text = await resBody(res);
  return text ? (JSON.parse(text) as T) : ([] as unknown as T);
}

export async function patchJson<T>(path: string, body: unknown): Promise<T | null> {
  const res = await fetch(`${SUPABASE_URL}${path}`, {
    method: "PATCH",
    headers: { ...headers(true), Prefer: "return=minimal" },
    body: JSON.stringify(body),
  });
  const text = await resBody(res);
  return text ? (JSON.parse(text) as T) : null;
}

export const REST = (table: string, query = ""): string => `/rest/v1/${table}?${query}`;

/** URL-encode a value in a PostgREST filter. */
export function enc(v: string | number): string {
  return encodeURIComponent(String(v));
}
