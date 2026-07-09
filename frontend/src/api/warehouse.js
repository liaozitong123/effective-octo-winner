import request from './request'

export const inventoryAPI = {
  list(params) { return request.get('/inventory', { params }) },
  create(data) { return request.post('/inventory', data) },
  update(id, data) { return request.put(`/inventory/${id}`, data) },
  delete(id) { return request.delete(`/inventory/${id}`) },
}
