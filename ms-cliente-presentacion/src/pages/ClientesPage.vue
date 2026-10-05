<script setup>
import { computed, inject, onMounted, ref } from 'vue'
import { useClientes } from '../domain/useClientes.js'
import ClienteTable from '../components/ClienteTable.vue'
import ClienteForm from '../components/ClienteForm.vue'
import ConfirmDialog from '../components/ConfirmDialog.vue'
import LoadingSpinner from '../components/LoadingSpinner.vue'
import ErrorAlert from '../components/ErrorAlert.vue'

const { clientes, cargando, error, cargar, crear, actualizar, eliminar } = useClientes(
  inject('clienteService'),
)

const formularioAbierto = ref(false)
const editando = ref(null)
const guardando = ref(false)
const erroresCampo = ref({})
const errorForm = ref('')
const porEliminar = ref(null)

const mensajeError = computed(() =>
  error.value ? error.value.detail || error.value.title || 'Error inesperado' : '',
)
const tituloForm = computed(() => (editando.value ? 'Editar cliente' : 'Nuevo cliente'))

onMounted(cargar)

function abrirNuevo() {
  editando.value = null
  abrirForm()
}
function abrirEditar(cliente) {
  editando.value = cliente
  abrirForm()
}
function abrirForm() {
  erroresCampo.value = {}
  errorForm.value = ''
  formularioAbierto.value = true
}

async function guardar(datos) {
  guardando.value = true
  erroresCampo.value = {}
  errorForm.value = ''
  try {
    if (editando.value) await actualizar(editando.value.id, datos)
    else await crear(datos)
    formularioAbierto.value = false
  } catch (e) {
    erroresCampo.value = e.errores ?? {}
    errorForm.value = Object.keys(erroresCampo.value).length ? '' : e.detail || e.title || 'Error inesperado'
  } finally {
    guardando.value = false
  }
}

async function confirmarEliminar() {
  const cliente = porEliminar.value
  porEliminar.value = null
  await eliminar(cliente.id)
}
</script>

<template>
  <section>
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h2 class="h4 m-0">Listado de clientes</h2>
      <button v-if="!formularioAbierto" type="button" class="btn btn-primary" @click="abrirNuevo">
        Nuevo cliente
      </button>
    </div>

    <ErrorAlert v-if="mensajeError" :mensaje="mensajeError" />
    <LoadingSpinner v-if="cargando" />

    <div v-if="formularioAbierto" class="card card-body mb-4">
      <h3 class="h5">{{ tituloForm }}</h3>
      <ErrorAlert v-if="errorForm" :mensaje="errorForm" />
      <ClienteForm
        :key="editando?.id ?? 'nuevo'"
        :cliente="editando"
        :errores="erroresCampo"
        :guardando="guardando"
        @guardar="guardar"
        @cancelar="formularioAbierto = false"
      />
    </div>

    <ClienteTable
      v-if="!cargando"
      :clientes="clientes"
      @editar="abrirEditar"
      @eliminar="porEliminar = $event"
    />

    <ConfirmDialog
      v-if="porEliminar"
      titulo="Eliminar cliente"
      :mensaje="`¿Eliminar a ${porEliminar.nombres} ${porEliminar.apellidos}?`"
      @confirmar="confirmarEliminar"
      @cancelar="porEliminar = null"
    />
  </section>
</template>
