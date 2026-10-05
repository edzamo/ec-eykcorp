import { ref, computed } from 'vue'

const KEY = 'token'

export function createAuth({ authService, storage = globalThis.sessionStorage }) {
  const token = ref(storage?.getItem(KEY) ?? null)

  async function login(usuario, password) {
    const res = await authService.login(usuario, password)
    token.value = res.token
    storage?.setItem(KEY, res.token)
  }

  function logout() {
    token.value = null
    storage?.removeItem(KEY)
  }

  return {
    estaAutenticado: computed(() => Boolean(token.value)),
    getToken: () => token.value,
    login,
    logout,
  }
}

export const useAuth = createAuth
