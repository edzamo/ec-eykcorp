const LOGIN_PATH = '/auth/login'

export class ApiError extends Error {
  constructor({ status, title, detail, errores } = {}) {
    super(detail || title || 'Error de red')
    this.name = 'ApiError'
    this.status = status ?? 0
    this.title = title ?? ''
    this.detail = detail ?? ''
    this.errores = errores ?? {}
  }
}

async function leerError(res) {
  try {
    const body = await res.json()
    return new ApiError({ ...body, status: res.status })
  } catch {
    return new ApiError({ status: res.status, title: res.statusText })
  }
}

export function createHttpClient({ baseUrl = '/api', getToken, onUnauthorized, fetchImpl } = {}) {
  async function request(method, path, body) {
    const headers = { Accept: 'application/json' }
    const token = getToken?.()
    if (token) headers.Authorization = `Bearer ${token}`
    const init = { method, headers }
    if (body !== undefined) {
      headers['Content-Type'] = 'application/json'
      init.body = JSON.stringify(body)
    }
    let res
    try {
      res = await (fetchImpl ?? fetch)(`${baseUrl}${path}`, init)
    } catch {
      throw new ApiError({ status: 0, title: 'Error de red', detail: 'No se pudo conectar con el servidor' })
    }
    if (!res.ok) {
      const err = await leerError(res)
      // El 401 del propio login son credenciales incorrectas, no una sesión caducada.
      if (res.status === 401 && path !== LOGIN_PATH) onUnauthorized?.()
      throw err
    }
    return res.status === 204 ? null : res.json()
  }

  return {
    get: (path) => request('GET', path),
    post: (path, body) => request('POST', path, body),
    put: (path, body) => request('PUT', path, body),
    delete: (path) => request('DELETE', path),
  }
}
