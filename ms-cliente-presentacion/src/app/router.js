import { createRouter, createWebHistory } from 'vue-router'
import ClientesPage from '../pages/ClientesPage.vue'
import LoginPage from '../pages/LoginPage.vue'

export function createAppRouter({ auth, history = createWebHistory() }) {
  const router = createRouter({
    history,
    routes: [
      { path: '/', component: ClientesPage, meta: { requiereAuth: true } },
      { path: '/login', component: LoginPage },
      { path: '/:pathMatch(.*)*', redirect: '/' },
    ],
  })
  router.beforeEach((to) => {
    if (to.meta.requiereAuth && !auth.estaAutenticado.value) return '/login'
    if (to.path === '/login' && auth.estaAutenticado.value) return '/'
  })
  return router
}
