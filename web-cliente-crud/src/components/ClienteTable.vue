<script setup>
defineProps({
  clientes: {
    type: Array,
    required: true,
    validator: (lista) => lista.every((c) => c !== null && typeof c === 'object' && 'id' in c),
  },
})
defineEmits(['editar', 'eliminar'])
</script>

<template>
  <p v-if="clientes.length === 0">No hay clientes registrados.</p>
  <table v-else class="table table-striped align-middle">
    <thead>
      <tr>
        <th scope="col">Nombres</th>
        <th scope="col">Apellidos</th>
        <th scope="col">Correo</th>
        <th scope="col">Teléfono</th>
        <th scope="col">Acciones</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="c in clientes" :key="c.id">
        <td>{{ c.nombres }}</td>
        <td>{{ c.apellidos }}</td>
        <td>{{ c.correo }}</td>
        <td>{{ c.telefono }}</td>
        <td class="text-nowrap">
          <button
            type="button"
            class="btn btn-sm btn-outline-primary me-2"
            :aria-label="`Editar ${c.nombres} ${c.apellidos}`"
            @click="$emit('editar', c)"
          >
            Editar
          </button>
          <button
            type="button"
            class="btn btn-sm btn-outline-danger"
            :aria-label="`Eliminar ${c.nombres} ${c.apellidos}`"
            @click="$emit('eliminar', c)"
          >
            Eliminar
          </button>
        </td>
      </tr>
    </tbody>
  </table>
</template>
