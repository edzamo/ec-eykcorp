import { ref } from 'vue'

/**
 * Estado y operaciones de clientes. Contrato de errores (intencionalmente distinto por operación):
 *  - `crear` y `actualizar` LANZAN el error (ApiError) al llamador, que lo muestra junto al
 *    formulario (400 con errores por campo, 409 conflicto). No tocan `error`.
 *  - `cargar` y `eliminar` NO lanzan: guardan el error en `error` para mostrarlo como alerta de página.
 * @param {{listar: Function, crear: Function, actualizar: Function, eliminar: Function}} clienteService
 */
export function useClientes(clienteService) {
  const clientes = ref([])
  const cargando = ref(false)
  const error = ref(null)

  /** No lanza: ante fallo deja el error en `error`. */
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

  /** Lanza ante fallo (el llamador muestra 400/409); no usa `error`. */
  async function crear(datos) {
    const creado = await clienteService.crear(datos)
    clientes.value = [...clientes.value, creado]
    return creado
  }

  /** Lanza ante fallo (el llamador muestra 400/409); no usa `error`. */
  async function actualizar(id, datos) {
    const actualizado = await clienteService.actualizar(id, datos)
    clientes.value = clientes.value.map((c) => (c.id === id ? actualizado : c))
    return actualizado
  }

  /** No lanza: ante fallo deja el error en `error`. */
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
