#!/usr/bin/env bash
# NutriPath — end-to-end smoke test for the deployed `generate-meal-plan`
# function. Seeds a throwaway profile + a condition, invokes the function with
# the anon key, asserts the returned plan obeys the rules, then cleans up.
#
# Needs ONLY the anon (publishable) key — demo-mode RLS lets anon insert/read
# everything, so the service_role key is never required or printed.
#
# Usage:
#   PROJECT_REF=<ref> ANON_KEY=<anon> ./tools/smoke_test.sh
#   (or set both in supabase/.env.local — gitignored — and just run the script)
set -uo pipefail

# --- resolve config -----------------------------------------------------------
if [ -f supabase/.env.local ]; then
  # shellcheck disable=SC1091
  . supabase/.env.local
fi
REF="${PROJECT_REF:-}"
ANON="${ANON_KEY:-}"
if [ -z "$REF" ] || [ -z "$ANON" ]; then
  echo "Missing PROJECT_REF / ANON_KEY."
  echo "  Run:  PROJECT_REF=<ref> ANON_KEY=<anon> $0"
  echo "  or set them in supabase/.env.local"
  exit 2
fi

REST="https://$REF.supabase.co/rest/v1"
FN="https://$REF.functions.supabase.co/generate-meal-plan"

step()   { printf '\n==> %s\n' "$*"; }
pass()   { printf '  [PASS] %s\n' "$*"; }
fail()   { printf '  [FAIL] %s\n' "$*"; }
info()   { printf '  [INFO] %s\n' "$*"; }

# JSON helpers (python3, no jq dependency)
json_field() { python3 -c 'import sys,json; print(json.load(sys.stdin).get(sys.argv[1]))' "$1"; }

curl_api() { curl -sS --max-time 90 -w '\n%{http_code}' \
  -H "apikey: $ANON" -H "Authorization: Bearer $ANON" "$@"; }

# --- 1. seed a throwaway profile ----------------------------------------------
step "Seeding a throwaway profile (type2_diabetes)…"
PID="$(python3 -c 'import uuid; print(uuid.uuid4())')"

BODY=$(curl_api -X POST "$REST/profiles" \
  -H 'Content-Type: application/json' -H 'Prefer: return=representation' \
  -d "{\"id\":\"$PID\",\"display_name\":\"Smoke Test\",\"age\":45,\"sex\":\"male\",\"height_cm\":175,\"weight_kg\":82,\"activity_level\":\"moderate\"}")
CODE=${BODY##*$'\n'}
if [ "$CODE" != "201" ]; then
  echo "${BODY%$'\n'*}" | head -5
  fail "profile insert failed (HTTP $CODE)"
  exit 1
fi
pass "profile $PID created"

COND_ID=$(curl_api "$REST/conditions?select=id&slug=eq.type2_diabetes" \
  | sed '$d' | python3 -c 'import sys,json; print(json.load(sys.stdin)[0]["id"])' 2>/dev/null)
if [ -z "$COND_ID" ]; then
  fail "could not fetch a condition id (is the 0002 seed applied?)"
  exit 1
fi
curl_api -X POST "$REST/profile_conditions" \
  -H 'Content-Type: application/json' \
  -d "{\"profile_id\":\"$PID\",\"condition_id\":\"$COND_ID\"}" >/dev/null
pass "linked condition type2_diabetes ($COND_ID)"

# --- 2. invoke the edge function ----------------------------------------------
step "Invoking generate-meal-plan (anon key, days=3)…"
RESP=$(curl_api -X POST "$FN" -H 'Content-Type: application/json' \
  -d "{\"profileId\":\"$PID\",\"days\":3}")
CODE=${RESP##*$'\n'}
BODY=${RESP%$'\n'*}
if [ "$CODE" != "200" ]; then
  echo "$BODY" | head -8
  case "$CODE" in
    401|403) fail "auth rejected (HTTP $CODE) — is ANON_KEY the project's anon key?";;
    408|504) fail "function timed out (HTTP $CODE) — retry, or the Claude call is slow";;
    404)     fail "function not found (HTTP $CODE) — deploy with: supabase functions deploy generate-meal-plan";;
    500)     fail "function error (HTTP $CODE) — message above; check: supabase functions logs generate-meal-plan";;
    *)       fail "unexpected HTTP $CODE";;
  esac
  exit 1
