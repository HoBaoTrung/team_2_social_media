// ================ IMPROVED CHAT SYSTEM ================

class ChatManager {
    constructor() {
        this.openChats = new Map();
        this.chatBubbles = new Map();
        this.maxChats = 3; // Maximum visible chat windows
        this.setupContainers();
        this.setupEventListeners();
    }

    setupContainers() {
        // Ensure separate containers exist
        let bubblesContainer = document.getElementById('chatBubblesContainer');
        if (!bubblesContainer) {
            bubblesContainer = document.createElement('div');
            bubblesContainer.id = 'chatBubblesContainer';
            bubblesContainer.className = 'chat-bubbles-container';
            document.body.appendChild(bubblesContainer);
        }

        let windowsContainer = document.getElementById('chatWindowsContainer');
        if (!windowsContainer) {
            windowsContainer = document.createElement('div');
            windowsContainer.id = 'chatWindowsContainer';
            windowsContainer.className = 'chat-windows-container';
            document.body.appendChild(windowsContainer);
        }
    }

    setupEventListeners() {
        // Setup any global event listeners if needed
        document.addEventListener('click', (e) => {
            // Close message dropdown when clicking outside
            if (!e.target.closest('#messageIcon')) {
                const dropdown = document.getElementById('messageDropdown');
                if (dropdown) {
                    dropdown.classList.remove('show');
                }
            }
        });
    }

    openChat(userId, userName, avatar) {
        console.log("open chat in new feed")
        // Check if chat is already open
        if (this.openChats.has(userId)) {
            const existingChat = this.openChats.get(userId);
            existingChat.style.display = 'flex';

            // Remove bubble if exists
            if (this.chatBubbles.has(userId)) {
                this.chatBubbles.get(userId).remove();
                this.chatBubbles.delete(userId);
            }
            return;
        }

        // Check if we need to minimize oldest chat
        if (this.openChats.size >= this.maxChats) {
            const oldestChatId = Array.from(this.openChats.keys())[0];
            this.minimizeChat(oldestChatId);
        }

        // Create new chat window
        const chatWindow = this.createChatWindow(userId, userName, avatar);
        const windowsContainer = document.getElementById('chatWindowsContainer');
        windowsContainer.appendChild(chatWindow);

        this.openChats.set(userId, chatWindow);

        // Load chat history
        setTimeout(() => {
            this.loadChatHistory(userId);
        }, 100);
    }

    createChatWindow(userId, userName, avatar) {
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
                    <button class="chat-btn" onclick="chatManager.toggleVideo('${userId}')">
                        <i class="fas fa-video"></i>
                    </button>
                    <button class="chat-btn" onclick="chatManager.minimizeChat('${userId}')">
                        <i class="fas fa-minus"></i>
                    </button>
                    <button class="chat-btn" onclick="chatManager.closeChat('${userId}')">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            </div>
            <div class="chat-messages" id="messages-${userId}">
                <div class="message-time">Hôm nay</div>
            </div>
            <div class="chat-input">
                <div class="input-container">
                    <input type="text" placeholder="Nhập tin nhắn..." id="input-${userId}" onkeypress="chatManager.handleKeyPress(event, '${userId}')">
                    <div class="input-icons">
                        <i class="far fa-smile input-icon" onclick="chatManager.toggleEmoji('${userId}')"></i>
                        <i class="fas fa-paperclip input-icon" onclick="chatManager.attachFile('${userId}')"></i>
                        <i class="fas fa-paper-plane input-icon" onclick="chatManager.sendMessage('${userId}')"></i>
                    </div>
                </div>
            </div>
        `;

        return chatWindow;
    }

    minimizeChat(userId) {
        const chatWindow = this.openChats.get(userId);
        if (chatWindow) {
            chatWindow.style.display = 'none';

            // Create bubble and add to bubbles container
            const bubble = this.createChatBubble(userId, chatWindow);
            const bubblesContainer = document.getElementById('chatBubblesContainer');
            bubblesContainer.appendChild(bubble);

            this.chatBubbles.set(userId, bubble);
        }
    }

    createChatBubble(userId, chatWindow) {
        const bubble = document.createElement('div');
        bubble.className = 'chat-bubble';
        bubble.onclick = () => this.restoreChat(userId);

        const userName = chatWindow.querySelector('.chat-name').textContent;
        const avatar = chatWindow.querySelector('.chat-avatar').src;

        bubble.innerHTML = `
            <img src="${avatar}" alt="${userName}">
            <div class="online-indicator"></div>
        `;

        return bubble;
    }

    restoreChat(userId) {
        const chatWindow = this.openChats.get(userId);
        const bubble = this.chatBubbles.get(userId);

        if (chatWindow && bubble) {
            // Check if we need to minimize another chat
            const visibleChats = Array.from(this.openChats.values()).filter(chat => chat.style.display !== 'none');
            if (visibleChats.length >= this.maxChats) {
                const oldestVisible = visibleChats[0];
                const oldestId = oldestVisible.id.replace('chat-', '');
                this.minimizeChat(oldestId);
            }

            chatWindow.style.display = 'flex';
            bubble.remove();
            this.chatBubbles.delete(userId);
        }
    }

    closeChat(userId) {
        const chatWindow = this.openChats.get(userId);
        const bubble = this.chatBubbles.get(userId);

        if (chatWindow) {
            chatWindow.remove();
            this.openChats.delete(userId);
        }

        if (bubble) {
            bubble.remove();
            this.chatBubbles.delete(userId);
        }
    }

    sendMessage(userId) {
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

            // Send message to server
            this.sendMessageToServer(userId, message);

            // Simulate response (remove when implementing real chat)
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

    sendMessageToServer(userId, message) {
        if (typeof stompClient !== 'undefined' && stompClient && stompClient.connected) {
            const messageData = {
                senderId: this.getCurrentUserId(),
                receiverId: userId,
                content: message,
                type: 'TEXT',
                timestamp: new Date().toISOString()
            };

            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(messageData));
        } else {
            // Fallback to AJAX
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
                        this.showErrorMessage('Không thể gửi tin nhắn. Vui lòng thử lại.');
                    }
                })
                .catch(error => {
                    console.error('Error sending message:', error);
                    this.showErrorMessage('Lỗi kết nối. Vui lòng kiểm tra mạng.');
                });
        }
    }

    loadChatHistory(userId) {
        // Add some demo messages
        const messagesContainer = document.getElementById(`messages-${userId}`);
        if (messagesContainer) {
            const demoMessages = [
                { type: 'received', content: 'Chào bạn! 👋' },
                { type: 'sent', content: 'Chào! Bạn khỏe không?' },
                { type: 'received', content: 'Mình khỏe, cảm ơn bạn!' }
            ];

            demoMessages.forEach(msg => {
                const messageDiv = document.createElement('div');
                messageDiv.className = `message ${msg.type}`;

                if (msg.type === 'received') {
                    const avatar = document.querySelector(`#chat-${userId} .chat-avatar`).src;
                    messageDiv.innerHTML = `
                        <img src="${avatar}" alt="Avatar" class="message-avatar">
                        <div class="message-content">${msg.content}</div>
                    `;
                } else {
                    messageDiv.innerHTML = `<div class="message-content">${msg.content}</div>`;
                }

