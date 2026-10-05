import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import App from '../../src/App.vue'

// Requisito cambiado (E2): App ahora contiene <router-view> y se monta con router;
// el <h1> "Clientes" se conserva.
describe('App', () => {
  it('debe_renderizar_un_encabezado_de_nivel_1_con_el_texto_Clientes', () => {
    const router = createRouter({ history: createMemoryHistory(), routes: [] })
    const wrapper = mount(App, { global: { plugins: [router] } })

    const headings = wrapper.findAll('h1')

    expect(headings).toHaveLength(1)
    expect(headings[0].text()).toBe('Clientes')
  })
})
