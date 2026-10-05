import { ref } from 'vue'

export function useClientes(clienteService) {
  const clientes = ref([])
  const cargando = ref(false)
  const error = ref(null)

  async function cargar() {
    cargando.value = true
    error.value = null
    try {
      clientes.value = await clienteService.listar()
    } catch (e) {
      error.value = e
    } finally {
      cargando.value = false
    }
  }

  async function crear(datos) {
    const creado = await clienteService.crear(datos)
    clientes.value = [...clientes.value, creado]
    return creado
  }

  async function actualizar(id, datos) {
    const actualizado = await clienteService.actualizar(id, datos)
    clientes.value = clientes.value.map((c) => (c.id === id ? actualizado : c))
    return actualizado
  }

  async function eliminar(id) {
    error.value = null
    try {
      await clienteService.eliminar(id)
      clientes.value = clientes.value.filter((c) => c.id !== id)
    } catch (e) {
      error.value = e
    }
  }

  return { clientes, cargando, error, cargar, crear, actualizar, eliminar }
}
