<script setup>
import { onMounted, ref, useId } from 'vue'

defineProps({
  titulo: { type: String, required: true },
  mensaje: { type: String, required: true },
  textoConfirmar: { type: String, default: 'Eliminar' },
})
defineEmits(['confirmar', 'cancelar'])

const tituloId = useId()
const cancelarBtn = ref(null)
onMounted(() => cancelarBtn.value?.focus())
</script>

<template>
  <div class="modal d-block bg-dark bg-opacity-50" tabindex="-1">
    <div
      class="modal-dialog modal-dialog-centered"
      role="dialog"
      aria-modal="true"
      :aria-labelledby="tituloId"
      @keydown.esc="$emit('cancelar')"
    >
      <div class="modal-content">
        <div class="modal-header">
          <h2 :id="tituloId" class="modal-title fs-5">{{ titulo }}</h2>
        </div>
        <div class="modal-body">{{ mensaje }}</div>
        <div class="modal-footer">
          <button ref="cancelarBtn" type="button" class="btn btn-secondary" @click="$emit('cancelar')">
            Cancelar
          </button>
          <button type="button" class="btn btn-danger" @click="$emit('confirmar')">
            {{ textoConfirmar }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
