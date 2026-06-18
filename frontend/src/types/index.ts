/** 统一API响应 */
export interface ResultVO<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: number
}

/** 分页响应 */
export interface PageVO<T> {
  total: number
  page: number
  size: number
  items: T[]
}

/** 用户信息 */
export interface UserInfo {
  id: number
  studentId: string
  username: string
  avatar: string | null
  email?: string
  phone?: string
  reputationScore: number
  role: 'USER' | 'ADMIN'
  status: string
}

/** 商品卡片 */
export interface ProductCard {
  id: number
  title: string
  type: 'SALE' | 'AUCTION'
  fixedPrice: number | null
  startPrice: number | null
  currentPrice: number | null
  coverImage: string | null
  viewCount: number
  status: string
  sellerName: string
  createdAt: string
  auctionEnd: string | null
}

/** WebSocket消息 */
export interface WSMessage {
  type: string
  payload: Record<string, unknown>
  timestamp: number
}
