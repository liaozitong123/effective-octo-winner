import request from './request'

export const productionOrdersAPI = {
  list(params) { return request.get('/production-orders', { params }) },
  get(id) { return request.get(`/production-orders/${id}`) },
  create(data) { return request.post('/production-orders', data) },
  update(id, data) { return request.put(`/production-orders/${id}`, data) },
  markPrinted(id) { return request.post(`/production-orders/${id}/mark-printed`) },
  delete(id) { return request.delete(`/production-orders/${id}`) },
  progress(id) { return request.get(`/production-orders/${id}/progress`) },
}

export const productionRecordsAPI = {
  list(params) { return request.get('/production-records', { params }) },
  create(data) { return request.post('/production-records', data) },
  update(id, data) { return request.put(`/production-records/${id}`, data) },
  delete(id) { return request.delete(`/production-records/${id}`) },
}
