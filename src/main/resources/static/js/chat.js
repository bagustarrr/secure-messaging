// Establish WebSocket connection using SockJS and STOMP
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);
const chatInfoDiv = document.getElementById('chatInfo');
const currentChatId = chatInfoDiv.dataset.chatId;
const currentChatType = chatInfoDiv.dataset.chatType;
const currentUserIIN = chatInfoDiv.dataset.currentUser;

// Connect and set up subscriptions
stompClient.connect({}, function (frame) {
    console.log("Connected: " + frame);
    // Subscribe to personal queue for private messages
    stompClient.subscribe('/user/queue/messages', function (message) {
        const msg = JSON.parse(message.body);
        if (!currentChatId || msg.chatId !== currentChatId) {
            // If message belongs to a different chat (not currently open), we could notify or ignore
            console.log("New message for another chat:", msg);
            // Optionally, update sidebar or highlight chat item for unread.
            return;
        }
        displayMessage(msg);
    });
    // If current chat is a group, subscribe to its topic
    if (currentChatType === 'GROUP' && currentChatId) {
        stompClient.subscribe('/topic/chatroom/' + currentChatId, function (message) {
            const msg = JSON.parse(message.body);
            displayMessage(msg);
        });
    }
});

// Display a message in the chat window
function displayMessage(msg) {
    const messageArea = document.getElementById('messageArea');
    if (!messageArea) return;
    // Create message div
    const messageDiv = document.createElement('div');
    let text;
    if (msg.sender === currentUserIIN) {
        // Message from current user
        text = "You: " + msg.content;
        messageDiv.className = "message my-message";
    } else {
        // Message from another user
        text = msg.senderName + ": " + msg.content;
        messageDiv.className = "message their-message";
    }
    messageDiv.textContent = text;
    messageArea.appendChild(messageDiv);
    // Scroll to bottom
    messageArea.scrollTop = messageArea.scrollHeight;
}

// Send a message (called on form submit)
function sendMessage() {
    const input = document.getElementById('messageInput');
    if (!input || !input.value.trim()) {
        return;
    }
    const content = input.value.trim();
    // Determine destination (unified endpoint "/app/chat" handles both private and group)
    stompClient.send("/app/chat", {}, JSON.stringify({chatId: currentChatId, content: content}));
    input.value = "";
}

// Mobile: sidebar toggle logic
const sidebar = document.getElementById('sidebar');
const toggleBtn = document.getElementById('toggleSidebar');
const closeBtn = document.getElementById('closeSidebar');
if (toggleBtn) {
    toggleBtn.addEventListener('click', () => {
        sidebar.classList.add('open');
    });
}
if (closeBtn) {
    closeBtn.addEventListener('click', () => {
        sidebar.classList.remove('open');
    });
}
