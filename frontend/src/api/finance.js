import request from './request'

export const paymentsAPI = {
  list(params) { return request.get('/payments', { params }) },
  summary(params) { return request.get('/payments/summary', { params }) },
  create(data) { return request.post('/payments', data) },
  update(id, data) { return request.put(`/payments/${id}`, data) },
  delete(id) { return request.delete(`/payments/${id}`) },
}

export const reconciliationsAPI = {
  list(params) { return request.get('/reconciliations', { params }) },
  create(data) { return request.post('/reconciliations', data) },
  update(id, data) { return request.put(`/reconciliations/${id}`, data) },
  delete(id) { return request.delete(`/reconciliations/${id}`) },
}

export const salaryRecordsAPI = {
  list(params) { return request.get('/salary-records', { params }) },
  create(data) { return request.post('/salary-records', data) },
  update(id, data) { return request.put(`/salary-records/${id}`, data) },
  delete(id) { return request.delete(`/salary-records/${id}`) },
}

export const dailyExpensesAPI = {
  list(params) { return request.get('/daily-expenses', { params }) },
  create(data) { return request.post('/daily-expenses', data) },
  update(id, data) { return request.put(`/daily-expenses/${id}`, data) },
  delete(id) { return request.delete(`/daily-expenses/${id}`) },
}

export const financeAPI = {
  profitAnalysis(params) { return request.get('/finance/profit-analysis', { params }) },
  payableReceivable() { return request.get('/finance/payable-receivable') },
}
