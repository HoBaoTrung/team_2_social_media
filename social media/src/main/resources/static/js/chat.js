// ================ UPDATED chat.js ================

let openChats = new Map();
let chatBubbles = new Map();
const maxChats = 3;

// Initialize chat system
document.addEventListener("DOMContentLoaded", function() {
    initChatSelectors();
    setupChatContainer();
});

function setupChatContainer() {
    const chatContainer = document.getElementById("chatContainer");
    if (!chatContainer) {
        // Create chat container if it doesn't exist
        const container = document.createElement('div');
        container.id = 'chatContainer';
        container.className = 'chat-container';
        document.body.appendChild(container);
    }
}

function openChat(userId, userName, avatar) {
    // Check if chat is already open
    if (openChats.has(userId)) {
        const existingChat = openChats.get(userId);
        existingChat.style.display = 'flex';
        // Remove bubble if exists
        if (chatBubbles.has(userId)) {
            chatBubbles.get(userId).remove();
            chatBubbles.delete(userId);
        }
        return;
    }

    // Check if we need to minimize oldest chat
    if (openChats.size >= maxChats) {
        const oldestChatId = Array.from(openChats.keys())[0];
        minimizeChat(oldestChatId);
    }

    // Create new chat window
    const chatWindow = createChatWindow(userId, userName, avatar);
    document.getElementById('chatContainer').appendChild(chatWindow);
    openChats.set(userId, chatWindow);
}

function createChatWindow(userId, userName, avatar) {
    const chatWindow = document.createElement('div');
    chatWindow.className = 'chat-window';
    chatWindow.id = `chat-${userId}`;

    chatWindow.innerHTML = `
        <div class="chat-header">
            <div class="chat-user-info">
                <img src="${avatar || '/images/default-avatar.jpg'}" alt="${userName}" class="chat-avatar">
                <div>
                    <div class="chat-name">${userName}</div>
                    <div class="chat-status">Đang hoạt động</div>
                </div>
            </div>
            <div class="chat-controls">
                <button class="chat-btn" onclick="toggleVideo('${userId}')">
                    <i class="fas fa-video"></i>
                </button>
                <button class="chat-btn" onclick="minimizeChat('${userId}')">
                    <i class="fas fa-minus"></i>
                </button>
                <button class="chat-btn" onclick="closeChat('${userId}')">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        </div>
        <div class="chat-messages" id="messages-${userId}">
            <div class="message-time">Hôm nay</div>
            <div class="message received">
                <img src="${avatar || '/images/default-avatar.jpg'}" alt="${userName}" class="message-avatar">
                <div class="message-content">Chào bạn! Bạn có khỏe không? 😊</div>
            </div>
            <div class="message sent">
                <div class="message-content">Chào! Mình khỏe, cảm ơn bạn!</div>
            </div>
            <div class="message received">
                <img src="${avatar || '/images/default-avatar.jpg'}" alt="${userName}" class="message-avatar">
                <div class="message-content">Tuyệt vời! Hôm nay bạn có rảnh không?</div>
            </div>
        </div>
        <div class="chat-input">
            <div class="input-container">
                <input type="text" placeholder="Nhập tin nhắn..." id="input-${userId}" onkeypress="handleKeyPress(event, '${userId}')">
                <div class="input-icons">
                    <i class="far fa-smile input-icon" onclick="toggleEmoji('${userId}')"></i>
                    <i class="fas fa-paperclip input-icon" onclick="attachFile('${userId}')"></i>
                    <i class="fas fa-paper-plane input-icon" onclick="sendMessage('${userId}')"></i>
                </div>
            </div>
        </div>
    `;

    // Auto scroll to bottom
    setTimeout(() => {
        const messagesContainer = chatWindow.querySelector('.chat-messages');
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }, 100);

    return chatWindow;
}

function minimizeChat(userId) {
    const chatWindow = openChats.get(userId);
    if (chatWindow) {
        chatWindow.style.display = 'none';

        // Create bubble
        const bubble = createChatBubble(userId, chatWindow);
        document.getElementById('chatContainer').appendChild(bubble);
        chatBubbles.set(userId, bubble);

        // Don't remove from openChats, just hide
    }
}

function createChatBubble(userId, chatWindow) {
    const bubble = document.createElement('div');
    bubble.className = 'chat-bubble';
    bubble.onclick = () => restoreChat(userId);

    const userName = chatWindow.querySelector('.chat-name').textContent;
    const avatar = chatWindow.querySelector('.chat-avatar').src;

    bubble.innerHTML = `
        <img src="${avatar}" alt="${userName}">
        <div class="online-indicator"></div>
    `;

    return bubble;
}

