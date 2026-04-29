# API Checklist

Auth:
- `POST /api/v1/auth/login/sms`
- `POST /api/v1/auth/role/switch`
- `POST /api/v1/auth/ui-mode/switch`
- `GET /api/v1/auth/me`

Farmer:
- `POST /api/v1/demands`
- `GET /api/v1/demands`
- `GET /api/v1/demands/{id}/match-status`
- `GET /api/v1/contact-sessions`
- `POST /api/v1/contact-sessions/{id}/confirm`
- `GET /api/v1/orders`
- `GET /api/v1/orders/{id}`
- `POST /api/v1/orders/{id}/confirm-finish`

Owner:
- `GET /api/v1/dispatches/pending`
- `GET /api/v1/dispatches/{attemptId}`
- `POST /api/v1/dispatches/{attemptId}/accept`
- `POST /api/v1/dispatches/{attemptId}/reject`
- `GET /api/v1/equipment`
- `GET /api/v1/service-items`

Debug:
- `GET /api/v1/debug/map/config`
- `POST /api/v1/debug/map/geocode`
- `POST /api/v1/debug/map/distance`
- `GET /api/v1/debug/unified-match/{demandId}/eligible`
- `GET /api/v1/debug/unified-match/{demandId}/tiers`
- `POST /api/v1/debug/unified-match/{demandId}/start`
- `POST /api/v1/debug/unified-match/attempt/{attemptId}/owner-accept`
- `POST /api/v1/debug/unified-match/attempt/{attemptId}/owner-reject`
- `POST /api/v1/debug/unified-match/attempt/{attemptId}/farmer-confirm`
- `POST /api/v1/debug/unified-match/attempt/{attemptId}/farmer-reject`

Match debug payload notes:
- `distanceSource` now indicates whether candidate distance comes from `gaode` or `fallback`
- current local setup uses `fallback` until `GAODE_MAP_API_KEY` is configured
