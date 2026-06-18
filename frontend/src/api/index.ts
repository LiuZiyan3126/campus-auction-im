import request from './request'
export { request }

// Auth API
export const authApi = {
  login: (data: { studentId: string; password: string }) => request.post('/auth/login', data),
  register: (data: { studentId: string; email: string; password: string; phone?: string }) => request.post('/auth/register', data),
  refresh: (token: string) => request.post('/auth/refresh', token),
}

// User API
export const userApi = {
  getMyProfile: () => request.get('/users/me'),
  updateMyProfile: (data: unknown) => request.put('/users/me', data),
  getUserProfile: (id: number) => request.get(`/users/${id}`),
}

// Product API
export const productApi = {
  list: (params: Record<string, unknown>) => request.get('/products', { params }),
  create: (formData: FormData) => request.post('/products', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  getById: (id: number) => request.get(`/products/${id}`),
  update: (id: number, data: unknown) => request.put(`/products/${id}`, data),
  search: (params: Record<string, unknown>) => request.get('/products/search', { params }),
  toggleFavorite: (id: number) => request.post(`/products/${id}/favorite`),
  myFavorites: (params: Record<string, unknown>) => request.get('/products/favorites', { params }),
  myProducts: (params: Record<string, unknown>) => request.get('/products/my', { params }),
  delete: (id: number) => request.delete(`/products/${id}`),
}

// Auction API
export const auctionApi = {
  placeBid: (productId: number, amount: number) => request.post(`/products/${productId}/bids`, { bidAmount: amount }),
  getBids: (productId: number, params: Record<string, unknown>) => request.get(`/products/${productId}/bids`, { params }),
  getCurrentPrice: (productId: number) => request.get(`/products/${productId}/bids/current-price`),
}
