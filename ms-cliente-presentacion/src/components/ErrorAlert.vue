<script setup>
defineProps({
  mensaje: { type: String, required: true },
})
</script>

<template>
  <div class="alert alert-danger" role="alert">{{ mensaje }}</div>
</template>
