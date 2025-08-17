// Posts JavaScript
class PostManager {
    constructor() {
        this.currentPage = 0;
        this.isLoading = false;
        this.hasMorePosts = true;
        this.selectedImages = [];
        this.editingPostId = null;
        this.imagesToDelete = [];
        this.form = document.getElementById("create-post-form");
        this.submitBtn = document.getElementById("post-submit-btn");
        const container = document.getElementById('posts-container');
        this.username = container.getAttribute('data-username');
        this.init();
    }

    init() {
        if (this.form != null && this.form != undefined) {
            this.form.addEventListener("submit", (e) => {
                this.handleSubmit();
            });
        }
        this.setupEventListeners();
        this.loadInitialPosts();
        this.setupInfiniteScroll();
    }

    handleSubmit() {
        // Disable nút
        this.submitBtn.disabled = true;
        this.submitBtn.innerText = "Đang đăng...";
        this.form.querySelectorAll("button").forEach(el => {
            if (el !== this.submitBtn) {
                el.disabled = true;
            }
        });
        this.form.querySelectorAll("input, textarea").forEach(el => {
            el.readOnly = true; // thay vì el.disabled = true
        });

    }

    setupEventListeners() {

        // Content input validation
        const contentInput = document.querySelector('.post-content-input');
        if (contentInput) {
            contentInput.addEventListener('input', () => this.validatePostForm());
        }

        // Image input
        const imageInput = document.getElementById('image-input');
        if (imageInput) {
            imageInput.addEventListener('change', (e) => this.handleImageSelection(e));
        }

        // Edit post form
        const editForm = document.getElementById('edit-post-form');
        if (editForm) {
            editForm.addEventListener('submit', (e) => this.handleEditPost(e));

        }

        // Edit modal events
        const editModal = document.getElementById('editPostModal');
        if (editModal) {
            editModal.addEventListener('hidden.bs.modal', () => this.resetEditForm());
        }

    }

    validatePostForm() {
        const content = document.querySelector('.post-content-input').value.trim();
        // const submitBtn = document.getElementById('post-submit-btn');

        if (content.length > 0 || this.selectedImages.length > 0) {
            this.submitBtn.disabled = false;
        } else {
            this.submitBtn.disabled = true;
        }

    }

    handleImageSelection(event) {
        const files = Array.from(event.target.files);

        // Gộp file mới vào selectedImages
        this.selectedImages = this.selectedImages.concat(files);

        // Update lại input file duy nhất bằng DataTransfer
        const dt = new DataTransfer();
        this.selectedImages.forEach(file => dt.items.add(file));
        event.target.files = dt.files;

        // Hiển thị preview
        this.displayImagePreview(this.selectedImages);
        this.validatePostForm();

    }

