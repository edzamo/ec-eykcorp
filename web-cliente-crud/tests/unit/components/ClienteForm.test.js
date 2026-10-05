import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ClienteForm from '../../../src/components/ClienteForm.vue'
import { campo, boton } from '../../support/dom.js'

const ana = {
  id: 1,
  nombres: 'Ana',
  apellidos: 'Pérez',
  correo: 'ana@x.com',
  telefono: '555',
  fechaCreacion: 'x',
}

async function llenar(w, d) {
  await campo(w, 'Nombres').setValue(d.nombres)
  await campo(w, 'Apellidos').setValue(d.apellidos)
  await campo(w, 'Correo').setValue(d.correo)
  await campo(w, 'Teléfono').setValue(d.telefono)
}

describe('ClienteForm', () => {
  it('debe_iniciar_vacio_en_modo_crear', () => {
    const w = mount(ClienteForm)
    expect(campo(w, 'Nombres').element.value).toBe('')
  })
  it('debe_precargar_los_datos_en_modo_editar', () => {
    const w = mount(ClienteForm, { props: { cliente: ana } })
    expect(campo(w, 'Correo').element.value).toBe('ana@x.com')
    expect(campo(w, 'Teléfono').element.value).toBe('555')
  })
  it('debe_emitir_guardar_con_los_datos_sin_id_ni_fecha', async () => {
    const w = mount(ClienteForm, { props: { cliente: ana } })
    await campo(w, 'Nombres').setValue('Anita')
    await w.find('form').trigger('submit')
    expect(w.emitted('guardar')[0][0]).toEqual({
      nombres: 'Anita',
      apellidos: 'Pérez',
      correo: 'ana@x.com',
      telefono: '555',
    })
  })
  it('debe_mostrar_obligatorio_y_no_emitir_si_faltan_campos_requeridos', async () => {
    const w = mount(ClienteForm)
    await w.find('form').trigger('submit')
    expect(w.emitted('guardar')).toBeUndefined()
    expect(w.findAll('[role="alert"]').length).toBeGreaterThanOrEqual(3)
    expect(w.text()).toContain('Los nombres son obligatorios')
  })
  it('debe_rechazar_correo_con_formato_invalido_solo_por_UX', async () => {
    const w = mount(ClienteForm)
    await llenar(w, { nombres: 'A', apellidos: 'B', correo: 'no-es-correo', telefono: '1' })
    await w.find('form').trigger('submit')
    expect(w.emitted('guardar')).toBeUndefined()
    expect(w.text()).toContain('Ingrese un correo válido')
  })
  it('debe_mostrar_los_errores_por_campo_recibidos_del_backend', () => {
    const w = mount(ClienteForm, {
      props: { errores: { correo: 'ya está registrado', telefono: 'formato inválido' } },
    })
    expect(w.text()).toContain('ya está registrado')
    expect(w.text()).toContain('formato inválido')
    expect(campo(w, 'Correo').attributes('aria-invalid')).toBe('true')
  })
  it('debe_deshabilitar_guardar_mientras_guarda', () => {
    const w = mount(ClienteForm, { props: { guardando: true } })
    expect(boton(w, 'Guardar').attributes('disabled')).toBeDefined()
  })
  it('debe_emitir_cancelar', async () => {
    const w = mount(ClienteForm)
    await boton(w, 'Cancelar').trigger('click')
    expect(w.emitted('cancelar')).toHaveLength(1)
  })
})
