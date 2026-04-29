const BASE_URL = 'http://localhost:8080/api/v1'

export async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    method: options.method || 'GET',
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    },
    body: options.body ? JSON.stringify(options.body) : undefined
  })

  const payload = await response.json()
  if (!response.ok || payload.code !== 0) {
    throw new Error(payload.message || 'Request failed')
  }
  return payload.data
}
