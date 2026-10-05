import { describe, it, expect, vi, beforeAll, afterEach, afterAll } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { http, HttpResponse, delay } from 'msw'
import { setupServer } from 'msw/node'
import { CLIENTE_SERVICE_KEY } from '../../../src/app/keys.js'
import ClientesPage from '../../../src/pages/ClientesPage.vue'
import { createHttpClient } from '../../../src/services/httpClient.js'
import { createClienteService } from '../../../src/services/clienteService.js'
import { campo, boton } from '../../support/dom.js'

const BASE = 'http://localhost/api'
const ana = {
  id: 1,
  nombres: 'Ana',
  apellidos: 'Pérez',
  correo: 'ana@x.com',
  telefono: '555',
  fechaCreacion: '2026-01-02T10:00:00Z',
}
const luis = { ...ana, id: 2, nombres: 'Luis', apellidos: 'Gómez', correo: 'luis@x.com' }

const server = setupServer()
beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

const listaHandler = (data = [ana, luis]) => http.get(`${BASE}/clientes`, () => HttpResponse.json(data))

async function montar({ onUnauthorized = vi.fn() } = {}) {
  const client = createHttpClient({ baseUrl: BASE, getToken: () => 't', onUnauthorized })
  const w = mount(ClientesPage, {
    global: { provide: { [CLIENTE_SERVICE_KEY]: createClienteService(client) } },
    attachTo: document.body,
  })
  await flushPromises()
  return w
}
const esperar = async () => {
  await new Promise((r) => setTimeout(r, 20))
  await flushPromises()
}