fi
pass "function returned HTTP 200"

# --- 3. validate the response -------------------------------------------------
step "Checking the plan against the rules…"
PLAN_JSON=$(python3 - "$BODY" <<'PY'
import json, sys
data = json.loads(sys.argv[1])
rules = data.get("rules", {})
plan = data.get("plan", {})
days = plan.get("days", [])
items = [(d["day"], m["meal_type"], i["food_name"]) for d in days for m in d["meals"] for i in m["items"]]
print(json.dumps({
    "model": data.get("model"),
    "condition_count": len(rules.get("conditions", [])),
    "day_count": len(days),
    "item_count": len(items),
    "avoid_count": rules.get("avoidCount", 0),
    "pool_count": rules.get("poolCount", 0),
    "safety_safe": data.get("safety", {}).get("safe", False),
    "summary": plan.get("summary", ""),
}, default=str))
PY
)
echo "$PLAN_JSON" | python3 -c 'import sys,json; d=json.load(sys.stdin); [print(f"  {k}: {v}") for k,v in d.items()]'

STEP_FAIL=0
[ "$(echo "$PLAN_JSON" | json_field model)" != "None" ] || { fail "no model in response"; STEP_FAIL=1; }
[ "$(echo "$PLAN_JSON" | json_field day_count)" -ge 1 ] 2>/dev/null || { fail "plan has no days"; STEP_FAIL=1; }
[ "$(echo "$PLAN_JSON" | json_field safety_safe)" = "True" ] || { fail "safety gate did not pass"; STEP_FAIL=1; }

# no item may reference an avoid-set food
AVOID_CHECK=$(python3 - "$BODY" <<'PY'
import json, sys
data = json.loads(sys.argv[1])
plan = data.get("plan", {})
rules = data.get("rules", {})
avoid = set()
for f in rules.get("avoid", []):
    if isinstance(f, dict):
        avoid.add(f.get("name", "").strip().lower())
    elif isinstance(f, str):
        avoid.add(f.strip().lower())
used = [(d["day"], m["meal_type"], i["food_name"]) for d in plan.get("days", [])
        for m in d["meals"] for i in m["items"]]
bad = [n for _, _, n in used if n.strip().lower() in avoid]
print("\n".join(bad))
PY
)
if [ -n "$AVOID_CHECK" ]; then
  fail "plan references avoid foods: $(echo "$AVOID_CHECK" | tr '\n' ', ')"
  STEP_FAIL=1
else
  pass "no avoid-set food appears in the plan"
fi

# --- 4. verify persistence ------------------------------------------------------
step "Verifying rows persisted…"
PLAN_STATUS=$(curl_api "$REST/meal_plans?select=id,status&profile_id=eq.$PID" | sed '$d')
[ "$PLAN_STATUS" != "[]" ] && echo "$PLAN_STATUS" | grep -q '"success"' && pass "meal_plans row = success" \
  || fail "meal_plans row missing / not success: $PLAN_STATUS"

# --- 5. cleanup -----------------------------------------------------------------
step "Cleaning up…"
curl_api -X DELETE "$REST/profile_conditions?profile_id=eq.$PID" >/dev/null
curl_api -X DELETE "$REST/profile_restrictions?profile_id=eq.$PID" >/dev/null
curl_api -X DELETE "$REST/meal_plans?profile_id=eq.$PID" >/dev/null   # cascades items
curl_api -X DELETE "$REST/profiles?id=eq.$PID" >/dev/null
pass "deleted test profile $PID"

if [ "$STEP_FAIL" -ne 0 ]; then
  fail "smoke test FAILED"
  exit 1
fi
pass "SMOKE TEST PASSED — extensible rules engine enforced, plan persisted"
