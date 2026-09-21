# Chat Customer Web Component

Ứng dụng Web Chat và Widget dành cho Khách hàng. Hỗ trợ cả **Browser Widget Mode** và **WebView Mode** cho ứng dụng di động Native.

## 🚀 Tính năng

- 💬 **Customer Chat Widget**: Nút bong bóng floating mở widget chat góc dưới màn hình.
- 📱 **WebView Mode Support**: Tự động chuyển giao diện full-screen khi truy cập qua URL parameter `?mode=webview`.
- ⚡ **Realtime Messaging**: Kết nối WebSocket (STOMP over SockJS) đến `chat-realtime-service`.
- 🔄 **REST API Backup**: Tự động fallback và đồng bộ tin nhắn với `chat-core-service`.

## 🛠️ Chạy ứng dụng

```bash
# Cài đặt thư viện
npm install

# Khởi chạy dev server (Port 4201 hoặc port khả dụng)
npm start
```

### URLs Thử nghiệm:
- **Browser Widget Mode**: `http://localhost:4201`
- **WebView Mobile Mode**: `http://localhost:4201/?mode=webview`
