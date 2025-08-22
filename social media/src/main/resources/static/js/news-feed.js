document.addEventListener('DOMContentLoaded', function() {
    loadUserStats();
});

async function loadUserStats() {
    try {
        const response = await fetch('/api/user/stats');
        if (response.ok) {
            const stats = await response.json();

            document.getElementById('friends-count').textContent = stats.friends || 0;
            document.getElementById('posts-count').textContent = stats.posts || 0;
            document.getElementById('likes-count').textContent = stats.likes || 0;
        }
    } catch (error) {
        console.error('Error loading user stats:', error);
    }
}

// Update stats when new post is created
function updatePostsCount() {
    const postsCountEl = document.getElementById('posts-count');
    if (postsCountEl) {
        const currentCount = parseInt(postsCountEl.textContent) || 0;
        postsCountEl.textContent = currentCount + 1;
    }
}