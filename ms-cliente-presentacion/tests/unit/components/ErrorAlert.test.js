import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ErrorAlert from '../../../src/components/ErrorAlert.vue'

describe('ErrorAlert', () => {
  it('debe_exponer_role_alert_con_el_mensaje', () => {
    const w = mount(ErrorAlert, { props: { mensaje: 'Algo falló' } })
    expect(w.find('[role="alert"]').text()).toContain('Algo falló')
  })
  it('debe_escapar_html_del_mensaje', () => {
    const w = mount(ErrorAlert, { props: { mensaje: '<b>x</b>' } })
    expect(w.find('b').exists()).toBe(false)
    expect(w.text()).toContain('<b>x</b>')
  })
})