function restoreChat(userId) {
    const chatWindow = openChats.get(userId);
    const bubble = chatBubbles.get(userId);

    if (chatWindow && bubble) {
        // Check if we need to minimize another chat
        const visibleChats = Array.from(openChats.values()).filter(chat => chat.style.display !== 'none');
        if (visibleChats.length >= maxChats) {
            const oldestVisible = visibleChats[0];
            const oldestId = oldestVisible.id.replace('chat-', '');
            minimizeChat(oldestId);
        }

        chatWindow.style.display = 'flex';
        bubble.remove();
        chatBubbles.delete(userId);
    }
}

function closeChat(userId) {
    const chatWindow = openChats.get(userId);
    const bubble = chatBubbles.get(userId);

    if (chatWindow) {
        chatWindow.remove();
        openChats.delete(userId);
    }

    if (bubble) {
        bubble.remove();
        chatBubbles.delete(userId);
    }
}

function sendMessage(userId) {
    const input = document.getElementById(`input-${userId}`);
    const message = input.value.trim();

    if (message) {
        const messagesContainer = document.getElementById(`messages-${userId}`);
        const messageDiv = document.createElement('div');
        messageDiv.className = 'message sent';
        messageDiv.innerHTML = `<div class="message-content">${message}</div>`;

        messagesContainer.appendChild(messageDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;

        input.value = '';

        // Send message to server via WebSocket or AJAX
        sendMessageToServer(userId, message);

        // Simulate response (remove this when implementing real chat)
        setTimeout(() => {
            const responses = [
                "Cảm ơn tin nhắn của bạn! 😊",
                "Mình hiểu rồi!",
                "Sounds good!",
                "OK nhé!",
                "Được rồi 👍"
            ];
            const randomResponse = responses[Math.floor(Math.random() * responses.length)];

            const responseDiv = document.createElement('div');
            responseDiv.className = 'message received';
            const avatar = document.querySelector(`#chat-${userId} .chat-avatar`).src;
            responseDiv.innerHTML = `
                <img src="${avatar}" alt="Avatar" class="message-avatar">
                <div class="message-content">${randomResponse}</div>
            `;
            messagesContainer.appendChild(responseDiv);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }, 1000 + Math.random() * 2000);
    }
}

function sendMessageToServer(userId, message) {
    // Implement WebSocket message sending here
    // Example:
    /*
    if (typeof stompClient !== 'undefined' && stompClient.connected) {
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify({
            senderId: getCurrentUserId(),
            receiverId: userId,
            content: message,
            type: 'TEXT'
        }));
    }
    */
}

function handleKeyPress(event, userId) {
    if (event.key === 'Enter') {
        sendMessage(userId);
    }
}

function toggleVideo(userId) {
    alert('Tính năng video call sẽ sớm có!');
}

function toggleEmoji(userId) {
    alert('Tính năng emoji sẽ sớm có!');
}

function attachFile(userId) {
    alert('Tính năng đính kèm file sẽ sớm có!');
}

// Initialize chat selectors for friend items
function initChatSelectors() {
    document.querySelectorAll(".friend-item, .group-item").forEach(item => {
        item.addEventListener("click", () => {
            const id = item.dataset.id;
            const name = item.dataset.name;
            const type = item.dataset.type;
            const avatar = item.dataset.avatar;
            openChat(id, name, avatar);
        });
    });
}

// Global function to open chat from dropdown (called from header)
function openChatFromDropdown(userId, userName, avatar) {
    openChat(userId, userName, avatar);
}

// Function to open chat from sidebar
function openChatFromSidebar(userId, userName, avatar) {
    openChat(userId, userName, avatar);
}

// Legacy support for existing code
function minimizeChatLegacy(chat) {
    const userId = chat.id.replace('chat-', '');
    minimizeChat(userId);
}

function stackMinimizedChats() {
    // This function is no longer needed with the new bubble system
    // but kept for compatibility
}

function closeChatLegacy(btn) {
    const chatWindow = btn.closest(".chat-window");
    if (chatWindow) {
        const userId = chatWindow.id.replace('chat-', '');
        closeChat(userId);
    }
}

// WebSocket integration for real-time messaging
let stompClient = null;

function connectWebSocket() {
    if (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, function (frame) {
            console.log('Connected to chat WebSocket: ' + frame);

            // Subscribe to personal message queue
            const currentUserId = getCurrentUserId();
            if (currentUserId) {
                stompClient.subscribe(`/user/${currentUserId}/queue/messages`, function (message) {
                    const messageData = JSON.parse(message.body);
                    handleIncomingMessage(messageData);
                });
            }
        }, function (error) {
            console.log('WebSocket connection error: ' + error);
            // Retry connection after 5 seconds
            setTimeout(connectWebSocket, 5000);
        });
    }
}