describe('ClientesPage', () => {
  it('debe_mostrar_spinner_mientras_carga_y_luego_la_lista', async () => {
    server.use(
      http.get(`${BASE}/clientes`, async () => {
        await delay(30)
        return HttpResponse.json([ana])
      }),
    )
    const w = mount(ClientesPage, {
      global: {
        provide: { [CLIENTE_SERVICE_KEY]: createClienteService(createHttpClient({ baseUrl: BASE })) },
      },
    })
    await w.vm.$nextTick()
    expect(w.find('[role="status"]').exists()).toBe(true)
    await new Promise((r) => setTimeout(r, 80))
    await flushPromises()
    expect(w.find('[role="status"]').exists()).toBe(false)
    expect(w.text()).toContain('Ana')
  })

  it('debe_listar_los_clientes', async () => {
    server.use(listaHandler())
    const w = await montar()
    expect(w.text()).toContain('Ana')
    expect(w.text()).toContain('luis@x.com')
  })

  it('debe_mostrar_alerta_cuando_el_listado_falla_con_500', async () => {
    server.use(
      http.get(`${BASE}/clientes`, () =>
        HttpResponse.json(
          { status: 500, title: 'Error interno', detail: 'Fallo inesperado' },
          { status: 500 },
        ),
      ),
    )
    const w = await montar()
    expect(w.find('[role="alert"]').text()).toContain('Fallo inesperado')
  })

  it('debe_invocar_onUnauthorized_cuando_el_listado_responde_401', async () => {
    server.use(
      http.get(`${BASE}/clientes`, () =>
        HttpResponse.json({ status: 401, title: 'No autorizado' }, { status: 401 }),
      ),
    )
    const onUnauthorized = vi.fn()
    await montar({ onUnauthorized })
    expect(onUnauthorized).toHaveBeenCalled()
  })

  it('debe_crear_un_cliente_y_mostrarlo_en_la_tabla', async () => {
    server.use(listaHandler([ana]))
    let body
    server.use(
      http.post(`${BASE}/clientes`, async ({ request }) => {
        body = await request.json()
        return HttpResponse.json({ ...luis }, { status: 201 })
      }),
    )
    const w = await montar()
    await boton(w, 'Nuevo cliente').trigger('click')
    await campo(w, 'Nombres').setValue('Luis')
    await campo(w, 'Apellidos').setValue('Gómez')
    await campo(w, 'Correo').setValue('luis@x.com')
    await campo(w, 'Teléfono').setValue('555')
    await w.find('form').trigger('submit')
    await esperar()
    expect(body).toEqual({ nombres: 'Luis', apellidos: 'Gómez', correo: 'luis@x.com', telefono: '555' })
    expect(w.text()).toContain('Gómez')
    expect(w.find('form').exists()).toBe(false)
  })

  it('debe_crear_un_cliente_sin_telefono_enviando_telefono_null', async () => {
    server.use(listaHandler([]))
    let body
    server.use(
      http.post(`${BASE}/clientes`, async ({ request }) => {
        body = await request.json()
        return HttpResponse.json({ ...luis, telefono: null }, { status: 201 })
      }),
    )
    const w = await montar()
    await boton(w, 'Nuevo cliente').trigger('click')
    await campo(w, 'Nombres').setValue('Luis')
    await campo(w, 'Apellidos').setValue('Gómez')
    await campo(w, 'Correo').setValue('luis@x.com')
    await w.find('form').trigger('submit')
    await esperar()
    expect(body).toEqual({ nombres: 'Luis', apellidos: 'Gómez', correo: 'luis@x.com', telefono: null })
    expect(w.text()).toContain('Gómez')
    expect(w.find('form').exists()).toBe(false)
  })

  it('debe_editar_un_cliente_con_telefono_null_mostrando_el_campo_vacio', async () => {
    server.use(listaHandler([{ ...ana, telefono: null }]))
    let body
    server.use(
      http.put(`${BASE}/clientes/1`, async ({ request }) => {
        body = await request.json()
        return HttpResponse.json({ ...ana, ...body })
      }),
    )
    const w = await montar()
    await boton(w, 'Editar Ana Pérez').trigger('click')
    expect(campo(w, 'Teléfono').element.value).toBe('')
    await w.find('form').trigger('submit')
    await esperar()
    expect(body.telefono).toBeNull()
    expect(w.find('form').exists()).toBe(false)
  })

  it('debe_mostrar_el_mensaje_del_backend_en_409_y_mantener_el_formulario', async () => {
    server.use(listaHandler([]))
    server.use(
      http.post(`${BASE}/clientes`, () =>
        HttpResponse.json(
          { status: 409, title: 'Conflicto', detail: 'El correo ya está registrado' },
          { status: 409 },
        ),
      ),
    )
    const w = await montar()
    await boton(w, 'Nuevo cliente').trigger('click')
    await campo(w, 'Nombres').setValue('A')
    await campo(w, 'Apellidos').setValue('B')
    await campo(w, 'Correo').setValue('a@b.com')
    await w.find('form').trigger('submit')
    await esperar()
    expect(w.text()).toContain('El correo ya está registrado')
    expect(w.find('form').exists()).toBe(true)
  })

  it('debe_mostrar_errores_por_campo_en_400', async () => {
    server.use(listaHandler([]))
    server.use(
      http.post(`${BASE}/clientes`, () =>
        HttpResponse.json(
          {
            status: 400,
            title: 'Validación',
            detail: 'Datos inválidos',
            errores: { telefono: 'teléfono inválido' },
          },
          { status: 400 },
        ),
      ),
    )
    const w = await montar()
    await boton(w, 'Nuevo cliente').trigger('click')
    await campo(w, 'Nombres').setValue('A')
    await campo(w, 'Apellidos').setValue('B')
    await campo(w, 'Correo').setValue('a@b.com')
    await campo(w, 'Teléfono').setValue('x')
    await w.find('form').trigger('submit')
    await esperar()
    expect(w.text()).toContain('teléfono inválido')
    expect(campo(w, 'Teléfono').attributes('aria-invalid')).toBe('true')
  })

  it('debe_editar_un_cliente_precargado_y_reflejar_el_cambio', async () => {
    server.use(listaHandler([ana]))
    let body
    server.use(
      http.put(`${BASE}/clientes/1`, async ({ request }) => {
        body = await request.json()
        return HttpResponse.json({ ...ana, ...body })
      }),
    )
    const w = await montar()
    await boton(w, 'Editar Ana Pérez').trigger('click')
    expect(campo(w, 'Nombres').element.value).toBe('Ana')
    await campo(w, 'Nombres').setValue('Anita')
    await w.find('form').trigger('submit')
    await esperar()
    expect(body.nombres).toBe('Anita')
    expect(w.text()).toContain('Anita')
    expect(w.find('form').exists()).toBe(false)
  })

  it('debe_enfocar_el_primer_campo_al_abrir_el_formulario_de_alta_y_de_edicion', async () => {
    server.use(listaHandler([ana]))
    const w = await montar()
    await boton(w, 'Nuevo cliente').trigger('click')
    await flushPromises()
    expect(document.activeElement).toBe(campo(w, 'Nombres').element)
    await boton(w, 'Cancelar').trigger('click')
    await boton(w, 'Editar Ana Pérez').trigger('click')
    await flushPromises()
    expect(document.activeElement).toBe(campo(w, 'Nombres').element)
    w.unmount()
  })

  it('debe_cerrar_el_formulario_al_cancelar', async () => {
    server.use(listaHandler([]))
    const w = await montar()
    await boton(w, 'Nuevo cliente').trigger('click')
    await boton(w, 'Cancelar').trigger('click')
    expect(w.find('form').exists()).toBe(false)
  })

  it('debe_eliminar_tras_confirmar_en_el_dialogo', async () => {
    server.use(listaHandler([ana, luis]))
    const borrado = vi.fn()
    server.use(
      http.delete(`${BASE}/clientes/1`, () => {
        borrado()
        return new HttpResponse(null, { status: 204 })
      }),
    )
    const w = await montar()
    await boton(w, 'Eliminar Ana Pérez').trigger('click')
    expect(w.find('[role="dialog"]').exists()).toBe(true)
    await w
      .find('[role="dialog"]')
      .findAll('button')
      .find((b) => b.text() === 'Eliminar')
      .trigger('click')
    await esperar()
    expect(borrado).toHaveBeenCalledOnce()
    expect(w.text()).not.toContain('ana@x.com')
    expect(w.find('[role="dialog"]').exists()).toBe(false)
  })

  it('debe_conservar_el_cliente_si_se_cancela_la_confirmacion', async () => {
    server.use(listaHandler([ana]))
    const w = await montar()
    await boton(w, 'Eliminar Ana Pérez').trigger('click')
    await w
      .find('[role="dialog"]')
      .findAll('button')
      .find((b) => b.text() === 'Cancelar')
      .trigger('click')
    expect(w.find('[role="dialog"]').exists()).toBe(false)
    expect(w.text()).toContain('ana@x.com')
  })

  it('debe_mostrar_alerta_si_eliminar_falla_con_404', async () => {
    server.use(listaHandler([ana]))
    server.use(
      http.delete(`${BASE}/clientes/1`, () =>
        HttpResponse.json(
          { status: 404, title: 'No encontrado', detail: 'Cliente no existe' },
          { status: 404 },
        ),
      ),
    )
    const w = await montar()
    await boton(w, 'Eliminar Ana Pérez').trigger('click')
    await w
      .find('[role="dialog"]')
      .findAll('button')
      .find((b) => b.text() === 'Eliminar')
      .trigger('click')
    await esperar()
    expect(w.find('[role="alert"]').text()).toContain('Cliente no existe')
  })
})
