<script setup>
import { reactive, ref, useId } from 'vue'

const props = defineProps({
  cliente: { type: Object, default: null },
  errores: { type: Object, default: () => ({}) },
  guardando: { type: Boolean, default: false },
})
const emit = defineEmits(['guardar', 'cancelar'])

// La validación local es solo de UX; el backend es la fuente de verdad (FE-01).
const EMAIL_UX = /^\S+@\S+\.\S+$/
const idBase = useId()
const campos = [
  { name: 'nombres', label: 'Nombres', type: 'text' },
  { name: 'apellidos', label: 'Apellidos', type: 'text' },
  { name: 'correo', label: 'Correo', type: 'email' },
  { name: 'telefono', label: 'Teléfono', type: 'tel' },
]
const form = reactive({
  nombres: props.cliente?.nombres ?? '',
  apellidos: props.cliente?.apellidos ?? '',
  correo: props.cliente?.correo ?? '',
  telefono: props.cliente?.telefono ?? '',
})
const local = ref({})

function validar() {
  const e = {}
  if (!form.nombres.trim()) e.nombres = 'Los nombres son obligatorios'
  if (!form.apellidos.trim()) e.apellidos = 'Los apellidos son obligatorios'
  if (!form.correo.trim()) e.correo = 'El correo es obligatorio'
  else if (!EMAIL_UX.test(form.correo)) e.correo = 'Ingrese un correo válido'
  return e
}

function enviar() {
  local.value = validar()
  if (Object.keys(local.value).length === 0) emit('guardar', { ...form })
}

const mensaje = (name) => local.value[name] ?? props.errores[name]
</script>

<template>
  <form novalidate @submit.prevent="enviar">
    <div v-for="f in campos" :key="f.name" class="mb-3">
      <label :for="`${idBase}-${f.name}`" class="form-label">{{ f.label }}</label>
      <input
        :id="`${idBase}-${f.name}`"
        v-model="form[f.name]"
        :type="f.type"
        class="form-control"
        :aria-invalid="mensaje(f.name) ? 'true' : undefined"
        :aria-describedby="mensaje(f.name) ? `${idBase}-${f.name}-err` : undefined"
      />
      <div v-if="mensaje(f.name)" :id="`${idBase}-${f.name}-err`" class="text-danger small" role="alert">
        {{ mensaje(f.name) }}
      </div>
    </div>
    <button type="submit" class="btn btn-primary me-2" :disabled="guardando">Guardar</button>
    <button type="button" class="btn btn-secondary" @click="emit('cancelar')">Cancelar</button>
  </form>
</template>
