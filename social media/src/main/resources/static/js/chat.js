const chatContainer = document.getElementById("chatContainer");

function minimizeChat(chat) {
    chat.dataset.state = 'minimized';
    stackMinimizedChats();
}

function stackMinimizedChats() {
    const bubbles = [...document.querySelectorAll('.chat-window[data-state="minimized"]')];
    bubbles.forEach((chat, index) => {
        chat.style.bottom = `${20 + index * 60}px`;
        chat.style.right = '20px';
        chat.style.zIndex = 10000 + index;
    });
}
function openChat(chatId, chatName, chatType, avatar) {
    if (document.querySelector(`.chat-window[data-chat-id="${chatId}"]`)) return;

    const openChats = document.querySelectorAll('.chat-window[data-state="open"]');
    if (openChats.length >= 2) {
        const oldestChat = openChats[0];
        minimizeChat(oldestChat);
    }

    fetch(`/chat-window?chatId=${chatId}&chatName=${encodeURIComponent(chatName)}&chatType=${chatType}&avatar=${avatar}`)
        .then(res => res.text())
        .then(html => {
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = html;
            const newChat = tempDiv.firstElementChild;

            // Thêm vào DOM trước khi kiểm tra
            chatContainer.appendChild(newChat);

            // Kích hoạt reflow để đảm bảo CSS được áp dụng
            void newChat.offsetHeight;

            const chatBox = newChat.querySelector('.chat-box');
            if (chatBox && chatBox.offsetHeight === 0) {
                // Thêm nội dung mẫu nếu cần
                chatBox.innerHTML = `
                    <div class="chat-r">
                        <div class="mess mess-r"><p>Test message right</p></div>
                    </div>
                    <div class="chat-l">
                        <div class="mess"><p>Test message left</p></div>
                    </div>
                `;

            }

            // Thêm sự kiện
            newChat.querySelector('.btn-minimize')?.addEventListener('click', e => {
                e.stopPropagation();
                if (newChat.dataset.state !== 'minimized') {
                    minimizeChat(newChat);
                }
            });

            stackMinimizedChats();
        })
        .catch(error => console.error('Fetch error:', error));
}

function closeChat(btn) {
    btn.closest(".chat-window").remove();
    stackMinimizedChats();
}

document.getElementById('chatContainer').addEventListener('click', e => {
    const btnMinimize = e.target.closest('.btn-minimize');
    if (btnMinimize) {
        e.stopPropagation();
        const chat = btnMinimize.closest('.chat-window');
        if (chat && chat.dataset.state !== 'minimized') {
            minimizeChat(chat);
        }
    }

    const minimized = e.target.closest('.chat-window[data-state="minimized"]');
    if (minimized) {
        e.stopPropagation();

        const openChats = document.querySelectorAll('.chat-window[data-state="open"]');

        if (openChats.length >= 2) {
            const oldestChat = openChats[0];
            minimizeChat(oldestChat);
        }

        minimized.dataset.state = 'open';

        stackMinimizedChats();
    }
});

function initChatSelectors() {
    document.querySelectorAll(".friend-item, .group-item").forEach(item => {
        item.addEventListener("click", () => {
            const id = item.dataset.id;
            const name = item.dataset.name;
            const type = item.dataset.type;
            const avatar = item.dataset.avatar;
            openChat(id, name, type, avatar);
        });
    });
}

document.addEventListener("DOMContentLoaded", initChatSelectors);