import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ClienteTable from '../../../src/components/ClienteTable.vue'
import { boton } from '../../support/dom.js'

const ana = {
  id: 1,
  nombres: 'Ana',
  apellidos: 'Pérez',
  correo: 'ana@x.com',
  telefono: '555',
  fechaCreacion: '2026-01-02T10:00:00Z',
}
const luis = { ...ana, id: 2, nombres: 'Luis', apellidos: 'Gómez', correo: 'luis@x.com' }

describe('ClienteTable', () => {
  it('debe_mostrar_mensaje_de_vacio_sin_clientes', () => {
    const w = mount(ClienteTable, { props: { clientes: [] } })
    expect(w.text()).toContain('No hay clientes registrados')
    expect(w.findAll('tbody tr')).toHaveLength(0)
  })
  it('debe_renderizar_una_fila_por_cliente_con_sus_datos', () => {
    const w = mount(ClienteTable, { props: { clientes: [ana, luis] } })
    expect(w.findAll('tbody tr')).toHaveLength(2)
    expect(w.text()).toContain('Ana')
    expect(w.text()).toContain('Pérez')
    expect(w.text()).toContain('luis@x.com')
  })
  it('debe_tener_encabezados_de_columna_accesibles', () => {
    const w = mount(ClienteTable, { props: { clientes: [ana] } })
    const th = w.findAll('th[scope="col"]').map((t) => t.text())
    expect(th).toEqual(expect.arrayContaining(['Nombres', 'Apellidos', 'Correo', 'Teléfono']))
  })
  it('debe_emitir_editar_con_el_cliente', async () => {
    const w = mount(ClienteTable, { props: { clientes: [ana, luis] } })
    await boton(w, 'Editar Luis Gómez').trigger('click')
    expect(w.emitted('editar')[0]).toEqual([luis])
  })
  it('debe_emitir_eliminar_con_el_cliente', async () => {
    const w = mount(ClienteTable, { props: { clientes: [ana] } })
    await boton(w, 'Eliminar Ana Pérez').trigger('click')
    expect(w.emitted('eliminar')[0]).toEqual([ana])
  })
  it('debe_escapar_html_en_los_datos', () => {
    const w = mount(ClienteTable, { props: { clientes: [{ ...ana, nombres: '<i>x</i>' }] } })
    expect(w.find('i').exists()).toBe(false)
  })
})