                messagesContainer.appendChild(messageDiv);
            });

            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }
    }

    handleKeyPress(event, userId) {
        if (event.key === 'Enter') {
            this.sendMessage(userId);
        }
    }

    toggleVideo(userId) {
        alert('Tính năng video call sẽ sớm có!');
    }

    toggleEmoji(userId) {
        alert('Tính năng emoji sẽ sớm có!');
    }

    attachFile(userId) {
        alert('Tính năng đính kèm file sẽ sớm có!');
    }

    getCurrentUserId() {
        return document.querySelector('meta[name="user-id"]')?.content ||
            document.querySelector('[data-user-id]')?.getAttribute('data-user-id') ||
            '1';
    }

    showErrorMessage(message) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-notification';
        errorDiv.textContent = message;
        errorDiv.style.cssText = `
            position: fixed;
            top: 80px;
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

    // Load online friends for sidebar
    loadOnlineFriends() {
        const friendsList = document.getElementById('onlineFriendsList');
        if (!friendsList) return;

        fetch('/api/chat/online-friends')
            .then(response => response.json())
            .then(friends => {
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
                    <div class="friend-item-enhanced" onclick="chatManager.openChat('${friend.id}', '${friend.name}', '${friend.avatar || '/images/default-avatar.jpg'}')">
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
                friendsList.innerHTML = `
                    <div class="text-center p-3 text-danger">
                        <i class="fas fa-exclamation-triangle"></i><br>
                        Lỗi tải danh sách
                    </div>
                `;
            });
    }
}

// Global functions for external calls
function openChatFromDropdown(userId, userName, avatar) {
    if (typeof chatManager !== 'undefined') {
        chatManager.openChat(userId, userName, avatar);
    }
}

function openChatFromSidebar(userId, userName, avatar) {
    if (typeof chatManager !== 'undefined') {
        chatManager.openChat(userId, userName, avatar);
    }
}

function openChatFromProfile(userId, userName, avatar) {
    if (typeof chatManager !== 'undefined') {
        chatManager.openChat(userId, userName, avatar || '/images/default-avatar.jpg');
    }
}

// Global chat function for backward compatibility
function openChat(userId, userName, avatar) {
    if (typeof chatManager !== 'undefined') {
        chatManager.openChat(userId, userName, avatar);
    }
}

// Initialize chat manager
let chatManager;
document.addEventListener("DOMContentLoaded", function() {
    chatManager = new ChatManager();

    // Load online friends if sidebar exists
    if (document.getElementById('onlineFriendsList')) {
        chatManager.loadOnlineFriends();
    }
});

// Handle incoming chat messages (WebSocket)
function handleIncomingMessage(messageData) {
    const senderId = messageData.senderId;
    const content = messageData.content;
    const senderName = messageData.senderName;
    const senderAvatar = messageData.senderAvatar;

    // Check if chat window is open
    if (typeof chatManager !== 'undefined' && chatManager.openChats.has(senderId.toString())) {
        // Add message to existing chat
        const messagesContainer = document.getElementById(`messages-${senderId}`);
        if (messagesContainer) {
            const messageDiv = document.createElement('div');
            messageDiv.className = 'message received';
            messageDiv.innerHTML = `
                <img src="${senderAvatar || '/images/default-avatar.jpg'}" alt="${senderName}" class="message-avatar">
                <div class="message-content">${content}</div>
            `;
            messagesContainer.appendChild(messageDiv);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }
    } else {
        // Show notification
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
        if (typeof chatManager !== 'undefined') {
            chatManager.openChat(senderId.toString(), senderName, senderAvatar);
        }
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