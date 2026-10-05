import { describe, it, expect, vi } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import LoginPage from '../../../src/pages/LoginPage.vue'
import { ApiError } from '../../../src/services/httpClient.js'
import { campo, boton } from '../../support/dom.js'

async function montar(login) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<p>home</p>' } },
      { path: '/login', component: LoginPage },
    ],
  })
  await router.push('/login')
  const w = mount(LoginPage, { global: { plugins: [router], provide: { auth: { login } } } })
  return { w, router }
}
async function enviar(w, u = 'admin', p = 'pw') {
  await campo(w, 'Usuario').setValue(u)
  await campo(w, 'Contraseña').setValue(p)
  await w.find('form').trigger('submit')
  await flushPromises()
}

describe('LoginPage', () => {
  it('debe_ocultar_la_contraseña', async () => {
    const { w } = await montar(vi.fn())
    expect(campo(w, 'Contraseña').attributes('type')).toBe('password')
  })
  it('debe_hacer_login_y_navegar_a_la_raiz', async () => {
    const login = vi.fn().mockResolvedValue()
    const { w, router } = await montar(login)
    await enviar(w)
    expect(login).toHaveBeenCalledWith('admin', 'pw')
    expect(router.currentRoute.value.path).toBe('/')
  })
  it('debe_mostrar_error_con_credenciales_invalidas_y_no_navegar', async () => {
    const login = vi.fn().mockRejectedValue(new ApiError({ status: 401, title: 'No autorizado' }))
    const { w, router } = await montar(login)
    await enviar(w)
    expect(w.find('[role="alert"]').text()).toContain('Usuario o contraseña incorrectos')
    expect(router.currentRoute.value.path).toBe('/login')
  })
  it('debe_mostrar_mensaje_generico_ante_otro_error', async () => {
    const login = vi.fn().mockRejectedValue(new ApiError({ status: 500, title: 'x' }))
    const { w } = await montar(login)
    await enviar(w)
    expect(w.find('[role="alert"]').text()).toContain('No se pudo iniciar sesión')
  })
  it('debe_requerir_usuario_y_password_sin_llamar_al_servicio', async () => {
    const login = vi.fn()
    const { w } = await montar(login)
    await enviar(w, '', '')
    expect(login).not.toHaveBeenCalled()
    expect(boton(w, 'Ingresar')).toBeTruthy()
  })
})
