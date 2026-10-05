import { describe, it, expect, beforeAll, afterEach, afterAll } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import { createMemoryHistory } from 'vue-router'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { buildApp } from '../../../src/app/buildApp.js'

const BASE = 'http://localhost/api'
const server = setupServer()
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

const memStorage = (init = {}) => {
  const d = { ...init }
  return { getItem: (k) => d[k] ?? null, setItem: (k, v) => (d[k] = v), removeItem: (k) => delete d[k], d }
}

async function arrancar(storage) {
  const { app, router } = buildApp({ baseUrl: BASE, storage, history: createMemoryHistory() })
  const el = document.createElement('div')
  document.body.appendChild(el)
  app.mount(el)
  await router.push('/')
  await flushPromises()
  const texto = () => el.textContent
  const h1s = () => [...el.querySelectorAll('h1')].map((h) => h.textContent)
  const boton = (t) => [...el.querySelectorAll('button')].find((b) => b.textContent.trim() === t)
  return { router, storage, texto, h1s, boton }
}

describe('composition root', () => {
  it('debe_mostrar_login_sin_token', async () => {
    const { texto, router } = await arrancar(memStorage())
    expect(router.currentRoute.value.path).toBe('/login')
    expect(texto()).toContain('Iniciar sesión')
  })

  it('debe_enviar_el_token_almacenado_y_mostrar_clientes', async () => {
    let auth
    server.use(
      http.get(`${BASE}/clientes`, ({ request }) => {
        auth = request.headers.get('authorization')
        return HttpResponse.json([
          { id: 1, nombres: 'Ana', apellidos: 'P', correo: 'a@x.com', telefono: '1' },
        ])
      }),
    )
    const { texto } = await arrancar(memStorage({ token: 'jwt1' }))
    expect(auth).toBe('Bearer jwt1')
    expect(texto()).toContain('a@x.com')
  })

  it('debe_redirigir_a_login_y_borrar_el_token_ante_401', async () => {
    server.use(
      http.get(`${BASE}/clientes`, () => HttpResponse.json({ status: 401, title: 'x' }, { status: 401 })),
    )
    const { texto, router, storage } = await arrancar(memStorage({ token: 'viejo' }))
    await flushPromises()
    expect(router.currentRoute.value.path).toBe('/login')
    expect(storage.getItem('token')).toBeNull()
    expect(texto()).toContain('Iniciar sesión')
  })

  it('debe_permitir_cerrar_sesion', async () => {
    server.use(http.get(`${BASE}/clientes`, () => HttpResponse.json([])))
    const { boton, router, storage } = await arrancar(memStorage({ token: 'jwt1' }))
    boton('Cerrar sesión').click()
    await flushPromises()
    expect(storage.getItem('token')).toBeNull()
    expect(router.currentRoute.value.path).toBe('/login')
  })

  it('debe_mostrar_el_encabezado_h1_Clientes', async () => {
    const { h1s } = await arrancar(memStorage())
    expect(h1s()).toEqual(['Clientes'])
  })
})
