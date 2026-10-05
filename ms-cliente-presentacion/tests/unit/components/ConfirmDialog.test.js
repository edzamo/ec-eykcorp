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
  it('debe_emitir_cancelar_al_pulsar_Escape_con_el_foco_en_un_boton_o_en_el_contenedor', async () => {
    montar()
    await boton(w, 'Eliminar').trigger('keydown', { key: 'Escape' })
    w.element.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }))
    expect(w.emitted('cancelar')).toHaveLength(2)
  })
  it('debe_restaurar_el_foco_al_elemento_que_lo_abrio_al_cerrarse', async () => {
    const opener = document.createElement('button')
    document.body.appendChild(opener)
    opener.focus()
    montar()
    await w.vm.$nextTick()
    expect(document.activeElement).toBe(boton(w, 'Cancelar').element)
    w.unmount()
    expect(document.activeElement).toBe(opener)
    opener.remove()
    w = null
  })
  it('debe_ciclar_el_foco_con_Tab_desde_el_ultimo_boton_al_primero', async () => {
    montar()
    boton(w, 'Eliminar').element.focus()
    const ev = new KeyboardEvent('keydown', { key: 'Tab', bubbles: true, cancelable: true })
    boton(w, 'Eliminar').element.dispatchEvent(ev)
    expect(ev.defaultPrevented).toBe(true)
    expect(document.activeElement).toBe(boton(w, 'Cancelar').element)
  })
  it('debe_ciclar_el_foco_con_Shift_Tab_desde_el_primer_boton_al_ultimo', async () => {
    montar()
    boton(w, 'Cancelar').element.focus()
    const ev = new KeyboardEvent('keydown', { key: 'Tab', shiftKey: true, bubbles: true, cancelable: true })
    boton(w, 'Cancelar').element.dispatchEvent(ev)
    expect(ev.defaultPrevented).toBe(true)
    expect(document.activeElement).toBe(boton(w, 'Eliminar').element)
  })
  it('no_debe_interceptar_Tab_cuando_el_foco_esta_en_medio_del_ciclo', async () => {
    montar()
    boton(w, 'Cancelar').element.focus()
    const ev = new KeyboardEvent('keydown', { key: 'Tab', bubbles: true, cancelable: true })
    boton(w, 'Cancelar').element.dispatchEvent(ev)
    expect(ev.defaultPrevented).toBe(false)
  })
})