function handleIncomingMessage(messageData) {
    const senderId = messageData.senderId;
    const content = messageData.content;
    const senderName = messageData.senderName;
    const senderAvatar = messageData.senderAvatar;

    // Check if chat window is open
    if (openChats.has(senderId)) {
        // Add message to existing chat
        const messagesContainer = document.getElementById(`messages-${senderId}`);
        const messageDiv = document.createElement('div');
        messageDiv.className = 'message received';
        messageDiv.innerHTML = `
            <img src="${senderAvatar || '/images/default-avatar.jpg'}" alt="${senderName}" class="message-avatar">
            <div class="message-content">${content}</div>
        `;
        messagesContainer.appendChild(messageDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    } else {
        // Show notification or auto-open chat
        showMessageNotification(senderId, senderName, content, senderAvatar);
    }
}

function showMessageNotification(senderId, senderName, content, senderAvatar) {
    // Create notification popup
    const notification = document.createElement('div');
    notification.className = 'message-notification';
    notification.innerHTML = `
        <div class="notification-content">
            <img src="${senderAvatar || '/images/default-avatar.jpg'}" alt="${senderName}" class="notification-avatar">
            <div class="notification-text">
                <div class="notification-name">${senderName}</div>
                <div class="notification-message">${content}</div>
            </div>
        </div>
    `;

    notification.style.cssText = `
        position: fixed;
        top: 80px;
        right: 20px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        padding: 12px;
        max-width: 300px;
        cursor: pointer;
        z-index: 10000;
        animation: slideInRight 0.3s ease;
    `;

    notification.onclick = () => {
        openChat(senderId, senderName, senderAvatar);
        notification.remove();
    };

    document.body.appendChild(notification);

    // Auto remove after 5 seconds
    setTimeout(() => {
        if (notification.parentNode) {
            notification.remove();
        }
    }, 5000);
}

function getCurrentUserId() {
    // Get current user ID from authentication context
    // This should be implemented based on your authentication system
    return document.querySelector('meta[name="user-id"]')?.content || null;
}

// Enhanced message sending with server integration
function sendMessageToServer(userId, message) {
    if (stompClient && stompClient.connected) {
        const messageData = {
            senderId: getCurrentUserId(),
            receiverId: userId,
            content: message,
            type: 'TEXT',
            timestamp: new Date().toISOString()
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(messageData));
    } else {
        // Fallback to AJAX if WebSocket is not available
        fetch('/api/chat/send-message', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            },
            body: JSON.stringify({
                receiverId: userId,
                content: message,
                type: 'TEXT'
            })
        })
            .then(response => response.json())
            .then(data => {
                if (!data.success) {
                    console.error('Failed to send message:', data.error);
                    // Show error message to user
                    showErrorMessage('Không thể gửi tin nhắn. Vui lòng thử lại.');
                }
            })
            .catch(error => {
                console.error('Error sending message:', error);
                showErrorMessage('Lỗi kết nối. Vui lòng kiểm tra mạng.');
            });
    }
}

