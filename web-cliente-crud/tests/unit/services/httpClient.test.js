import { describe, it, expect, vi, beforeAll, afterEach, afterAll } from 'vitest'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { createHttpClient, ApiError } from '../../../src/services/httpClient.js'

const BASE = 'http://localhost/api'
const server = setupServer()
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

const crear = (opts = {}) =>
  createHttpClient({ baseUrl: BASE, getToken: () => null, onUnauthorized: () => {}, ...opts })

describe('httpClient', () => {
  it('debe_enviar_el_header_Authorization_Bearer_cuando_hay_token', async () => {
    let auth
    server.use(
      http.get(`${BASE}/x`, ({ request }) => {
        auth = request.headers.get('authorization')
        return HttpResponse.json({ ok: true })
      }),
    )
    await crear({ getToken: () => 'abc' }).get('/x')
    expect(auth).toBe('Bearer abc')
  })

  it('debe_omitir_Authorization_cuando_no_hay_token', async () => {
    let auth = 'sentinel'
    server.use(
      http.get(`${BASE}/x`, ({ request }) => {
        auth = request.headers.get('authorization')
        return HttpResponse.json({})
      }),
    )
    await crear().get('/x')
    expect(auth).toBeNull()
  })

  it('debe_enviar_body_json_con_content_type_en_post', async () => {
    let recibido
    let ct
    server.use(
      http.post(`${BASE}/x`, async ({ request }) => {
        ct = request.headers.get('content-type')
        recibido = await request.json()
        return HttpResponse.json({ id: 1 }, { status: 201 })
      }),
    )
    const res = await crear().post('/x', { a: 1 })
    expect(recibido).toEqual({ a: 1 })
    expect(ct).toContain('application/json')
    expect(res).toEqual({ id: 1 })
  })

  it('debe_enviar_put_con_body', async () => {
    let recibido
    server.use(
      http.put(`${BASE}/x/1`, async ({ request }) => {
        recibido = await request.json()
        return HttpResponse.json({ id: 1, a: 2 })
      }),
    )
    const res = await crear().put('/x/1', { a: 2 })
    expect(recibido).toEqual({ a: 2 })
    expect(res.a).toBe(2)
  })

  it('debe_devolver_null_en_204', async () => {
    server.use(http.delete(`${BASE}/x/1`, () => new HttpResponse(null, { status: 204 })))
    expect(await crear().delete('/x/1')).toBeNull()
  })

  it('debe_normalizar_ProblemDetail_a_ApiError_con_errores_por_campo', async () => {
    server.use(
      http.post(`${BASE}/x`, () =>
        HttpResponse.json(
          { title: 'Validación', status: 400, detail: 'Datos inválidos', errores: { correo: 'inválido' } },
          { status: 400 },
        ),
      ),
    )
    const err = await crear()
      .post('/x', {})
      .catch((e) => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err).toMatchObject({
      status: 400,
      title: 'Validación',
      detail: 'Datos inválidos',
      errores: { correo: 'inválido' },
    })
  })

  it('debe_crear_ApiError_generico_cuando_el_cuerpo_de_error_no_es_json', async () => {
    server.use(http.get(`${BASE}/x`, () => new HttpResponse('boom', { status: 500 })))
    const err = await crear()
      .get('/x')
      .catch((e) => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(500)
    expect(err.errores).toEqual({})
  })

  it('debe_crear_ApiError_status_0_cuando_falla_la_red', async () => {
    server.use(http.get(`${BASE}/x`, () => HttpResponse.error()))
    const err = await crear()
      .get('/x')
      .catch((e) => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(0)
  })

  it('debe_invocar_onUnauthorized_y_lanzar_ApiError_en_401', async () => {
    const onUnauthorized = vi.fn()
    server.use(
      http.get(`${BASE}/x`, () =>
        HttpResponse.json({ status: 401, title: 'No autorizado' }, { status: 401 }),
      ),
    )
    const err = await crear({ onUnauthorized })
      .get('/x')
      .catch((e) => e)
    expect(onUnauthorized).toHaveBeenCalledOnce()
    expect(err.status).toBe(401)
  })
})
