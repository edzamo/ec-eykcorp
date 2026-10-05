import { describe, it, expect, vi } from 'vitest'
import { createAuthService } from '../../../src/services/authService.js'

describe('authService', () => {
  it('debe_hacer_POST_auth_login_con_usuario_y_password_y_devolver_token', async () => {
    const h = { post: vi.fn().mockResolvedValue({ token: 't', expiraEn: 60 }) }
    const res = await createAuthService(h).login('admin', 'pw')
    expect(h.post).toHaveBeenCalledWith('/auth/login', { usuario: 'admin', password: 'pw' })
    expect(res).toEqual({ token: 't', expiraEn: 60 })
  })
})
