import request from './request'

export const suppliersAPI = {
  list(params) { return request.get('/suppliers', { params }) },
  get(id) { return request.get(`/suppliers/${id}`) },
  create(data) { return request.post('/suppliers', data) },
  update(id, data) { return request.put(`/suppliers/${id}`, data) },
  delete(id) { return request.delete(`/suppliers/${id}`) },
}

export const purchaseOrdersAPI = {
  list(params) { return request.get('/purchase-orders', { params }) },
  create(data) { return request.post('/purchase-orders', data) },
  update(id, data) { return request.put(`/purchase-orders/${id}`, data) },
  delete(id) { return request.delete(`/purchase-orders/${id}`) },
}
