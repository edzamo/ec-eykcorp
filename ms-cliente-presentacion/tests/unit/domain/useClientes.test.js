import { describe, it, expect, vi } from 'vitest'
import { useClientes } from '../../../src/domain/useClientes.js'

const c1 = { id: 1, nombres: 'Ana' }
const c2 = { id: 2, nombres: 'Luis' }

describe('useClientes', () => {
  it('debe_iniciar_vacio_sin_carga_ni_error', () => {
    const s = useClientes({})
    expect(s.clientes.value).toEqual([])
    expect(s.cargando.value).toBe(false)
    expect(s.error.value).toBeNull()
  })

  it('debe_cargar_clientes_y_marcar_cargando_durante_la_llamada', async () => {
    let resolver
    const svc = { listar: vi.fn(() => new Promise((r) => (resolver = r))) }
    const s = useClientes(svc)
    const p = s.cargar()
    expect(s.cargando.value).toBe(true)
    resolver([c1])
    await p
    expect(s.cargando.value).toBe(false)
    expect(s.clientes.value).toEqual([c1])
  })

  it('debe_exponer_el_error_cuando_cargar_falla', async () => {
    const err = new Error('500')
    const s = useClientes({ listar: vi.fn().mockRejectedValue(err) })
    await s.cargar()
    expect(s.error.value).toBe(err)
    expect(s.cargando.value).toBe(false)
  })

  it('debe_limpiar_el_error_previo_al_recargar', async () => {
    const listar = vi.fn().mockRejectedValueOnce(new Error('x')).mockResolvedValueOnce([])
    const s = useClientes({ listar })
    await s.cargar()
    await s.cargar()
    expect(s.error.value).toBeNull()
  })

  it('debe_agregar_el_cliente_creado_a_la_lista', async () => {
    const s = useClientes({ crear: vi.fn().mockResolvedValue(c2) })
    const res = await s.crear({ nombres: 'Luis' })
    expect(res).toEqual(c2)
    expect(s.clientes.value).toEqual([c2])
  })

  it('debe_propagar_el_error_de_crear_sin_modificar_la_lista', async () => {
    const s = useClientes({ crear: vi.fn().mockRejectedValue(new Error('409')) })
    await expect(s.crear({})).rejects.toThrow('409')
    expect(s.clientes.value).toEqual([])
  })

  it('debe_reemplazar_el_cliente_actualizado', async () => {
    const svc = {
      listar: vi.fn().mockResolvedValue([c1, c2]),
      actualizar: vi.fn().mockResolvedValue({ id: 1, nombres: 'Ana M' }),
    }
    const s = useClientes(svc)
    await s.cargar()
    await s.actualizar(1, { nombres: 'Ana M' })
    expect(svc.actualizar).toHaveBeenCalledWith(1, { nombres: 'Ana M' })
    expect(s.clientes.value).toEqual([{ id: 1, nombres: 'Ana M' }, c2])
  })

  it('debe_quitar_el_cliente_eliminado', async () => {
    const svc = { listar: vi.fn().mockResolvedValue([c1, c2]), eliminar: vi.fn().mockResolvedValue(null) }
    const s = useClientes(svc)
    await s.cargar()
    await s.eliminar(1)
    expect(s.clientes.value).toEqual([c2])
  })

  it('debe_exponer_error_y_conservar_lista_si_eliminar_falla', async () => {
    const svc = {
      listar: vi.fn().mockResolvedValue([c1]),
      eliminar: vi.fn().mockRejectedValue(new Error('404')),
    }
    const s = useClientes(svc)
    await s.cargar()
    await s.eliminar(1).catch(() => {})
    expect(s.clientes.value).toEqual([c1])
    expect(s.error.value).toBeInstanceOf(Error)
  })
})
