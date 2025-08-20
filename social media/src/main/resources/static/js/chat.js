// ================ FIXED HORIZONTAL CHAT LAYOUT ================

let openChats = new Map();
let chatBubbles = new Map();
const maxChats = 3; // Maximum visible chat windows

// Initialize chat system
document.addEventListener("DOMContentLoaded", function() {
    initChatSelectors();
    setupChatContainer();
});

function setupChatContainer() {
    let chatContainer = document.getElementById("chatContainer");
    if (!chatContainer) {
        chatContainer = document.createElement('div');
        chatContainer.id = 'chatContainer';
        chatContainer.className = 'chat-container';
        document.body.appendChild(chatContainer);
    }

    // Ensure horizontal layout
    chatContainer.style.flexDirection = 'row';
    chatContainer.style.alignItems = 'flex-end';
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
    const chatContainer = document.getElementById('chatContainer');

    // Add chat window before bubbles (maintain order)
    const firstBubble = chatContainer.querySelector('.chat-bubble');
    if (firstBubble) {
        chatContainer.insertBefore(chatWindow, firstBubble);
    } else {
        chatContainer.appendChild(chatWindow);
    }

    openChats.set(userId, chatWindow);

    // Load chat history
    setTimeout(() => {
        loadChatHistory(userId);
    }, 100);
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

    return chatWindow;
}

function minimizeChat(userId) {
    const chatWindow = openChats.get(userId);
    if (chatWindow) {
        chatWindow.style.display = 'none';

        // Create bubble and add to end of container (after chat windows)
        const bubble = createChatBubble(userId, chatWindow);
        const chatContainer = document.getElementById('chatContainer');
        chatContainer.appendChild(bubble); // Add to end

        chatBubbles.set(userId, bubble);
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

        // Move chat window back before bubbles
        const chatContainer = document.getElementById('chatContainer');
        const firstBubble = chatContainer.querySelector('.chat-bubble');
        if (firstBubble) {
            chatContainer.insertBefore(chatWindow, firstBubble);
        } else {
            chatContainer.appendChild(chatWindow);
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

        // Send message to server
        sendMessageToServer(userId, message);

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

function sendMessageToServer(userId, message) {
    if (typeof stompClient !== 'undefined' && stompClient && stompClient.connected) {
        const messageData = {
            senderId: getCurrentUserId(),
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
                    showErrorMessage('Không thể gửi tin nhắn. Vui lòng thử lại.');
                }
            })
            .catch(error => {
                console.error('Error sending message:', error);
                showErrorMessage('Lỗi kết nối. Vui lòng kiểm tra mạng.');
            });
    }
}

function loadChatHistory(userId) {
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

function getCurrentUserId() {
    return document.querySelector('meta[name="user-id"]')?.content ||
        document.querySelector('[data-user-id]')?.getAttribute('data-user-id') ||
        '1';
}

// Global functions for external calls
function openChatFromDropdown(userId, userName, avatar) {
    openChat(userId, userName, avatar);
}

function openChatFromSidebar(userId, userName, avatar) {
    openChat(userId, userName, avatar);
}

function openChatFromProfile(userId, userName, avatar) {
    openChat(userId, userName, avatar || '/images/default-avatar.jpg');
}

// Initialize chat selectors
function initChatSelectors() {
    document.querySelectorAll(".friend-item, .group-item").forEach(item => {
        item.addEventListener("click", () => {
            const id = item.dataset.id;
            const name = item.dataset.name;
            const avatar = item.dataset.avatar;
            openChat(id, name, avatar);
        });
    });
}

// Error handling
function showErrorMessage(message) {
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