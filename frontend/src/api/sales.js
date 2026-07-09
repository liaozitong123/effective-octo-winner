import request from './request'

export const customersAPI = {
  list(params) { return request.get('/customers', { params }) },
  get(id) { return request.get(`/customers/${id}`) },
  create(data) { return request.post('/customers', data) },
  update(id, data) { return request.put(`/customers/${id}`, data) },
  delete(id) { return request.delete(`/customers/${id}`) },
}

export const salesOrdersAPI = {
  list(params) { return request.get('/sales-orders', { params }) },
  get(id) { return request.get(`/sales-orders/${id}`) },
  create(data) { return request.post('/sales-orders', data) },
  update(id, data) { return request.put(`/sales-orders/${id}`, data) },
  delete(id) { return request.delete(`/sales-orders/${id}`) },
}

export const deliveryNotesAPI = {
  list(params) { return request.get('/delivery-notes', { params }) },
  create(data) { return request.post('/delivery-notes', data) },
  update(id, data) { return request.put(`/delivery-notes/${id}`, data) },
  delete(id) { return request.delete(`/delivery-notes/${id}`) },
}
