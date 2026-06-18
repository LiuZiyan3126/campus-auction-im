import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useWebSocket } from './useWebSocket'
import request from '@/api/request'
import type { ResultVO, WSMessage } from '@/types'
import { logger } from '@/utils/logger'

export interface BidRecord {
  id: number
  productId: number
  bidderId: number
  bidderName: string
  bidAmount: number
  bidTime: string
}

export function useAuction(productId: number) {
  const currentPrice = ref(0)
  const bids = ref<BidRecord[]>([])
  const auctionEnd = ref<string | null>(null)
  const winner = ref<{ winnerId: number; winnerName: string; finalPrice: number } | null>(null)
  const expired = ref(false)
  const bidding = ref(false)

  const { connected, connect, subscribe, disconnect } = useWebSocket()

  async function fetchCurrentPrice() {
    try {
      const res = await request.get<ResultVO<number>>(`/products/${productId}/bids/current-price`)
      currentPrice.value = res.data.data
    } catch {
      logger.error('获取当前价失败')
    }
  }

  async function fetchBidHistory() {
    try {
      const res = await request.get<ResultVO<{ items: BidRecord[] }>>(`/products/${productId}/bids`)
      bids.value = res.data.data?.items || []
    } catch {
      logger.error('获取出价记录失败')
    }
  }

  async function placeBid(amount: number) {
    if (bidding.value) return
    if (!amount || amount <= 0) {
      ElMessage.warning('请输入有效的出价金额')
      return
    }
    bidding.value = true
    try {
      await request.post(`/products/${productId}/bids`, { bidAmount: amount })
      ElMessage.success('出价成功')
      currentPrice.value = amount
      bids.value.unshift({
        id: 0,
        productId: productId,
        bidderId: 0,
        bidderName: '我',
        bidAmount: amount,
        bidTime: new Date().toISOString(),
      })
    } catch (e: unknown) {
      const err = e as { response?: { data?: { message?: string } } }
      ElMessage.error(err.response?.data?.message || '出价失败')
    } finally {
      setTimeout(() => { bidding.value = false }, 1000)
    }
  }

  function connectAuction() {
    connect(() => {})
    subscribe(`/topic/auction/${productId}`, (msg: WSMessage) => {
      if (msg.type === 'NEW_BID') {
        const p = msg.payload as Record<string, unknown>
        const newBidAmount = Number(p.bidAmount)
        currentPrice.value = Number(p.currentPrice || p.bidAmount)
        // 去重：避免本地已插入的出价重复
        if (!bids.value.some(b => b.bidAmount === newBidAmount && b.bidderName === '我')) {
          bids.value.unshift({
            id: 0,
            productId: productId,
            bidderId: Number(p.bidderId),
            bidderName: String(p.bidderName || ''),
            bidAmount: newBidAmount,
            bidTime: String(p.bidTime || ''),
          })
        }
      } else if (msg.type === 'AUCTION_END') {
        const p = msg.payload as Record<string, unknown>
        winner.value = {
          winnerId: Number(p.winnerId),
          winnerName: String(p.winnerName),
          finalPrice: Number(p.finalPrice),
        }
      } else if (msg.type === 'AUCTION_EXPIRED') {
        expired.value = true
      }
    })
  }

  return {
    currentPrice, bids, auctionEnd, winner, expired, bidding, connected,
    fetchCurrentPrice, fetchBidHistory, placeBid, connectAuction, disconnect,
  }
}
