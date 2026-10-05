import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import App from '../../src/App.vue'

describe('App', () => {
  it('debe_renderizar_un_encabezado_de_nivel_1_con_el_texto_Clientes', () => {
    const wrapper = mount(App)

    // <h1> es el elemento con rol implícito "heading" nivel 1; Testing Library no está
    // instalada, por lo que se consulta por etiqueta semántica y texto accesible.
    const headings = wrapper.findAll('h1')

    expect(headings).toHaveLength(1)
    expect(headings[0].text()).toBe('Clientes')
  })
})
