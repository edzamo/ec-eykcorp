import { createApp } from 'vue'
import App from '../App.vue'
import { createHttpClient } from '../services/httpClient.js'
import { createClienteService } from '../services/clienteService.js'
import { createAuthService } from '../services/authService.js'
import { createAuth } from '../domain/useAuth.js'
import { createAppRouter } from './router.js'
import { AUTH_KEY, CLIENTE_SERVICE_KEY } from './keys.js'

// Composition root: único lugar que conoce todas las implementaciones.
export function buildApp({ baseUrl = '/api', storage = globalThis.sessionStorage, history } = {}) {
  let auth
  let router
  const httpClient = createHttpClient({
    baseUrl,
    getToken: () => auth.getToken(),
    onUnauthorized: () => {
      auth.logout()
      router.push('/login')
    },
  })
  auth = createAuth({ authService: createAuthService(httpClient), storage })
  router = createAppRouter({ auth, history })

  const app = createApp(App)
  app.provide(AUTH_KEY, auth)
  app.provide(CLIENTE_SERVICE_KEY, createClienteService(httpClient))
  app.use(router)
  return { app, router, auth }
}
