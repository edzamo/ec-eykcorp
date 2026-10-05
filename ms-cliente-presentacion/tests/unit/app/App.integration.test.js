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
  const mains = () => el.querySelectorAll('main').length
  const boton = (t) => [...el.querySelectorAll('button')].find((b) => b.textContent.trim() === t)
  return { router, storage, texto, h1s, boton, mains, el }
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

  it('debe_tener_un_unico_main_con_el_contenido_tanto_en_login_como_en_clientes', async () => {
    const login = await arrancar(memStorage())
    expect(login.mains()).toBe(1)
    expect(login.el.querySelector('main').textContent).toContain('Iniciar sesión')
    server.use(http.get(`${BASE}/clientes`, () => HttpResponse.json([])))
    const clientes = await arrancar(memStorage({ token: 'jwt1' }))
    expect(clientes.mains()).toBe(1)
    expect(clientes.el.querySelector('main').textContent).toContain('Listado de clientes')
  })

  it('debe_mostrar_el_error_en_el_formulario_sin_redirigir_con_credenciales_incorrectas', async () => {
    server.use(
      http.post(`${BASE}/auth/login`, () =>
        HttpResponse.json({ status: 401, title: 'No autorizado' }, { status: 401 }),
      ),
    )
    const { el, router, texto, storage } = await arrancar(memStorage())
    const input = (i) => el.querySelectorAll('input')[i]
    input(0).value = 'admin'
    input(0).dispatchEvent(new Event('input'))
    input(1).value = 'mala'
    input(1).dispatchEvent(new Event('input'))
    el.querySelector('form').dispatchEvent(new Event('submit', { cancelable: true }))
    await flushPromises()
    await new Promise((r) => setTimeout(r, 30))
    await flushPromises()
    expect(texto()).toContain('Usuario o contraseña incorrectos')
    expect(router.currentRoute.value.path).toBe('/login')
    expect(storage.getItem('token')).toBeNull()
  })
})
