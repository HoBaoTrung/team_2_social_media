// Kích hoạt Bootstrap tooltip
document.addEventListener("DOMContentLoaded", function () {
    // Message dropdown functionality
    setupMessageDropdown();
});

function setupMessageDropdown() {
    const messageIcon = document.getElementById('messageIcon');
    const messageDropdown = document.getElementById('messageDropdown');

    if (!messageIcon || !messageDropdown) return;

    // Toggle dropdown
    messageIcon.addEventListener('click', function(e) {
        e.stopPropagation();
        messageDropdown.classList.toggle('show');

        // Load conversations when opening
        if (messageDropdown.classList.contains('show')) {
            loadConversationsForDropdown();
        }
    });

    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (!e.target.closest('#messageIcon')) {
            messageDropdown.classList.remove('show');
        }
    });
}

function loadConversationsForDropdown() {
    const conversationList = document.getElementById('conversationList');

    // Use real API to load conversations
    fetch('/api/conversations')
        .then(response => response.json())
        .then(conversations => {
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
                        <div class="conversation-item" onclick="openChatFromDropdown('${conv.id}', '${conv.name}', '${conv.avatar}')">
                            <div style="position: relative;">
                                <img src="${conv.avatar || '/images/default-avatar.jpg'}" alt="Avatar" class="conversation-avatar">
                                ${conv.isOnline ? '<div class="online-dot"></div>' : ''}
                            </div>
                            <div class="conversation-info">
                                <div style="font-weight: 600; font-size: 14px;">${conv.name}</div>
                                <div class="conversation-preview">${conv.lastMessage || 'Chưa có tin nhắn'} • ${conv.timeAgo}</div>
                            </div>
                        </div>
                    `).join('');
        })
        .catch(error => {
            console.error('Error loading conversations:', error);
            conversationList.innerHTML = `
                        <div class="text-center p-3 text-danger">
                            <i class="fas fa-exclamation-triangle"></i><br>
                            Lỗi tải danh sách
                        </div>
                    `;
        });
}

function openChatFromDropdown(userId, userName, avatar) {
    // Close dropdown
    document.getElementById('messageDropdown').classList.remove('show');

    // Call the global openChat function
    if (typeof openChat === 'function') {
        openChat(userId, userName, avatar);
    } else if (typeof chatManager !== 'undefined') {
        chatManager.openChat(userId, userName, avatar);
    }
}