    displayImagePreview(files, containerId = 'image-preview-container', listId = 'image-preview-list') {
        const container = document.getElementById(containerId);
        const list = document.getElementById(listId);

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
                <button type="button" class="image-preview-remove" onclick="postManager.removeSelectedImage(${index})">
                    <i class="fas fa-times"></i>
                </button>
            `;
                list.appendChild(item);
            };
            reader.readAsDataURL(file);
        });
    }

    // displayImagePreview(files) {
    //     const container = document.getElementById('image-preview-container');
    //     const list = document.getElementById('image-preview-list');
    //
    //     if (files.length === 0) {
    //         container.style.display = 'none';
    //         return;
    //     }
    //
    //     container.style.display = 'block';
    //     list.innerHTML = '';
    //
    //     files.forEach((file, index) => {
    //         const reader = new FileReader();
    //         reader.onload = (e) => {
    //             const item = document.createElement('div');
    //             item.className = 'image-preview-item';
    //             item.innerHTML = `
    //                 <img src="${e.target.result}" alt="Preview">
    //                 <button type="button" class="image-preview-remove" onclick="postManager.removeSelectedImage(${index})">
    //                     <i class="fas fa-times"></i>
    //                 </button>
    //             `;
    //             list.appendChild(item);
    //         };
    //         reader.readAsDataURL(file);
    //     });
    // }

    removeSelectedImage(index) {
        this.selectedImages.splice(index, 1);
        this.displayImagePreview(this.selectedImages);
        this.validatePostForm();

        // Update file input
        const imageInput = document.getElementById('image-input');
        const dt = new DataTransfer();
        this.selectedImages.forEach(file => dt.items.add(file));
        imageInput.files = dt.files;
    }

    resetCreateForm() {
        const form = document.getElementById('create-post-form');
        form.reset();
        this.selectedImages = [];
        document.getElementById('image-preview-container').style.display = 'none';
        this.validatePostForm();
    }

    async loadInitialPosts() {
        this.currentPage = 0;
        this.hasMorePosts = true;
        const postsContainer = document.getElementById('posts-container');
        postsContainer.innerHTML = '';

        await this.loadPosts();
    }

    async loadPosts() {

        if (this.isLoading || !this.hasMorePosts || this.hasError) return;

        this.isLoading = true;
        this.showLoading(true);

        let controllerURL = `/posts/api/feed?page=${this.currentPage}&size=10`;

        if (this.username != null && this.username != undefined && this.username.trim() != '') {
            controllerURL = `/posts/api/user/${this.username}?page=${this.currentPage}&size=10`;
        }

        try {
            const data = await $.ajax({
                url: controllerURL,
                method: "GET",
                dataType: "json",
                xhrFields: {
                    withCredentials: true
                }
            });

            if (data.content && data.content.length > 0) {
                data.content.forEach(post => {
                    this.appendPost(post);
                });

                this.currentPage++;
                this.hasMorePosts = !data.last;
            } else {
                this.hasMorePosts = false;
                if (this.currentPage === 0) {
                    const noPostsEl = document.getElementById('profile-no-posts');
                    if (noPostsEl) {
                        noPostsEl.style.display = 'block';
                    }
                }
                this.showNoMorePosts();
            }
        } catch (error) {
            console.error('Error loading posts:', error);

            // Hiển thị thông báo lỗi chi tiết hơn
            const errorMessage = error.responseJSON?.message || error.statusText || 'Không thể tải bài viết';
            this.showNotification(errorMessage, 'error');

            this.hasError = true; // Ngăn không cho load tiếp khi có lỗi
        } finally {
            this.isLoading = false;
            this.showLoading(false);
        }
    }

    showNoMorePosts() {
        const noMorePosts = document.getElementById('no-more-posts');
        if (noMorePosts) {
            noMorePosts.style.display = 'block';
        }
    }

    setupInfiniteScroll() {
        window.addEventListener('scroll', () => {
            if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 100) {
                this.loadPosts();
            }
        });
    }

    appendPost(post) {
        const postsContainer = document.getElementById('posts-container');
        const postElement = this.createPostElement(post);
        postsContainer.insertAdjacentHTML('beforeend', postElement);
    }

    subscribeAllPosts() {
        document.querySelectorAll(".post-item").forEach(postEl => {
            const postId = postEl.getAttribute("data-post-id");
            this.subscribeToPostLikes(postId);
        });
    }

    createPostElement(post) {

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
                       <!-- Dropdown -->
                        <button class="btn btn-light btn-sm" type="button" id="dropdownMenuButton" 
                                  data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="fas fa-ellipsis-h"></i>
                          </button>
                          <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="dropdownMenuButton">
                            <li>
                             ${post.canEdit ? `<button class="dropdown-item" onclick="postManager.editPost(${post.id})">
                                    <i class="fas fa-edit"></i> Chỉnh sửa
                                </button>` : ''}
                            </li>
                            <li>
                                ${post.canDelete ? `<button  class="dropdown-item text-danger" onclick="postManager.deletePost(${post.id})">
                                    <i class="fas fa-trash"></i> Xóa
                                </button >` : ''}
                            </li>
                          </ul>
                    ` : ''}
                </div>
                
                <div class="post-content">${post.content}</div>
                
                ${imagesHtml}
                
                    <div class="post-stats">
                        <div class="post-likes">
                                <div class="post-likes-icon">
                                    <i class="fas fa-heart"></i>
                                </div>
                                <span>${post.likesCount} lượt thích</span>
                           
                        </div>
                        <div>
                                <span class="post-comments-count">${post.commentsCount} bình luận</span>
                        </div>
                    </div>
              
                
                <div class="post-actions">
                    <button class="post-action ${post.likedByCurrentUser ? 'liked' : ''}" 
                            onclick="postManager.toggleLike(${post.id})">
                        <i class="fas fa-heart"></i>
                        <span>Thích</span>
                    </button>
                    <button class="post-action" onclick="postManager.toggleComments(${post.id})">
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
                                     onkeypress="postManager.handleCommentKeyPress(event, ${post.id})"></textarea>
                            <button class="comment-submit" onclick="postManager.submitComment(${post.id})">
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
                        <img src="${imageUrls[0]}" alt="Post image" onclick="postManager.viewImage('${imageUrls[0]}')">
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
                     onclick="postManager.viewImages(${JSON.stringify(imageUrls).replace(/"/g, '&quot;')}, ${i})">
                    <img src="${imageUrls[i]}" alt="Post image">
                </div>
            `;
        }

