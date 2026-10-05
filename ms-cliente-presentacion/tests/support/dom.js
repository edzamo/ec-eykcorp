export const campo = (wrapper, etiqueta) => {
  const label = wrapper.findAll('label').find((l) => l.text().startsWith(etiqueta))
  if (!label) throw new Error(`No hay etiqueta ${etiqueta}`)
  return wrapper.find(`#${label.attributes('for')}`)
}
export const boton = (wrapper, texto) => {
  const b = wrapper.findAll('button').find((x) => (x.attributes('aria-label') ?? x.text()).includes(texto))
  if (!b) throw new Error(`No hay botón ${texto}`)
  return b
}
