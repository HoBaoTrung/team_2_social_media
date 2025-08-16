// Profile Posts JavaScript
class ProfilePostManager {
    constructor() {
        this.currentPage = 0;
        this.isLoading = false;
        this.hasMorePosts = true;
        this.selectedImages = [];
        this.username = null;
        this.searchTimeout = null;

        this.init();
    }

    init() {
        // Get username from container
        const container = document.getElementById('profile-posts-container');
        if (container) {
            this.username = container.getAttribute('data-username');
            this.setupEventListeners();
            this.loadInitialPosts();
            this.setupInfiniteScroll();
        }

        // Setup photos tab if active
        const photosContainer = document.getElementById('profile-photos-container');
        if (photosContainer) {
            this.username = photosContainer.getAttribute('data-username');
            this.loadPhotos();
        }
    }

    setupEventListeners() {
        // Profile post creation form
        // const profileForm = document.getElementById('profile-post-form');
        // if (profileForm) {
        //     profileForm.addEventListener('submit', (e) => this.handleProfilePostCreate(e));
        // }

        // Profile content input validation
        const profileContentInput = document.querySelector('#profile-post-form .post-content-input');
        if (profileContentInput) {
            profileContentInput.addEventListener('input', () => this.validateProfileForm());
        }

        // Profile image input
        const profileImageInput = document.getElementById('profile-image-input');
        if (profileImageInput) {
            profileImageInput.addEventListener('change', (e) => this.handleProfileImageSelection(e));
        }

        // Search functionality
        const searchInput = document.getElementById('post-search-input');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => this.handleSearch(e));
        }
    }

    validateProfileForm() {
        const content = document.querySelector('#profile-post-form .post-content-input').value.trim();
        const submitBtn = document.getElementById('profile-post-submit-btn');

        if (content.length > 0 || this.selectedImages.length > 0) {
            submitBtn.disabled = false;
        } else {
            submitBtn.disabled = true;
        }
    }

    handleProfileImageSelection(event) {
        const files = Array.from(event.target.files);
        this.selectedImages = files;
        this.displayProfileImagePreview(files);
        this.validateProfileForm();
    }

    displayProfileImagePreview(files) {
        const container = document.getElementById('profile-image-preview-container');
        const list = document.getElementById('profile-image-preview-list');

        if (files.length === 0) {
            container.style.display = 'none';
            return;
        }

        container.style.display = 'block';
        list.innerHTML = '';

        files.forEach((file, index) => {
            const reader = new FileReader();
            reader.onload = (e) => {
                const item = document.createElement('div');
                item.className = 'image-preview-item';
                item.innerHTML = `
                    <img src="${e.target.result}" alt="Preview">
                    <button type="button" class="image-preview-remove" onclick="profilePostManager.removeProfileSelectedImage(${index})">
                        <i class="fas fa-times"></i>
                    </button>
                `;
                list.appendChild(item);
            };
            reader.readAsDataURL(file);
        });
    }

    removeProfileSelectedImage(index) {
        this.selectedImages.splice(index, 1);
        this.displayProfileImagePreview(this.selectedImages);
        this.validateProfileForm();

        // Update file input
        const imageInput = document.getElementById('profile-image-input');
        const dt = new DataTransfer();
        this.selectedImages.forEach(file => dt.items.add(file));
        imageInput.files = dt.files;
    }

    async handleProfilePostCreate(event) {
        event.preventDefault();

        if (this.isLoading) return;

        const form = event.target;
        const formData = new FormData(form);

        // Add selected images
        this.selectedImages.forEach(image => {
            formData.append('images', image);
        });

        const submitBtn = form.querySelector('.post-btn');
        const originalText = submitBtn.innerHTML;

        try {
            this.isLoading = true;
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang đăng...';

            const response = await fetch('/posts/api/create', {
                method: 'POST',
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            const result = await response.json();

            if (result.success) {
                // Reset form
                this.resetProfileCreateForm();

                // Reload posts to show new post
                this.loadInitialPosts();

                // Show success message
                this.showNotification('Đăng bài thành công!', 'success');
            } else {
                this.showNotification(result.message || 'Có lỗi xảy ra', 'error');
            }
        } catch (error) {
            console.error('Error creating profile post:', error);
            this.showNotification('Có lỗi xảy ra khi đăng bài', 'error');
        } finally {
            this.isLoading = false;
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        }
    }

    resetProfileCreateForm() {
        const form = document.getElementById('profile-post-form');
        if (form) {
            form.reset();
            this.selectedImages = [];
            document.getElementById('profile-image-preview-container').style.display = 'none';
            this.validateProfileForm();
        }
    }

    async loadInitialPosts() {
        this.currentPage = 0;
        this.hasMorePosts = true;
        const postsContainer = document.getElementById('profile-posts-container');
        if (postsContainer) {
            postsContainer.innerHTML = '';
            await this.loadPosts();
        }
    }

    async loadPosts() {
        if (this.isLoading || !this.hasMorePosts || !this.username) return;

        this.isLoading = true;
        this.showProfileLoading(true);

        try {
            const response = await fetch(`/posts/api/user/${this.username}?page=${this.currentPage}&size=10`);

            if (!response.ok) {
                throw new Error('Failed to load posts');
            }

            const data = await response.json();

            if (data.content && data.content.length > 0) {
                data.content.forEach(post => {
                    this.appendProfilePost(post);
                });

                this.currentPage++;
                this.hasMorePosts = !data.last;

                // Hide no posts message
                const noPostsEl = document.getElementById('profile-no-posts');
                if (noPostsEl) {
                    noPostsEl.style.display = 'none';
                }
            } else {
                this.hasMorePosts = false;

                // Show no posts message if this is the first page
                if (this.currentPage === 0) {
                    const noPostsEl = document.getElementById('profile-no-posts');
                    if (noPostsEl) {
                        noPostsEl.style.display = 'block';
                    }
                }
            }
        } catch (error) {
            console.error('Error loading profile posts:', error);
            this.showNotification('Không thể tải bài viết', 'error');
        } finally {
            this.isLoading = false;
            this.showProfileLoading(false);
        }
    }

    appendProfilePost(post) {
        const postsContainer = document.getElementById('profile-posts-container');
        if (!postsContainer) return;

        const postElement = this.createProfilePostElement(post);
        postsContainer.insertAdjacentHTML('beforeend', postElement);
    }

    createProfilePostElement(post) {
        const privacyIcons = {
            'PUBLIC': '🌍',
            'FRIENDS': '👥',
            'PRIVATE': '🔒'
        };

        const imagesHtml = this.createImagesHtml(post.imageUrls);
        const timeAgo = this.formatTimeAgo(post.createdAt);

        return `
            <div class="post-item" data-post-id="${post.id}">
                <div class="post-header">
                    <img src="${post.userAvatarUrl || '/images/default-avatar.jpg'}" 
                         alt="Avatar" class="post-avatar">
                    <div class="post-user-info">
                        <a href="/profile/${post.username}" class="post-username">
                            ${post.userFullName}
                        </a>
                        <div class="post-meta">
                            <span class="post-time">${timeAgo}</span>
                            <span class="post-privacy">
                                <span class="privacy-icon">${privacyIcons[post.privacyLevel]}</span>
                                ${post.privacyLevel === 'PUBLIC' ? 'Công khai' :
            post.privacyLevel === 'FRIENDS' ? 'Bạn bè' : 'Chỉ mình tôi'}
                            </span>
                        </div>
                    </div>
                    ${post.canEdit || post.canDelete ? `
                        <div class="post-actions-menu">
                            <button class="post-menu-btn" onclick="profilePostManager.togglePostMenu(${post.id})">
                                <i class="fas fa-ellipsis-h"></i>
                            </button>
                            <div class="dropdown-menu" id="post-menu-${post.id}" style="display: none;">
                                ${post.canEdit ? `<a class="dropdown-item" href="#" onclick="profilePostManager.editPost(${post.id})">
                                    <i class="fas fa-edit"></i> Chỉnh sửa
                                </a>` : ''}
                                ${post.canDelete ? `<a class="dropdown-item text-danger" href="#" onclick="profilePostManager.deletePost(${post.id})">
                                    <i class="fas fa-trash"></i> Xóa
                                </a>` : ''}
                            </div>
                        </div>
                    ` : ''}
                </div>
                
                <div class="post-content">${post.content}</div>
                
                ${imagesHtml}
                
                ${post.likesCount > 0 || post.commentsCount > 0 ? `
                    <div class="post-stats">
                        <div class="post-likes">
                            ${post.likesCount > 0 ? `
                                <div class="post-likes-icon">
                                    <i class="fas fa-heart"></i>
                                </div>
                                <span>${post.likesCount} lượt thích</span>
                            ` : ''}
                        </div>
                        <div>
                            ${post.commentsCount > 0 ? `
                                <span class="post-comments-count">${post.commentsCount} bình luận</span>
                            ` : ''}
                        </div>
                    </div>
                ` : ''}
                
                <div class="post-actions">
                    <button class="post-action ${post.isLikedByCurrentUser ? 'liked' : ''}" 
                            onclick="profilePostManager.toggleLike(${post.id})">
                        <i class="fas fa-heart"></i>
                        <span>Thích</span>
                    </button>
                    <button class="post-action" onclick="profilePostManager.toggleComments(${post.id})">
                        <i class="fas fa-comment"></i>
                        <span>Bình luận</span>
                    </button>
                    <button class="post-action">
                        <i class="fas fa-share"></i>
                        <span>Chia sẻ</span>
                    </button>
                </div>
                
                <div class="post-comments" id="comments-${post.id}" style="display: none;">
                    <div class="comment-form">
                        <img src="${this.getCurrentUserAvatar()}" alt="Avatar" class="comment-avatar">
                        <div class="comment-input-container">
                            <textarea class="comment-input" placeholder="Viết bình luận..." 
                                     onkeypress="profilePostManager.handleCommentKeyPress(event, ${post.id})"></textarea>
                            <button class="comment-submit" onclick="profilePostManager.submitComment(${post.id})">
                                <i class="fas fa-paper-plane"></i>
                            </button>
                        </div>
                    </div>
                    <div class="comments-list" id="comments-list-${post.id}">
                        <!-- Comments will be loaded here -->
                    </div>
                </div>
            </div>
        `;
    }

    createImagesHtml(imageUrls) {
        if (!imageUrls || imageUrls.length === 0) {
            return '';
        }

        if (imageUrls.length === 1) {
            return `
                <div class="post-images">
                    <div class="post-images-single">
                        <img src="${imageUrls[0]}" alt="Post image" onclick="profilePostManager.viewImage('${imageUrls[0]}')">
                    </div>
                </div>
            `;
        }

        let gridClass = `grid-${Math.min(imageUrls.length, 4)}`;
        let html = `<div class="post-images"><div class="post-images-grid ${gridClass}">`;

        const displayCount = Math.min(imageUrls.length, 4);
        for (let i = 0; i < displayCount; i++) {
            const isLast = i === displayCount - 1 && imageUrls.length > 4;
            const extraCount = imageUrls.length - 4;

            html += `
                <div class="post-image-item ${isLast ? 'post-images-more' : ''}" 
                     ${isLast ? `data-count="${extraCount}"` : ''}
                     onclick="profilePostManager.viewImages(${JSON.stringify(imageUrls).replace(/"/g, '&quot;')}, ${i})">
                    <img src="${imageUrls[i]}" alt="Post image">
                </div>
            `;
        }

        html += '</div></div>';
        return html;
    }

    setupInfiniteScroll() {
        window.addEventListener('scroll', () => {
            if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 1000) {
                this.loadPosts();
            }
        });
    }

    // Search functionality
    handleSearch(event) {
        const keyword = event.target.value.trim();

        // Clear previous timeout
        if (this.searchTimeout) {
            clearTimeout(this.searchTimeout);
        }

        if (keyword.length === 0) {
            this.clearSearchResults();
            return;
        }

        // Debounce search
        this.searchTimeout = setTimeout(() => {
            this.searchPosts(keyword);
        }, 500);
    }

    async searchPosts(keyword) {
        const loadingEl = document.getElementById('search-loading');
        const resultsContainer = document.getElementById('search-results-container');
        const noResultsEl = document.getElementById('no-search-results');

        // Show loading
        loadingEl.style.display = 'block';
        resultsContainer.innerHTML = '';
        noResultsEl.style.display = 'none';

        try {
            const response = await fetch(`/posts/search?keyword=${encodeURIComponent(keyword)}&size=20`);
            const data = await response.json();

            if (data.content && data.content.length > 0) {
                data.content.forEach(post => {
                    const postElement = this.createSearchResultElement(post);
                    resultsContainer.insertAdjacentHTML('beforeend', postElement);
                });
            } else {
                noResultsEl.style.display = 'block';
            }
        } catch (error) {
            console.error('Error searching posts:', error);
            this.showNotification('Có lỗi xảy ra khi tìm kiếm', 'error');
        } finally {
            loadingEl.style.display = 'none';
        }
    }

    createSearchResultElement(post) {
        const timeAgo = this.formatTimeAgo(post.createdAt);
        const shortContent = post.content.length > 100 ?
            post.content.substring(0, 100) + '...' :
            post.content;

        return `
            <div class="search-result-item border-bottom py-3" onclick="profilePostManager.goToPost(${post.id})">
                <div class="d-flex">
                    <div class="flex-shrink-0 me-3">
                        <img src="${post.userAvatarUrl || '/images/default-avatar.jpg'}" 
                             class="rounded-circle" width="40" height="40" alt="Avatar">
                    </div>
                    <div class="flex-grow-1">
                        <div class="fw-bold">${post.userFullName}</div>
                        <div class="text-muted small">${timeAgo}</div>
                        <div class="mt-2">${shortContent}</div>
                        ${post.imageUrls && post.imageUrls.length > 0 ? `
                            <div class="mt-2">
                                <small class="text-muted">
                                    <i class="fas fa-image"></i> ${post.imageUrls.length} ảnh
                                </small>
                            </div>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;
    }

    clearSearchResults() {
        const resultsContainer = document.getElementById('search-results-container');
        const noResultsEl = document.getElementById('no-search-results');

        if (resultsContainer) resultsContainer.innerHTML = '';
        if (noResultsEl) noResultsEl.style.display = 'none';
    }

    goToPost(postId) {
        // Close search modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('searchPostsModal'));
        if (modal) {
            modal.hide();
        }

        // Scroll to post
        const postElement = document.querySelector(`[data-post-id="${postId}"]`);
        if (postElement) {
            postElement.scrollIntoView({ behavior: 'smooth', block: 'center' });

            // Highlight post briefly
            postElement.style.backgroundColor = '#e3f2fd';
            setTimeout(() => {
                postElement.style.backgroundColor = '';
            }, 2000);
        }
    }

    // Photos functionality
    async loadPhotos() {
        const photosContainer = document.getElementById('profile-photos-container');
        const loadingEl = document.getElementById('photos-loading');
        const noPhotosEl = document.getElementById('no-photos');

        if (!photosContainer || !this.username) return;

        loadingEl.style.display = 'block';

        try {
            const response = await fetch(`/posts/api/user/${this.username}/photos`);
            const photos = await response.json();

            if (photos && photos.length > 0) {
                photos.forEach(photo => {
                    const photoElement = this.createPhotoElement(photo);
                    photosContainer.insertAdjacentHTML('beforeend', photoElement);
                });
                noPhotosEl.style.display = 'none';
            } else {
                noPhotosEl.style.display = 'block';
            }
        } catch (error) {
            console.error('Error loading photos:', error);
            this.showNotification('Không thể tải ảnh', 'error');
        } finally {
            loadingEl.style.display = 'none';
        }
    }

    createPhotoElement(photo) {
        return `
            <div class="photo-item" onclick="profilePostManager.viewImage('${photo.url}')">
                <img src="${photo.url}" alt="Photo from post" loading="lazy">
            </div>
        `;
    }

    // Delegate methods to main PostManager if available
    async toggleLike(postId) {
        if (window.postManager && window.postManager.toggleLike) {
            return window.postManager.toggleLike(postId);
        }
        // Fallback implementation
        try {
            const response = await fetch(`/posts/api/like/${postId}`, {
                method: 'POST',
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            const result = await response.json();
            if (result.success) {
                this.updateLikeButton(postId, result.isLiked);
            }
        } catch (error) {
            console.error('Error toggling like:', error);
        }
    }

    updateLikeButton(postId, isLiked) {
        const postElement = document.querySelector(`[data-post-id="${postId}"]`);
        const likeButton = postElement.querySelector('.post-action');

        if (isLiked) {
            likeButton.classList.add('liked');
        } else {
            likeButton.classList.remove('liked');
        }
    }

    togglePostMenu(postId) {
        if (window.postManager && window.postManager.togglePostMenu) {
            return window.postManager.togglePostMenu(postId);
        }

        const menu = document.getElementById(`post-menu-${postId}`);
        const isVisible = menu.style.display !== 'none';

        document.querySelectorAll('.dropdown-menu').forEach(m => m.style.display = 'none');
        menu.style.display = isVisible ? 'none' : 'block';
    }

    editPost(postId) {
        if (window.postManager && window.postManager.editPost) {
            return window.postManager.editPost(postId);
        }
        console.log('Edit post:', postId);
    }

    async deletePost(postId) {
        if (window.postManager && window.postManager.deletePost) {
            return window.postManager.deletePost(postId);
        }

        const result = await Swal.fire({
            title: 'Xác nhận xóa',
            text: 'Bạn có chắc chắn muốn xóa bài viết này?',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Xóa',
            cancelButtonText: 'Hủy'
        });

        if (result.isConfirmed) {
            try {
                const response = await fetch(`/posts/api/${postId}`, {
                    method: 'DELETE',
                    headers: { 'X-Requested-With': 'XMLHttpRequest' }
                });
                const data = await response.json();
                if (data.success) {
                    document.querySelector(`[data-post-id="${postId}"]`).remove();
                    this.showNotification('Xóa bài viết thành công!', 'success');
                }
            } catch (error) {
                console.error('Error deleting post:', error);
            }
        }
    }

    toggleComments(postId) {
        const commentsSection = document.getElementById(`comments-${postId}`);
        commentsSection.style.display = commentsSection.style.display === 'none' ? 'block' : 'none';
    }

    handleCommentKeyPress(event, postId) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.submitComment(postId);
        }
    }

    submitComment(postId) {
        const commentInput = document.querySelector(`#comments-${postId} .comment-input`);
        const content = commentInput.value.trim();
        if (content) {
            commentInput.value = '';
            this.showNotification('Bình luận đã được thêm!', 'success');
        }
    }

    viewImage(imageUrl) {
        if (window.postManager && window.postManager.viewImage) {
            return window.postManager.viewImage(imageUrl);
        }
        window.open(imageUrl, '_blank');
    }

    viewImages(imageUrls, startIndex = 0) {
        if (window.postManager && window.postManager.viewImages) {
            return window.postManager.viewImages(imageUrls, startIndex);
        }
        this.viewImage(imageUrls[startIndex]);
    }

    // Utility methods
    formatTimeAgo(dateString) {
        const date = new Date(dateString);
        const now = new Date();
        const diffInMs = now - date;
        const diffInMinutes = Math.floor(diffInMs / (1000 * 60));
        const diffInHours = Math.floor(diffInMinutes / 60);
        const diffInDays = Math.floor(diffInHours / 24);

        if (diffInMinutes < 1) return 'Vừa xong';
        if (diffInMinutes < 60) return `${diffInMinutes} phút trước`;
        if (diffInHours < 24) return `${diffInHours} giờ trước`;
        if (diffInDays < 7) return `${diffInDays} ngày trước`;

        return date.toLocaleDateString('vi-VN', {
            day: '2-digit', month: '2-digit', year: 'numeric'
        });
    }

    getCurrentUserAvatar() {
        const userAvatar = document.querySelector('.navbar .dropdown img');
        return userAvatar ? userAvatar.src : '/images/default-avatar.jpg';
    }

    showProfileLoading(show) {
        const loadingIndicator = document.getElementById('profile-loading-indicator');
        if (loadingIndicator) {
            loadingIndicator.style.display = show ? 'block' : 'none';
        }
    }

    showNotification(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-white bg-${type === 'success' ? 'success' : type === 'error' ? 'danger' : 'info'} border-0`;
        toast.setAttribute('role', 'alert');
        toast.style.cssText = 'position: fixed; top: 80px; right: 20px; z-index: 9999;';

        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;

        document.body.appendChild(toast);
        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();

        toast.addEventListener('hidden.bs.toast', () => toast.remove());
    }
}

// Initialize ProfilePostManager when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    if (document.getElementById('profile-posts-container') || document.getElementById('profile-photos-container')) {
        window.profilePostManager = new ProfilePostManager();
    }
});