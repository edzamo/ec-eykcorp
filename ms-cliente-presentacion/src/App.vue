<script setup>
import { inject } from 'vue'
import { useRouter } from 'vue-router'
import { AUTH_KEY } from './app/keys.js'

const auth = inject(AUTH_KEY, null)
const router = useRouter()

async function cerrarSesion() {
  auth.logout()
  await router.push('/login')
}
</script>

<template>
  <div class="container py-4">
    <header class="d-flex justify-content-between align-items-center mb-4">
      <h1>Clientes</h1>
      <button
        v-if="auth?.estaAutenticado.value"
        type="button"
        class="btn btn-outline-secondary"
        @click="cerrarSesion"
      >
        Cerrar sesión
      </button>
    </header>
    <main>
      <router-view />
    </main>
  </div>
</template>
