import request from './request'

export function login(username, password) {
  return request.post('/login', { username, password })
}

export function getCurrentUser() {
  return request.get('/current-user')
}

export function logout() {
  return request.post('/logout', null, { skipAuthRedirect: true })
}

export function getDashboard() {
  return request.get('/dashboard')
}