        html += '</div></div>';
        return html;
    }

    // toggle like (gửi API)
    async toggleLike(postId) {
        try {
            const response = await fetch(`/posts/api/like/${postId}`, {
                method: 'POST',
                headers: {'X-Requested-With': 'XMLHttpRequest'}
            });

            const result = await response.json();

            if (result.success) {
                // cập nhật nút like ngay lập tức cho user hiện tại
                this.updateLikeButton(postId, result.isLiked);

                // số lượng like sẽ được cập nhật realtime qua websocket (subscribeToPostLikes)
                // không cần subscribe ở đây nữa
            } else {
                this.showNotification(result.message || 'Có lỗi xảy ra', 'error');
            }
        } catch (error) {
            console.error('Error toggling like:', error);
            this.showNotification('Có lỗi xảy ra', 'error');
        }
    }

    // cập nhật class liked của nút like
    updateLikeButton(postId, isLiked) {
        const likeBtn = document.querySelector(
            `.post-item[data-post-id="${postId}"] .post-action`
        );
        if (likeBtn) {
            if (isLiked) {
                likeBtn.classList.add("liked");
            } else {
                likeBtn.classList.remove("liked");
            }
        }
    }

    // subscribe cập nhật realtime likes qua websocket
    subscribeToPostLikes(postId) {
        stompClient.subscribe(`/topic/status/${postId}/likes`, (message) => {
            const data = JSON.parse(message.body);

            // update số lượng like
            const likesSpan = document.querySelector(
                `.post-item[data-post-id="${postId}"] .post-likes span`
            );

            if (likesSpan) {
                likesSpan.textContent = data.likeCount + " lượt thích";
            }

            // update nút like (cho user khác nhìn thấy realtime)
            this.updateLikeButton(data.postId, data.likedByCurrentUser);
        });
    }

    // hiện thông báo lỗi/thành công
    showNotification(msg, type = 'info') {
        alert(msg); // hoặc custom UI notification
    }

    async refreshPostStats(postId) {
        try {
            const response = await fetch(`/posts/api/${postId}`);
            const post = await response.json();

            const postElement = document.querySelector(`[data-post-id="${postId}"]`);
            const statsElement = postElement.querySelector('.post-stats');

            if (post.likesCount > 0 || post.commentsCount > 0) {
                statsElement.innerHTML = `
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
                `;
                statsElement.style.display = 'flex';
            } else {
                statsElement.style.display = 'none';
            }
        } catch (error) {
            console.error('Error refreshing post stats:', error);
        }
    }

    async editPost(postId) {
        try {
            const response = await fetch(`/posts/api/${postId}`);

            const post = await response.json();

            this.editingPostId = postId;
            this.imagesToDelete = [];

            // Populate modal
            document.getElementById('edit-post-id').value = postId;
            document.getElementById('edit-content').value = post.content;
            document.getElementById('edit-privacy-level').value = post.privacyLevel;

            // Display existing images
            this.displayExistingImages(post.imageUrls);

            // Show modal
            const modal = new bootstrap.Modal(document.getElementById('editPostModal'));
            modal.show();

        } catch (error) {
            console.error('Error loading post for edit:', error);
            this.showNotification('Không thể tải thông tin bài viết', 'error');
        }
    }

    displayExistingImages(imageUrls) {
        const container = document.getElementById('edit-existing-images');
        container.innerHTML = '';

        if (!imageUrls || imageUrls.length === 0) {
            return;
        }

        imageUrls.forEach((url, index) => {
            const item = document.createElement('div');
            item.className = 'existing-image-item';
            item.innerHTML = `
                <img src="${url}" alt="Existing image">
                <button type="button" class="existing-image-remove" 
                        onclick="postManager.markImageForDeletion('${url}', this)">
                    <i class="fas fa-times"></i>
                </button>
            `;
            container.appendChild(item);
        });
    }

    markImageForDeletion(imageUrl, button) {
        const item = button.closest('.existing-image-item');

        if (this.imagesToDelete.includes(imageUrl)) {
            // Remove from deletion list
            this.imagesToDelete = this.imagesToDelete.filter(url => url !== imageUrl);
            item.style.opacity = '1';
            button.style.background = '#f02849';
        } else {
            // Add to deletion list
            this.imagesToDelete.push(imageUrl);
            item.style.opacity = '0.5';
            button.style.background = '#28a745';
            button.innerHTML = '<i class="fas fa-undo"></i>';
        }
    }

    async handleEditPost(event) {
        event.preventDefault();

        const form = event.target;
        const formData = new FormData();

        formData.append('content', document.getElementById('edit-content').value);
        formData.append('privacyLevel', document.getElementById('edit-privacy-level').value);

        // Add existing images (not marked for deletion)
        const existingImages = Array.from(document.querySelectorAll('#edit-existing-images img'))
            .map(img => img.src)
            .filter(url => !this.imagesToDelete.includes(url));

        existingImages.forEach(url => formData.append('existingImages', url));

        // Add images to delete
        this.imagesToDelete.forEach(url => formData.append('imagesToDelete', url));

        // Add new images
        const newImagesInput = document.getElementById('edit-new-images');

        Array.from(newImagesInput.files).forEach(file => {
            formData.append('newImages', file);
        });

        const submitBtn = form.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerHTML;
        const elements = form.querySelectorAll('input, select, textarea, button');
        try {
            elements.forEach(element => {
                element.disabled = true;
            });
            // submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang lưu...';

            const response = await fetch(`/posts/api/update/${this.editingPostId}`, {
                method: 'PUT',
                body: formData
            });

            const result = await response.json();

            if (result.success) {
                // Close modal
                bootstrap.Modal.getInstance(document.getElementById('editPostModal')).hide();

                // Refresh the post
                await this.refreshPost(this.editingPostId);

                this.showNotification('Cập nhật bài viết thành công!', 'success');
            } else {
                this.showNotification(result.message || 'Có lỗi xảy ra', 'error');
            }
        } catch (error) {
            console.error('Error updating post:', error);
            this.showNotification('Có lỗi xảy ra khi cập nhật bài viết', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalText;
        }
    }

    async refreshPost(postId) {
        try {
            const response = await fetch(`/posts/api/${postId}`);
            const post = await response.json();

            const postElement = document.querySelector(`[data-post-id="${postId}"]`);
            const newPostHtml = this.createPostElement(post);
            postElement.outerHTML = newPostHtml;
        } catch (error) {
            console.error('Error refreshing post:', error);
        }
    }

    resetEditForm() {
        this.editingPostId = null;
        this.imagesToDelete = [];
        document.getElementById('edit-new-images').value = '';
    }

    async deletePost(postId) {
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
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                });

                const data = await response.json();

                if (data.success) {
                    // Remove post from DOM
                    const postElement = document.querySelector(`[data-post-id="${postId}"]`);
                    postElement.remove();

                    this.showNotification('Xóa bài viết thành công!', 'success');
                    this.updatePostsCount();
                } else {
                    this.showNotification(data.message || 'Có lỗi xảy ra', 'error');
                }
            } catch (error) {
                console.error('Error deleting post:', error);
                this.showNotification('Có lỗi xảy ra khi xóa bài viết', 'error');
            }
        }
    }

    toggleComments(postId) {
        const commentsSection = document.getElementById(`comments-${postId}`);
        const isVisible = commentsSection.style.display !== 'none';

        if (isVisible) {
            commentsSection.style.display = 'none';
        } else {
            commentsSection.style.display = 'block';
            this.loadComments(postId);
        }
    }

    async loadComments(postId) {
        // This would load comments from server
        // For now, we'll just show the comment form
        console.log('Loading comments for post', postId);
    }

    handleCommentKeyPress(event, postId) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            this.submitComment(postId);
        }
    }

    async submitComment(postId) {
        const commentInput = document.querySelector(`#comments-${postId} .comment-input`);
        const content = commentInput.value.trim();

        if (!content) return;

        try {
            // This would submit comment to server
            // For now, we'll just clear the input
            commentInput.value = '';
            this.showNotification('Bình luận đã được thêm!', 'success');

            // Refresh post stats to update comment count
            this.refreshPostStats(postId);
        } catch (error) {
            console.error('Error submitting comment:', error);
            this.showNotification('Có lỗi xảy ra khi gửi bình luận', 'error');
        }
    }

    viewImage(imageUrl) {
        // Open image in modal or lightbox
        const modal = document.createElement('div');
        modal.className = 'image-modal';
        modal.innerHTML = `
            <div class="image-modal-backdrop" onclick="this.parentElement.remove()">
                <div class="image-modal-content">
                    <img src="${imageUrl}" alt="Full size image">
                    <button class="image-modal-close" onclick="this.closest('.image-modal').remove()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            </div>
        `;

        // Add modal styles
        modal.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.9);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10000;
        `;

        const content = modal.querySelector('.image-modal-content');
        content.style.cssText = `
            position: relative;
            max-width: 90%;
            max-height: 90%;
        `;

        const img = modal.querySelector('img');
        img.style.cssText = `
            max-width: 100%;
            max-height: 100%;
            object-fit: contain;
        `;

        const closeBtn = modal.querySelector('.image-modal-close');
        closeBtn.style.cssText = `
            position: absolute;
            top: -40px;
            right: 0;
            background: rgba(255, 255, 255, 0.2);
            border: none;
            color: white;
            font-size: 24px;
            padding: 8px 12px;
            border-radius: 4px;
            cursor: pointer;
        `;

        document.body.appendChild(modal);
    }

    viewImages(imageUrls, startIndex = 0) {
        // Open image gallery modal
        console.log('Opening gallery:', imageUrls, 'starting at:', startIndex);
        this.viewImage(imageUrls[startIndex]);
    }

    formatTimeAgo(dateString) {
        // Tách ngày, tháng, năm và giờ, phút
        const [datePart, timePart] = dateString.split(" ");
        const [day, month, year] = datePart.split("/").map(Number);
        const [hour, minute] = timePart.split(":").map(Number);

        // Tạo đối tượng Date (lưu ý month phải -1 vì JS tính từ 0-11)
        const date = new Date(year, month - 1, day, hour, minute);

        const now = new Date();
        const diffInMs = now - date;
        const diffInMinutes = Math.floor(diffInMs / (1000 * 60));
        const diffInHours = Math.floor(diffInMinutes / 60);
        const diffInDays = Math.floor(diffInHours / 24);

        if (diffInMinutes < 1) {
            return 'Vừa xong';
        } else if (diffInMinutes < 60) {
            return `${diffInMinutes} phút trước`;
        } else if (diffInHours < 24) {
            return `${diffInHours} giờ trước`;
        } else if (diffInDays < 7) {
            return `${diffInDays} ngày trước`;
        } else {
            return date.toLocaleDateString('vi-VN', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric'
            });
        }
    }


    getCurrentUserAvatar() {
        // Get current user avatar from page context
        const userAvatar = document.querySelector('.navbar .dropdown img');
        return userAvatar ? userAvatar.src : '/images/default-avatar.jpg';
    }

    showLoading(show) {
        const loadingIndicator = document.getElementById('loading-indicator');
        if (loadingIndicator) {
            loadingIndicator.style.display = show ? 'block' : 'none';
        }
    }

    showNotification(message, type = 'info') {
        // Use Toast notification
        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-white bg-${type === 'success' ? 'success' : type === 'error' ? 'danger' : 'info'} border-0`;
        toast.setAttribute('role', 'alert');
        toast.style.cssText = 'position: fixed; top: 80px; right: 20px; z-index: 9999;';

        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" 
                        data-bs-dismiss="toast"></button>
            </div>
        `;

        document.body.appendChild(toast);

        const bsToast = new bootstrap.Toast(toast);
        bsToast.show();

        toast.addEventListener('hidden.bs.toast', () => {
            toast.remove();
        });
    }

    updatePostsCount() {
        // Update posts count in sidebar
        const postsCountEl = document.getElementById('posts-count');
        if (postsCountEl) {
            const currentCount = parseInt(postsCountEl.textContent) || 0;
            postsCountEl.textContent = currentCount + 1;
        }
    }
}
let stompClient = null;
// Initialize PostManager when DOM is loaded
document.addEventListener('DOMContentLoaded', function () {
    // connect WebSocket
    const socket = new SockJS('/ws'); // endpoint websocket spring boot
    stompClient = Stomp.over(socket);

    stompClient.connect({}, (frame) => {
        console.log('Connected: ' + frame);

        // 👉 chỉ subscribe sau khi connect thành công
        postManager.subscribeAllPosts();
    });

    window.postManager = new PostManager();

});