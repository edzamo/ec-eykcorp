export function createAuthService(http) {
  return {
    login: (usuario, password) => http.post('/auth/login', { usuario, password }),
  }
}
