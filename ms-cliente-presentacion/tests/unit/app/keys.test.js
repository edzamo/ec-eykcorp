import { describe, it, expect } from 'vitest'
import { createMemoryHistory } from 'vue-router'
import { AUTH_KEY, CLIENTE_SERVICE_KEY } from '../../../src/app/keys.js'
import { buildApp } from '../../../src/app/buildApp.js'

describe('claves de provide/inject', () => {
  it('debe_exportar_symbols_distintos', () => {
    expect(typeof AUTH_KEY).toBe('symbol')
    expect(typeof CLIENTE_SERVICE_KEY).toBe('symbol')
    expect(AUTH_KEY).not.toBe(CLIENTE_SERVICE_KEY)
  })
  it('debe_proveer_auth_y_clienteService_con_esas_claves_desde_el_composition_root', () => {
    const { app, auth } = buildApp({ history: createMemoryHistory(), storage: null })
    expect(app.runWithContext(() => app._context.provides[AUTH_KEY])).toBe(auth)
    expect(app._context.provides[CLIENTE_SERVICE_KEY]).toBeTypeOf('object')
  })
})
