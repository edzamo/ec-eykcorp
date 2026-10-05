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
  it('debe_emitir_telefono_null_cuando_el_telefono_esta_vacio', async () => {
    const w = mount(ClienteForm)
    await llenar(w, { nombres: 'A', apellidos: 'B', correo: 'a@b.com', telefono: '' })
    await w.find('form').trigger('submit')
    expect(w.emitted('guardar')[0][0].telefono).toBeNull()
  })
  it('debe_emitir_telefono_null_cuando_el_telefono_son_solo_espacios', async () => {
    const w = mount(ClienteForm)
    await llenar(w, { nombres: 'A', apellidos: 'B', correo: 'a@b.com', telefono: '   ' })
    await w.find('form').trigger('submit')
    expect(w.emitted('guardar')[0][0].telefono).toBeNull()
  })
  it('debe_emitir_el_telefono_recortado_cuando_tiene_valor', async () => {
    const w = mount(ClienteForm)
    await llenar(w, { nombres: 'A', apellidos: 'B', correo: 'a@b.com', telefono: ' 555 ' })
    await w.find('form').trigger('submit')
    expect(w.emitted('guardar')[0][0].telefono).toBe('555')
  })
  it('debe_mostrar_el_telefono_vacio_si_el_cliente_lo_trae_null', () => {
    const w = mount(ClienteForm, { props: { cliente: { ...ana, telefono: null } } })
    expect(campo(w, 'Teléfono').element.value).toBe('')
  })
  it('debe_emitir_telefono_null_al_editar_un_cliente_sin_telefono', async () => {
    const w = mount(ClienteForm, { props: { cliente: { ...ana, telefono: null } } })
    await w.find('form').trigger('submit')
    expect(w.emitted('guardar')[0][0].telefono).toBeNull()
  })
  it('debe_seguir_validando_el_correo_con_la_regex_segura', async () => {
    const validos = ['a@b.co', 'ana.perez+x@dominio.com.ec']
    const invalidos = ['sin-arroba', 'a@b', 'a@@b.com', 'a b@c.com', '@b.com', 'a@.com']
    for (const correo of validos) {
      const w = mount(ClienteForm)
      await llenar(w, { nombres: 'A', apellidos: 'B', correo, telefono: '' })
      await w.find('form').trigger('submit')
      expect(w.emitted('guardar'), correo).toHaveLength(1)
    }
    for (const correo of invalidos) {
      const w = mount(ClienteForm)
      await llenar(w, { nombres: 'A', apellidos: 'B', correo, telefono: '' })
      await w.find('form').trigger('submit')
      expect(w.emitted('guardar'), correo).toBeUndefined()
    }
  })
  it('debe_evaluar_rapido_una_cadena_patologica_de_correo', async () => {
    const w = mount(ClienteForm)
    await llenar(w, { nombres: 'A', apellidos: 'B', correo: 'a@' + '.'.repeat(50000) + ' x', telefono: '' })
    const t0 = performance.now()
    await w.find('form').trigger('submit')
    expect(performance.now() - t0).toBeLessThan(200)
    expect(w.emitted('guardar')).toBeUndefined()
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
