/** Shared CORS headers for the edge function. */
export const corsHeaders: Record<string, string> = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "POST, GET, OPTIONS",
};

/** Response for an OPTIONS preflight. */
export function corsResponse(): Response {
  return new Response("ok", { headers: corsHeaders });
}
