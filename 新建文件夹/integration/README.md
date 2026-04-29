# Nong Helper Integration Notes

This folder keeps the local integration checklist for the current workspace.

Current scope:
- backend unified match flow
- frontend uni-app MVP pages
- admin web dashboard
- MySQL + Redis local startup defaults
- gaode map integration adapter with fallback distance mode

Suggested demo path:
1. Start MySQL and Redis
2. Start backend
3. Open uni-app login page
4. Login as farmer and create a demand
5. Login as owner and accept a dispatch
6. Confirm contact and open order detail

Important local defaults:
- backend port: `8080`
- frontend API base: `http://localhost:8080/api/v1`
- default demo DB: `nongzhushou`

Map adapter notes:
- set `GAODE_MAP_API_KEY` in the workspace `.env` file when ready to enable real map distance
- debug endpoints under `/api/v1/debug/map/*` are safe to use before switching main flows
- match debug endpoints now expose `distanceSource` for candidate inspection
