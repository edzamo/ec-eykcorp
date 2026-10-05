export function createClienteService(http) {
  return {
    listar: () => http.get('/clientes'),
    obtener: (id) => http.get(`/clientes/${id}`),
    crear: (cliente) => http.post('/clientes', cliente),
    actualizar: (id, cliente) => http.put(`/clientes/${id}`, cliente),
    eliminar: (id) => http.delete(`/clientes/${id}`),
  }
}
