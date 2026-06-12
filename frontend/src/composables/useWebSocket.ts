import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useUserStore } from '@/stores/useUserStore'
import { logger } from '@/utils/logger'
import type { WSMessage } from '@/types'

export function useWebSocket() {
  const connected = ref(false)
  let client: Client | null = null

  function connect(onMessage: (msg: WSMessage) => void) {
    const userStore = useUserStore()
    client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {
        Authorization: `Bearer ${userStore.accessToken}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        connected.value = true
        logger.info('WebSocket已连接')
      },
      onDisconnect: () => {
        connected.value = false
        logger.info('WebSocket已断开')
      },
    })

    client.onStompError = (frame) => {
      logger.error('STOMP错误:', frame.headers['message'])
    }

    client.activate()
  }

  function subscribe(topic: string, callback: (msg: WSMessage) => void) {
    if (!client) return null
    return client.subscribe(topic, (message) => {
      try {
        const body = JSON.parse(message.body) as WSMessage
        callback(body)
      } catch {
        logger.error('WebSocket消息解析失败:', message.body)
      }
    })
  }

  function disconnect() {
    if (client) {
      client.deactivate()
      client = null
      connected.value = false
    }
  }

  onUnmounted(() => disconnect())

  return { connected, connect, subscribe, disconnect }
}
