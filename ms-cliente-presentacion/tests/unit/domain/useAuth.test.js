import { describe, it, expect, vi } from 'vitest'
import { createAuth } from '../../../src/domain/useAuth.js'

const memStorage = (init = {}) => {
  const d = { ...init }
  return {
    getItem: (k) => d[k] ?? null,
    setItem: (k, v) => {
      d[k] = v
    },
    removeItem: (k) => {
      delete d[k]
    },
  }
}

describe('useAuth', () => {
  it('debe_estar_no_autenticado_sin_token', () => {
    const auth = createAuth({ authService: {}, storage: memStorage() })
    expect(auth.estaAutenticado.value).toBe(false)
    expect(auth.getToken()).toBeNull()
  })

  it('debe_restaurar_el_token_desde_el_storage', () => {
    const auth = createAuth({ authService: {}, storage: memStorage({ token: 'x' }) })
    expect(auth.estaAutenticado.value).toBe(true)
    expect(auth.getToken()).toBe('x')
  })

  it('debe_guardar_token_en_storage_al_hacer_login', async () => {
    const storage = memStorage()
    const authService = { login: vi.fn().mockResolvedValue({ token: 'jwt', expiraEn: 60 }) }
    const auth = createAuth({ authService, storage })
    await auth.login('u', 'p')
    expect(authService.login).toHaveBeenCalledWith('u', 'p')
    expect(auth.estaAutenticado.value).toBe(true)
    expect(storage.getItem('token')).toBe('jwt')
  })

  it('debe_propagar_el_error_y_seguir_sin_autenticar_si_el_login_falla', async () => {
    const authService = { login: vi.fn().mockRejectedValue(new Error('401')) }
    const auth = createAuth({ authService, storage: memStorage() })
    await expect(auth.login('u', 'bad')).rejects.toThrow('401')
    expect(auth.estaAutenticado.value).toBe(false)
  })

  it('debe_limpiar_token_y_storage_al_hacer_logout', () => {
    const storage = memStorage({ token: 'x' })
    const auth = createAuth({ authService: {}, storage })
    auth.logout()
    expect(auth.estaAutenticado.value).toBe(false)
    expect(storage.getItem('token')).toBeNull()
  })
})