function showErrorMessage(message) {
    // Create error notification
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-notification';
    errorDiv.textContent = message;
    errorDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #dc3545;
        color: white;
        padding: 12px 16px;
        border-radius: 4px;
        z-index: 10001;
        animation: slideInRight 0.3s ease;
    `;

    document.body.appendChild(errorDiv);

    setTimeout(() => {
        if (errorDiv.parentNode) {
            errorDiv.remove();
        }
    }, 3000);
}

// Load chat history when opening a chat
function loadChatHistory(userId) {
    fetch(`/api/chat/history/${userId}`)
        .then(response => response.json())
        .then(messages => {
            const messagesContainer = document.getElementById(`messages-${userId}`);
            messagesContainer.innerHTML = '';

            // Add date separator
            const today = new Date().toLocaleDateString();
            messagesContainer.innerHTML = `<div class="message-time">${today}</div>`;

            // Add messages
            messages.forEach(message => {
                const messageDiv = document.createElement('div');
                const isCurrentUser = message.senderId === getCurrentUserId();
                messageDiv.className = `message ${isCurrentUser ? 'sent' : 'received'}`;

                if (isCurrentUser) {
                    messageDiv.innerHTML = `<div class="message-content">${message.content}</div>`;
                } else {
                    messageDiv.innerHTML = `
                        <img src="${message.senderAvatar || '/images/default-avatar.jpg'}" alt="${message.senderName}" class="message-avatar">
                        <div class="message-content">${message.content}</div>
                    `;
                }

                messagesContainer.appendChild(messageDiv);
            });

            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        })
        .catch(error => {
            console.error('Error loading chat history:', error);
        });
}

// Enhanced createChatWindow with history loading
function createChatWindowWithHistory(userId, userName, avatar) {
    const chatWindow = createChatWindow(userId, userName, avatar);

    // Load real chat history instead of dummy messages
    setTimeout(() => {
        loadChatHistory(userId);
    }, 100);

    return chatWindow;
}

// Initialize WebSocket connection when page loads
document.addEventListener("DOMContentLoaded", function() {
    connectWebSocket();
});

// CSS animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    .message-notification .notification-content {
        display: flex;
        align-items: center;
        gap: 10px;
    }
    
    .message-notification .notification-avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        object-fit: cover;
    }
    
    .message-notification .notification-name {
        font-weight: 600;
        font-size: 14px;
        color: #050505;
    }
    
    .message-notification .notification-message {
        font-size: 13px;
        color: #65676b;
        margin-top: 2px;
    }
    
    .message-notification:hover {
        transform: scale(1.02);
        transition: transform 0.2s ease;
    }
`;

// Function mới cho profile page
function openChatFromProfile(userId, userName, avatar) {
    if (typeof openChat === 'function') {
        openChat(userId, userName, avatar || '/images/default-avatar.jpg');
    }
}

// Load conversations cho dropdown
function loadConversations() {
    fetch('/api/conversations')
        .then(response => response.json())
        .then(conversations => {
            const conversationList = document.getElementById('conversationList');

            if (conversations.length === 0) {
                conversationList.innerHTML = `
                    <div class="text-center p-3 text-muted">
                        <i class="fas fa-comments"></i><br>
                        Chưa có cuộc trò chuyện nào
                    </div>
                `;
                return;
            }

            conversationList.innerHTML = conversations.map(conv => `
                <div class="conversation-item" onclick="openChatFromDropdown('${conv.id}', '${conv.name}', '${conv.avatar || '/images/default-avatar.jpg'}')">
                    <div style="position: relative;">
                        <img src="${conv.avatar || '/images/default-avatar.jpg'}" alt="Avatar" class="conversation-avatar">
                        ${conv.isOnline ? '<div class="online-dot"></div>' : ''}
                    </div>
                    <div class="conversation-info">
                        <div class="conversation-preview">${conv.lastMessage || 'Chưa có tin nhắn'} • ${conv.timeAgo}</div>
                    </div>
                </div>
            `).join('');
        })
        .catch(error => {
            console.error('Error loading conversations:', error);
            document.getElementById('conversationList').innerHTML = `
                <div class="text-center p-3 text-danger">
                    <i class="fas fa-exclamation-triangle"></i><br>
                    Lỗi tải danh sách
                </div>
            `;
        });
}

// Load online friends cho sidebar
function loadOnlineFriends() {
    fetch('/api/chat/online-friends')
        .then(response => response.json())
        .then(friends => {
            const friendsList = document.getElementById('onlineFriendsList');

            if (friends.length === 0) {
                friendsList.innerHTML = `
                    <div class="text-center p-3 text-muted">
                        <i class="fas fa-user-friends"></i><br>
                        Không có bạn bè online
                    </div>
                `;
                return;
            }

            friendsList.innerHTML = friends.map(friend => `
                <div class="friend-item-enhanced" onclick="openChatFromSidebar('${friend.id}', '${friend.name}', '${friend.avatar || '/images/default-avatar.jpg'}')">
                    <div style="position: relative;">
                        <img src="${friend.avatar || '/images/default-avatar.jpg'}" alt="${friend.name}" class="friend-avatar">
                        ${friend.isOnline ? '<div class="online-indicator"></div>' : ''}
                    </div>
                    <span class="friend-name">${friend.name}</span>
                </div>
            `).join('');
        })
        .catch(error => {
            console.error('Error loading online friends:', error);
            document.getElementById('onlineFriendsList').innerHTML = `
                <div class="text-center p-3 text-danger">
                    <i class="fas fa-exclamation-triangle"></i><br>
                    Lỗi tải danh sách
                </div>
            `;
        });
}
document.head.appendChild(style);