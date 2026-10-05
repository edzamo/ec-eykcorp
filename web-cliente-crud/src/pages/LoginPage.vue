<script setup>
import { inject, reactive, ref, useId } from 'vue'
import { useRouter } from 'vue-router'
import ErrorAlert from '../components/ErrorAlert.vue'

const auth = inject('auth')
const router = useRouter()
const idBase = useId()
const form = reactive({ usuario: '', password: '' })
const error = ref('')
const enviando = ref(false)

async function enviar() {
  error.value = ''
  if (!form.usuario.trim() || !form.password) {
    error.value = 'Ingrese usuario y contraseña'
    return
  }
  enviando.value = true
  try {
    await auth.login(form.usuario, form.password)
    await router.push('/')
  } catch (e) {
    error.value = e?.status === 401 ? 'Usuario o contraseña incorrectos' : 'No se pudo iniciar sesión'
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <main class="container py-5" style="max-width: 420px">
    <h2 class="h4 mb-3">Iniciar sesión</h2>
    <ErrorAlert v-if="error" :mensaje="error" />
    <form novalidate @submit.prevent="enviar">
      <div class="mb-3">
        <label :for="`${idBase}-u`" class="form-label">Usuario</label>
        <input
          :id="`${idBase}-u`"
          v-model="form.usuario"
          type="text"
          class="form-control"
          autocomplete="username"
        />
      </div>
      <div class="mb-3">
        <label :for="`${idBase}-p`" class="form-label">Contraseña</label>
        <input
          :id="`${idBase}-p`"
          v-model="form.password"
          type="password"
          class="form-control"
          autocomplete="current-password"
        />
      </div>
      <button type="submit" class="btn btn-primary" :disabled="enviando">Ingresar</button>
    </form>
  </main>
</template>
