<script setup>
defineProps({
  texto: { type: String, default: 'Cargando...' },
})
</script>

<template>
  <div class="d-flex align-items-center gap-2 my-3" role="status">
    <span class="spinner-border spinner-border-sm" aria-hidden="true"></span>
    <span>{{ texto }}</span>
  </div>
</template>
