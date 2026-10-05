import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import LoadingSpinner from '../../../src/components/LoadingSpinner.vue'

describe('LoadingSpinner', () => {
  it('debe_exponer_role_status_con_texto_por_defecto', () => {
    const w = mount(LoadingSpinner)
    expect(w.find('[role="status"]').text()).toContain('Cargando')
  })
  it('debe_mostrar_el_texto_recibido', () => {
    const w = mount(LoadingSpinner, { props: { texto: 'Guardando…' } })
    expect(w.find('[role="status"]').text()).toContain('Guardando…')
  })
})
