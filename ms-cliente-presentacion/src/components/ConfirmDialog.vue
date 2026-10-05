<script setup>
import { onBeforeUnmount, onMounted, ref, useId } from 'vue'

const props = defineProps({
  titulo: { type: String, required: true },
  mensaje: { type: String, required: true },
  textoConfirmar: { type: String, default: 'Eliminar' },
})
const emit = defineEmits(['confirmar', 'cancelar'])

const FOCUSABLES =
  'button:not([disabled]), [href], input:not([disabled]), select, textarea, [tabindex]:not([tabindex="-1"])'
const tituloId = useId()
const raiz = ref(null)
const cancelarBtn = ref(null)
// Elemento que tenía el foco al abrirse: se restaura al cerrarse.
const opener = document.activeElement

onMounted(() => cancelarBtn.value?.focus())
onBeforeUnmount(() => {
  if (opener instanceof HTMLElement && opener.isConnected) opener.focus()
})

// Trampa de foco: Tab / Shift+Tab ciclan dentro del diálogo.
function onKeydown(e) {
  if (e.key === 'Escape') {
    emit('cancelar')
    return
  }
  if (e.key !== 'Tab') return
  const items = [...raiz.value.querySelectorAll(FOCUSABLES)]
  const primero = items[0]
  const ultimo = items[items.length - 1]
  if (e.shiftKey && document.activeElement === primero) {
    e.preventDefault()
    ultimo.focus()
  } else if (!e.shiftKey && document.activeElement === ultimo) {
    e.preventDefault()
    primero.focus()
  }
}
</script>

<template>
  <div
    ref="raiz"
    class="modal d-block bg-dark bg-opacity-50"
    tabindex="-1"
    role="dialog"
    aria-modal="true"
    :aria-labelledby="tituloId"
    @keydown="onKeydown"
  >
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h2 :id="tituloId" class="modal-title fs-5">{{ props.titulo }}</h2>
        </div>
        <div class="modal-body">{{ props.mensaje }}</div>
        <div class="modal-footer">
          <button ref="cancelarBtn" type="button" class="btn btn-secondary" @click="emit('cancelar')">
            Cancelar
          </button>
          <button type="button" class="btn btn-danger" @click="emit('confirmar')">
            {{ props.textoConfirmar }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
