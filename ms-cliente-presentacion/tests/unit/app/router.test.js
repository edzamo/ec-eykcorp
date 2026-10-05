import { describe, it, expect } from 'vitest'
import { ref } from 'vue'
import { createMemoryHistory } from 'vue-router'
import { createAppRouter } from '../../../src/app/router.js'

const mk = (autenticado) =>
  createAppRouter({ auth: { estaAutenticado: ref(autenticado) }, history: createMemoryHistory() })

describe('router', () => {
  it('debe_redirigir_a_login_si_no_hay_autenticacion', async () => {
    const r = mk(false)
    await r.push('/')
    expect(r.currentRoute.value.path).toBe('/login')
  })
  it('debe_permitir_la_raiz_si_esta_autenticado', async () => {
    const r = mk(true)
    await r.push('/')
    expect(r.currentRoute.value.path).toBe('/')
  })
  it('debe_permitir_login_sin_autenticacion', async () => {
    const r = mk(false)
    await r.push('/login')
    expect(r.currentRoute.value.path).toBe('/login')
  })
  it('debe_enviar_a_la_raiz_si_ya_autenticado_visita_login', async () => {
    const r = mk(true)
    await r.push('/login')
    expect(r.currentRoute.value.path).toBe('/')
  })
})
