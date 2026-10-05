import { describe, it, expect, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import ConfirmDialog from '../../../src/components/ConfirmDialog.vue'
import { boton } from '../../support/dom.js'

let w
afterEach(() => w?.unmount())
const montar = () =>
  (w = mount(ConfirmDialog, {
    props: { titulo: 'Eliminar cliente', mensaje: '¿Eliminar a Ana?' },
    attachTo: document.body,
  }))

describe('ConfirmDialog', () => {
  it('debe_exponer_role_dialog_modal_etiquetado_con_titulo_y_mensaje', () => {
    montar()
    const d = w.find('[role="dialog"]')
    expect(d.attributes('aria-modal')).toBe('true')
    expect(d.attributes('aria-labelledby')).toBe(w.find('h2').attributes('id'))
    expect(d.text()).toContain('Eliminar cliente')
    expect(d.text()).toContain('¿Eliminar a Ana?')
  })
  it('debe_enfocar_el_boton_cancelar_al_abrir', async () => {
    montar()
    await w.vm.$nextTick()
    expect(document.activeElement).toBe(boton(w, 'Cancelar').element)
  })
  it('debe_emitir_confirmar_al_pulsar_el_boton_confirmar', async () => {
    montar()
    await boton(w, 'Eliminar').trigger('click')
    expect(w.emitted('confirmar')).toHaveLength(1)
  })
  it('debe_emitir_cancelar_al_pulsar_cancelar', async () => {
    montar()
    await boton(w, 'Cancelar').trigger('click')
    expect(w.emitted('cancelar')).toHaveLength(1)
  })
  it('debe_emitir_cancelar_al_pulsar_Escape', async () => {
    montar()
    await w.find('[role="dialog"]').trigger('keydown', { key: 'Escape' })
    expect(w.emitted('cancelar')).toHaveLength(1)
  })
})
