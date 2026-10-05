import { describe, it, expect, vi } from 'vitest'
import { createClienteService } from '../../../src/services/clienteService.js'

const fakeHttp = () => ({
  get: vi.fn().mockResolvedValue('g'),
  post: vi.fn().mockResolvedValue('p'),
  put: vi.fn().mockResolvedValue('u'),
  delete: vi.fn().mockResolvedValue(null),
})

describe('clienteService', () => {
  it('debe_listar_con_GET_clientes', async () => {
    const h = fakeHttp()
    expect(await createClienteService(h).listar()).toBe('g')
    expect(h.get).toHaveBeenCalledWith('/clientes')
  })
  it('debe_obtener_con_GET_clientes_id', async () => {
    const h = fakeHttp()
    await createClienteService(h).obtener(3)
    expect(h.get).toHaveBeenCalledWith('/clientes/3')
  })
  it('debe_crear_con_POST_clientes', async () => {
    const h = fakeHttp()
    expect(await createClienteService(h).crear({ nombres: 'A' })).toBe('p')
    expect(h.post).toHaveBeenCalledWith('/clientes', { nombres: 'A' })
  })
  it('debe_actualizar_con_PUT_clientes_id', async () => {
    const h = fakeHttp()
    await createClienteService(h).actualizar(3, { nombres: 'B' })
    expect(h.put).toHaveBeenCalledWith('/clientes/3', { nombres: 'B' })
  })
  it('debe_eliminar_con_DELETE_clientes_id', async () => {
    const h = fakeHttp()
    await createClienteService(h).eliminar(3)
    expect(h.delete).toHaveBeenCalledWith('/clientes/3')
  })
})
