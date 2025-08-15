// js/posts-enhanced.js

class EnhancedPostManager {
    // ====== CONFIG / SELECTORS ======
    API_BASE = "/posts/api";
    SELECTORS = {
        formCreate: "#enhanced-create-post-form",
        inputImages: "#enhanced-image-input",
        inputVideos: "#enhanced-video-input",
        previewImages: "#enhanced-image-preview",
        previewVideos: "#enhanced-video-preview",
        feed: "#post-feed",
        postCard: ".post-card",
        reactionBtn: ".reaction-btn",
        shareBtn: ".share-btn",
        saveBtn: ".save-btn",
        pinBtn: ".pin-btn",
        commentLikeBtn: ".comment-like-btn",
        replyBtn: ".reply-btn",
        loadMoreRepliesBtn: ".load-more-replies",
    };

    // Map field name cho backend DTO (tuỳ dự án của bạn)
    FIELD_MAP = {
        content: "content",
        privacyLevel: "privacyLevel",
        feeling: "feeling",
        activity: "activity",
        location: "location",
        images: "images", // MultipartFile[] trong PostCreateDto
        videos: "videos"  // MultipartFile[] trong PostCreateDto
    };

    // Reaction types khớp enum server (LIKE, LOVE, HAHA, WOW, SAD, ANGRY)
    REACTIONS = [
        { type: "LIKE", emoji: "👍", label: "Thích" },
        { type: "LOVE", emoji: "❤️", label: "Yêu thích" },
        { type: "HAHA", emoji: "😂", label: "Haha" },
        { type: "WOW",  emoji: "😮", label: "Wow" },
        { type: "SAD",  emoji: "😢", label: "Buồn" },
        { type: "ANGRY",emoji: "😡", label: "Phẫn nộ" },
    ];

    constructor() {
        this.currentPage = 0;
        this.isLoading = false;
        this.hasMorePosts = true;

        this.selectedImages = [];
        this.selectedVideos = [];
        this.editingPostId = null;

        this.repliesPage = {}; // track page per commentId
        this.reactionPickerTimeout = null;

        this.init();
    }

    // ====== INIT ======
    init() {
        this.setupEventListeners();
        this.loadInitialPosts();
        this.setupInfiniteScroll();
        this.setupReactionPickers();
    }

    setupEventListeners() {
        // Create post form
        const createForm = document.querySelector(this.SELECTORS.formCreate);
        if (createForm) {
            createForm.addEventListener("submit", (e) => this.handleCreatePost(e));
        }

        // Media selection
        const imageInput = document.querySelector(this.SELECTORS.inputImages);
        const videoInput = document.querySelector(this.SELECTORS.inputVideos);

        if (imageInput) {
            imageInput.addEventListener("change", (e) => this.handleImageSelection(e));
        }
        if (videoInput) {
            videoInput.addEventListener("change", (e) => this.handleVideoSelection(e));
        }

        // Feeling / Activity / Location buttons (uỷ quyền sự kiện)
        document.addEventListener("click", (e) => {
            if (e.target.closest(".feeling-btn")) {
                const btn = e.target.closest(".feeling-btn");
                this.selectFeeling(btn.dataset.feeling);
            }
            if (e.target.closest(".activity-btn")) {
                const btn = e.target.closest(".activity-btn");
                this.selectActivity(btn.dataset.activity);
            }
            if (e.target.closest(".location-btn")) {
                this.selectLocation();
            }
        });

        // Global click handler cho các action trên post/comment
        document.addEventListener("click", (e) => {
            // Reaction click
            const reactionEl = e.target.closest(this.SELECTORS.reactionBtn);
            if (reactionEl) {
                this.handleReactionClick(reactionEl);
            }

            // Share
            const shareEl = e.target.closest(this.SELECTORS.shareBtn);
            if (shareEl) {
                this.openShareModal(shareEl.dataset.postId);
            }

            // Save
            const saveEl = e.target.closest(this.SELECTORS.saveBtn);
            if (saveEl) {
                this.toggleSavePost(saveEl.dataset.postId, saveEl);
            }

            // Pin
            const pinEl = e.target.closest(this.SELECTORS.pinBtn);
            if (pinEl) {
                this.togglePinPost(pinEl.dataset.postId, pinEl);
            }

            // Comment like
            const cLikeEl = e.target.closest(this.SELECTORS.commentLikeBtn);
            if (cLikeEl) {
                this.toggleCommentLike(cLikeEl.dataset.commentId, cLikeEl);
            }

            // Reply
            const replyEl = e.target.closest(this.SELECTORS.replyBtn);
            if (replyEl) {
                this.showReplyForm(replyEl.dataset.commentId);
            }

            // Load more replies
            const moreEl = e.target.closest(this.SELECTORS.loadMoreRepliesBtn);
            if (moreEl) {
                this.loadMoreReplies(moreEl.dataset.commentId, moreEl);
            }
        });

        // Long press cho mobile
        this.setupMobileReactions();
    }

    setupReactionPickers() {
        // Hover để mở picker
        document.addEventListener("mouseenter", (e) => {
            const btn = e.target.closest(this.SELECTORS.reactionBtn);
            if (btn) this.showReactionPicker(btn);
        }, true);

        // Rời chuột để đóng picker trễ 200ms (để hover vào picker)
        document.addEventListener("mouseleave", (e) => {
            const btn = e.target.closest(this.SELECTORS.reactionBtn);
            if (btn) this.hideReactionPicker(btn);
        }, true);
    }

    setupMobileReactions() {
        let pressTimer;
        document.addEventListener("touchstart", (e) => {
            const btn = e.target.closest(this.SELECTORS.reactionBtn);
            if (btn) {
                pressTimer = setTimeout(() => this.showReactionPicker(btn), 500);
            }
        });
        document.addEventListener("touchend", () => clearTimeout(pressTimer));
    }

    // ====== CREATE POST ======
    async handleCreatePost(event) {
        event.preventDefault();
        if (this.isLoading) return;

        const form = event.target;
        const formData = new FormData();

        const contentEl = form.querySelector(`[name="${this.FIELD_MAP.content}"]`);
        const privacyEl = form.querySelector(`[name="${this.FIELD_MAP.privacyLevel}"]`);
        const feelingEl = form.querySelector(`[name="${this.FIELD_MAP.feeling}"]`);
        const activityEl = form.querySelector(`[name="${this.FIELD_MAP.activity}"]`);
        const locationEl = form.querySelector(`[name="${this.FIELD_MAP.location}"]`);

        if (contentEl) formData.append(this.FIELD_MAP.content, contentEl.value || "");
        if (privacyEl) formData.append(this.FIELD_MAP.privacyLevel, privacyEl.value || "PUBLIC");
        if (feelingEl) formData.append(this.FIELD_MAP.feeling, feelingEl.value || "");
        if (activityEl) formData.append(this.FIELD_MAP.activity, activityEl.value || "");
        if (locationEl) formData.append(this.FIELD_MAP.location, locationEl.value || "");

        // Append images
        for (const file of this.selectedImages) {
            formData.append(this.FIELD_MAP.images, file);
        }
        // Append videos
        for (const file of this.selectedVideos) {
            formData.append(this.FIELD_MAP.videos, file);
        }

        try {
            this.isLoading = true;
            this.toggleFormState(form, true);

            const res = await this.apiPostForm(`${this.API_BASE}/create`, formData);
            if (!res.success) {
                this.toast(res.message || "Đăng bài thất bại", "error");
                return;
            }

            this.toast("Đăng bài thành công!", "success");
            // Reset form & preview
            form.reset();
            this.selectedImages = [];
            this.selectedVideos = [];
            this.clearPreview(this.SELECTORS.previewImages);
            this.clearPreview(this.SELECTORS.previewVideos);

            if (res.post) {
                // Chèn bài mới lên đầu feed
                const feed = document.querySelector(this.SELECTORS.feed);
                if (feed) {
                    const card = this.buildPostCard(res.post);
                    feed.insertAdjacentElement("afterbegin", card);
                }
            }
        } catch (err) {
            this.toast(err?.message || "Có lỗi xảy ra khi đăng bài", "error");
        } finally {
            this.toggleFormState(form, false);
            this.isLoading = false;
        }
    }

    handleImageSelection(e) {
        const files = Array.from(e.target.files || []);
        this.selectedImages = files;
        this.renderMediaPreview(files, this.SELECTORS.previewImages, "image");
    }

    handleVideoSelection(e) {
        const files = Array.from(e.target.files || []);
        this.selectedVideos = files;
        this.renderMediaPreview(files, this.SELECTORS.previewVideos, "video");
    }

    renderMediaPreview(files, containerSelector, kind) {
        const wrap = document.querySelector(containerSelector);
        if (!wrap) return;
        wrap.innerHTML = "";
        files.forEach((file) => {
            const url = URL.createObjectURL(file);
            const item = document.createElement(kind === "image" ? "img" : "video");
            item.src = url;
            item.className = "media-preview";
            item.width = 120;
            if (kind === "video") item.controls = true;
            wrap.appendChild(item);
        });
    }

    clearPreview(containerSelector) {
        const wrap = document.querySelector(containerSelector);
        if (wrap) wrap.innerHTML = "";
    }

    selectFeeling(feeling) {
        const form = document.querySelector(this.SELECTORS.formCreate);
        if (!form) return;
        const input = form.querySelector(`[name="${this.FIELD_MAP.feeling}"]`);
        if (input) input.value = feeling || "";
        this.toast(`Đang cảm thấy: ${feeling}`, "info");
    }

    selectActivity(activity) {
        const form = document.querySelector(this.SELECTORS.formCreate);
        if (!form) return;
        const input = form.querySelector(`[name="${this.FIELD_MAP.activity}"]`);
        if (input) input.value = activity || "";
        this.toast(`Đang ${activity}`, "info");
    }

    selectLocation() {
        const form = document.querySelector(this.SELECTORS.formCreate);
        if (!form) return;
        const input = form.querySelector(`[name="${this.FIELD_MAP.location}"]`);
        if (!input) return;

        if ("geolocation" in navigator) {
            navigator.geolocation.getCurrentPosition(
                (pos) => {
                    const { latitude, longitude } = pos.coords;
                    input.value = `${latitude.toFixed(6)},${longitude.toFixed(6)}`;
                    this.toast("Đã lấy vị trí hiện tại", "success");
                },
                () => {
                    const manual = prompt("Nhập vị trí (ví dụ: TP.HCM, Quận 1):", "");
                    if (manual != null) input.value = manual;
                },
                { enableHighAccuracy: true, timeout: 5000 }
            );
        } else {
            const manual = prompt("Nhập vị trí (ví dụ: TP.HCM, Quận 1):", "");
            if (manual != null) input.value = manual;
        }
    }

    // ====== FEED / INFINITE SCROLL ======
    async loadInitialPosts() {
        // Nếu server đã render sẵn trang đầu, có thể set currentPage = 1
        if (document.querySelector(this.SELECTORS.postCard)) {
            this.currentPage = 1;
            return;
        }
        await this.loadMorePosts();
    }

    setupInfiniteScroll() {
        window.addEventListener("scroll", async () => {
            if (!this.hasMorePosts || this.isLoading) return;
            const nearBottom = window.innerHeight + window.scrollY >= document.body.offsetHeight - 200;
            if (nearBottom) {
                await this.loadMorePosts();
            }
        });
    }

    async loadMorePosts() {
        try {
            this.isLoading = true;
            const page = this.currentPage;
            const res = await this.apiGet(`${this.API_BASE}/feed?page=${page}&size=10`);
            if (!res || !res.content) {
                this.hasMorePosts = false;
                return;
            }
            this.appendPostsToFeed(res.content || []);
            this.currentPage = (res.number ?? page) + 1;
            const totalPages = res.totalPages ?? (this.currentPage + 1);
            if (this.currentPage >= totalPages) this.hasMorePosts = false;
        } catch (err) {
            this.toast("Không tải được bài viết", "error");
        } finally {
            this.isLoading = false;
        }
    }

    appendPostsToFeed(posts) {
        const feed = document.querySelector(this.SELECTORS.feed);
        if (!feed) return;
        posts.forEach((p) => {
            const card = this.buildPostCard(p);
            feed.appendChild(card);
        });
    }

    buildPostCard(post) {
        // Tạo thẻ bài cơ bản; tuỳ HTML thực tế bạn điều chỉnh
        const card = document.createElement("div");
        card.className = "post-card";
        card.dataset.postId = post.id;

        const authorName = post.authorName || post.user?.username || "Người dùng";
        const createdAt = post.createdAtFormatted || post.createdAt || "";
        const content = this.escapeHtml(post.content || "");

        // Media (ảnh/video)
        let mediaHtml = "";
        if (post.imageUrls && post.imageUrls.length) {
            mediaHtml += `<div class="post-images">${post.imageUrls.map(u => `<img src="${u}" class="post-image" />`).join("")}</div>`;
        }
        if (post.videoUrls && post.videoUrls.length) {
            mediaHtml += `<div class="post-videos">${post.videoUrls.map(u => `<video src="${u}" controls class="post-video"></video>`).join("")}</div>`;
        }

        // Reaction summary
        const summary = post.reactionSummary || []; // [{reactionType,count}]
        const totalReacts = (summary || []).reduce((a, b) => a + (b.count || 0), 0);
        const commentsCount = post.commentsCount ?? 0;
        const sharesCount = post.sharesCount ?? 0;

        card.innerHTML = `
          <div class="post-header">
            <div class="post-author">${this.escapeHtml(authorName)}</div>
            <div class="post-time">${this.escapeHtml(createdAt)}</div>
          </div>
          <div class="post-content">${content.replace(/\n/g, "<br/>")}</div>
          ${mediaHtml}
          <div class="post-actions">
            <button class="reaction-btn" data-post-id="${post.id}" aria-label="Reaction">
              👍 <span class="reaction-btn-label">Thích</span>
            </button>
            <button class="share-btn" data-post-id="${post.id}">Chia sẻ</button>
            <button class="save-btn" data-post-id="${post.id}" data-saved="${post.isSaved ? "true" : "false"}">${post.isSaved ? "Đã lưu" : "Lưu"}</button>
            <button class="pin-btn" data-post-id="${post.id}" data-pinned="${post.isPinned ? "true" : "false"}">${post.isPinned ? "Đã ghim" : "Ghim"}</button>
          </div>
          <div class="post-stats" data-post-id="${post.id}">
            <span class="reaction-total"><span class="reaction-total-number">${totalReacts}</span> phản ứng</span>
            <span class="comment-total"><span class="comment-total-number">${commentsCount}</span> bình luận</span>
            <span class="share-total"><span class="share-total-number">${sharesCount}</span> chia sẻ</span>
          </div>
          <div class="post-comments" data-post-id="${post.id}">
            <div class="comments-list"></div>
            <form class="comment-form" data-post-id="${post.id}">
              <input type="text" name="content" placeholder="Viết bình luận..." />
              <button type="submit">Gửi</button>
            </form>
          </div>
        `;

        // Gắn submit cho form comment
        const cmtForm = card.querySelector(".comment-form");
        if (cmtForm) {
            cmtForm.addEventListener("submit", (e) => this.submitComment(e));
        }

        return card;
    }

    // ====== REACTIONS ======
    handleReactionClick(btn) {
        const postId = btn.dataset.postId;
        const picker = btn.querySelector(".reaction-picker");
        // Nếu picker đang hiển thị và user click vào nút chính -> toggle LIKE nhanh
        if (!picker) {
            this.toggleReaction(postId, "LIKE");
        }
    }

    showReactionPicker(btn) {
        if (btn.querySelector(".reaction-picker")) {
            btn.querySelector(".reaction-picker").classList.add("open");
            return;
        }
        const picker = document.createElement("div");
        picker.className = "reaction-picker";
        picker.setAttribute("role", "menu");

        picker.innerHTML = this.REACTIONS.map(r => `
            <button class="reaction-item" data-reaction-type="${r.type}" title="${r.label}" aria-label="${r.label}">
              <span class="emoji">${r.emoji}</span>
            </button>
        `).join("");

        picker.addEventListener("click", (e) => {
            const item = e.target.closest(".reaction-item");
            if (!item) return;
            const reactionType = item.dataset.reactionType;
            const postId = btn.dataset.postId;
            this.pickReaction(postId, reactionType);
        });

        picker.addEventListener("mouseenter", () => {
            if (this.reactionPickerTimeout) {
                clearTimeout(this.reactionPickerTimeout);
                this.reactionPickerTimeout = null;
            }
        });
        picker.addEventListener("mouseleave", () => {
            picker.classList.remove("open");
        });

        btn.appendChild(picker);
        requestAnimationFrame(() => picker.classList.add("open"));
    }

    hideReactionPicker(btn) {
        const picker = btn.querySelector(".reaction-picker");
        if (!picker) return;
        this.reactionPickerTimeout = setTimeout(() => {
            picker.classList.remove("open");
        }, 200);
    }

    async pickReaction(postId, reactionType) {
        await this.toggleReaction(postId, reactionType);
    }

    async toggleReaction(postId, reactionType) {
        try {
            const res = await this.apiPost(`${this.API_BASE}/${postId}/react?reactionType=${encodeURIComponent(reactionType)}`);
            if (res?.success) {
                // Cập nhật tổng hợp reaction
                await this.refreshReactionSummary(postId);
                // Cập nhật text nút theo reaction vừa chọn
                const btn = document.querySelector(`${this.SELECTORS.reactionBtn}[data-post-id="${postId}"]`);
                if (btn) {
                    const found = this.REACTIONS.find(r => r.type === reactionType);
                    btn.innerHTML = `${found ? found.emoji : "👍"} <span class="reaction-btn-label">${found ? found.label : "Thích"}</span>`;
                }
            } else {
                this.toast(res?.message || "Không thể thực hiện reaction", "error");
            }
        } catch (err) {
            this.toast("Lỗi khi gửi reaction", "error");
        }
    }

    async refreshReactionSummary(postId) {
        try {
            const summary = await this.apiGet(`${this.API_BASE}/${postId}/reactions`);
            // tổng
            const total = (summary || []).reduce((a, b) => a + (b.count || 0), 0);
            const statWrap = document.querySelector(`.post-stats[data-post-id="${postId}"]`);
            if (statWrap) {
                const totalEl = statWrap.querySelector(".reaction-total-number");
                if (totalEl) totalEl.textContent = total;
            }
        } catch {
            // ignore
        }
        // cập nhật comments/shares (stats)
        try {
            const stats = await this.apiGet(`${this.API_BASE}/${postId}/stats`);
            const statWrap = document.querySelector(`.post-stats[data-post-id="${postId}"]`);
            if (statWrap && stats) {
                const cEl = statWrap.querySelector(".comment-total-number");
                const sEl = statWrap.querySelector(".share-total-number");
                if (cEl && typeof stats.commentsCount === "number") cEl.textContent = stats.commentsCount;
                if (sEl && typeof stats.sharesCount === "number") sEl.textContent = stats.sharesCount;
            }
        } catch {
            // ignore
        }
    }

    // ====== SHARE / SAVE / PIN ======
    async openShareModal(postId) {
        // Nếu bạn có modal UI riêng, hook vào đây. Tạm thời prompt nhanh:
        const shareText = prompt("Nội dung chia sẻ:", "");
        if (shareText === null) return;
        try {
            const res = await this.apiPostJson(`${this.API_BASE}/${postId}/share`, { shareText });
            if (res?.success) {
                this.toast("Chia sẻ bài viết thành công!", "success");
                await this.refreshReactionSummary(postId);
            } else {
                this.toast(res?.message || "Chia sẻ thất bại", "error");
            }
        } catch {
            this.toast("Lỗi khi chia sẻ", "error");
        }
    }

    async toggleSavePost(postId, btnEl) {
        const isSaved = btnEl?.dataset.saved === "true";
        if (!isSaved) {
            let collection = prompt("Lưu vào bộ sưu tập (để trống dùng 'Đã lưu'):", "");
            if (collection === null) return; // user cancel
            if (!collection) collection = "";
            try {
                const res = await this.apiPost(`${this.API_BASE}/${postId}/save${collection ? `?collection=${encodeURIComponent(collection)}` : ""}`);
                if (res?.success) {
                    this.toast("Đã lưu bài viết", "success");
                    if (btnEl) {
                        btnEl.dataset.saved = "true";
                        btnEl.textContent = "Đã lưu";
                    }
                    await this.refreshReactionSummary(postId);
                } else {
                    this.toast(res?.message || "Lưu thất bại", "error");
                }
            } catch {
                this.toast("Lỗi khi lưu bài viết", "error");
            }
        } else {
            try {
                const res = await this.apiDelete(`${this.API_BASE}/${postId}/unsave`);
                if (res?.success) {
                    this.toast("Đã bỏ lưu bài viết", "success");
                    if (btnEl) {
                        btnEl.dataset.saved = "false";
                        btnEl.textContent = "Lưu";
                    }
                    await this.refreshReactionSummary(postId);
                } else {
                    this.toast(res?.message || "Bỏ lưu thất bại", "error");
                }
            } catch {
                this.toast("Lỗi khi bỏ lưu", "error");
            }
        }
    }

    async togglePinPost(postId, btnEl) {
        const isPinned = btnEl?.dataset.pinned === "true";
        try {
            const res = isPinned
                ? await this.apiDelete(`${this.API_BASE}/${postId}/unpin`)
                : await this.apiPost(`${this.API_BASE}/${postId}/pin`);
            if (res?.success) {
                if (isPinned) {
                    this.toast("Đã bỏ ghim bài viết", "success");
                    if (btnEl) {
                        btnEl.dataset.pinned = "false";
                        btnEl.textContent = "Ghim";
                    }
                } else {
                    this.toast("Đã ghim bài viết", "success");
                    if (btnEl) {
                        btnEl.dataset.pinned = "true";
                        btnEl.textContent = "Đã ghim";
                    }
                }
            } else {
                this.toast(res?.message || "Thao tác ghim/bỏ ghim thất bại", "error");
            }
        } catch {
            this.toast("Lỗi khi ghim/bỏ ghim", "error");
        }
    }

    // ====== COMMENTS ======
    async submitComment(e) {
        e.preventDefault();
        const form = e.target;
        const postId = form.dataset.postId;
        const input = form.querySelector('input[name="content"]');
        if (!input || !input.value.trim()) return;

        const body = new FormData();
        body.append("content", input.value.trim());

        try {
            const res = await this.apiPostForm(`${this.API_BASE}/${postId}/comments`, body);
            if (res?.success && res.comment) {
                const list = form.closest(".post-comments").querySelector(".comments-list");
                if (list) {
                    list.insertAdjacentHTML("beforeend", this.renderCommentItem(res.comment));
                }
                input.value = "";
                await this.refreshReactionSummary(postId);
            } else {
                this.toast(res?.message || "Không thể gửi bình luận", "error");
            }
        } catch {
            this.toast("Lỗi khi gửi bình luận", "error");
        }
    }

    renderCommentItem(cmt) {
        const author = this.escapeHtml(cmt.authorName || cmt.user?.username || "Người dùng");
        const content = this.escapeHtml(cmt.content || "");
        const createdAt = this.escapeHtml(cmt.createdAtFormatted || cmt.createdAt || "");
        const id = cmt.id;

        return `
          <div class="comment-item" data-comment-id="${id}">
            <div class="comment-author">${author}</div>
            <div class="comment-content">${content}</div>
            <div class="comment-meta">
              <span class="comment-time">${createdAt}</span>
              <button class="comment-like-btn" data-comment-id="${id}">Thích</button>
              <button class="reply-btn" data-comment-id="${id}">Trả lời</button>
              <button class="load-more-replies" data-comment-id="${id}" style="display:none;">Xem thêm phản hồi</button>
            </div>
            <div class="replies-list" data-comment-id="${id}"></div>
            <div class="reply-form-wrap" data-comment-id="${id}"></div>
          </div>
        `;
    }

    async toggleCommentLike(commentId, btnEl) {
        try {
            const res = await this.apiPost(`${this.API_BASE}/comments/${commentId}/like`);
            if (res?.success) {
                this.toast(res.liked ? "Đã thích bình luận" : "Đã bỏ thích bình luận", "success");
                if (btnEl) {
                    btnEl.textContent = res.liked ? "Đã thích" : "Thích";
                }
            } else {
                this.toast(res?.message || "Không thể thích bình luận", "error");
            }
        } catch {
            this.toast("Lỗi khi thích bình luận", "error");
        }
    }

    showReplyForm(commentId) {
        const wrap = document.querySelector(`.reply-form-wrap[data-comment-id="${commentId}"]`);
        if (!wrap) return;
        if (wrap.querySelector("form")) {
            wrap.querySelector("input[name='content']").focus();
            return;
        }
        wrap.innerHTML = `
          <form class="reply-form" data-parent-id="${commentId}">
            <input type="text" name="content" placeholder="Viết phản hồi..." />
            <button type="submit">Gửi</button>
          </form>
        `;
        wrap.querySelector("form").addEventListener("submit", (e) => this.submitReply(e));
    }

    async submitReply(e) {
        e.preventDefault();
        const form = e.target;
        const parentId = form.dataset.parentId;
        const content = form.querySelector('input[name="content"]').value.trim();
        if (!content) return;

        // Cần biết postId của comment cha -> lấy từ DOM gần nhất
        const commentItem = document.querySelector(`.comment-item[data-comment-id="${parentId}"]`);
        if (!commentItem) return;
        const postWrap = commentItem.closest(".post-comments");
        if (!postWrap) return;
        const postId = postWrap.dataset.postId;

        const payload = new FormData();
        payload.append("content", content);
        payload.append("parentCommentId", parentId);

        try {
            const res = await this.apiPostForm(`${this.API_BASE}/${postId}/comments`, payload);
            if (res?.success && res.comment) {
                const list = commentItem.querySelector(`.replies-list[data-comment-id="${parentId}"]`);
                if (list) list.insertAdjacentHTML("beforeend", this.renderCommentItem(res.comment));
                form.reset();
                await this.refreshReactionSummary(postId);
            } else {
                this.toast(res?.message || "Không thể gửi phản hồi", "error");
            }
        } catch {
            this.toast("Lỗi khi gửi phản hồi", "error");
        }
    }

    async loadMoreReplies(commentId, btnEl) {
        const page = this.repliesPage[commentId] || 0;
        try {
            const res = await this.apiGet(`${this.API_BASE}/comments/${commentId}/replies?page=${page}&size=5`);
            if (!res || !res.content) {
                if (btnEl) btnEl.style.display = "none";
                return;
            }
            const list = document.querySelector(`.replies-list[data-comment-id="${commentId}"]`);
            if (list) {
                res.content.forEach(c => list.insertAdjacentHTML("beforeend", this.renderCommentItem(c)));
            }
            this.repliesPage[commentId] = (res.number ?? page) + 1;
            const totalPages = res.totalPages ?? (this.repliesPage[commentId] + 1);
            if (this.repliesPage[commentId] >= totalPages && btnEl) btnEl.style.display = "none";
            if (btnEl && this.repliesPage[commentId] < totalPages) btnEl.style.display = "inline-block";
        } catch {
            this.toast("Không tải được phản hồi", "error");
        }
    }

    // ====== HELPERS ======
    getCsrf() {
        const tokenMeta = document.querySelector('meta[name="_csrf"]');
        const headerMeta = document.querySelector('meta[name="_csrf_header"]');
        const token = tokenMeta ? tokenMeta.getAttribute("content") : null;
        const header = headerMeta ? headerMeta.getAttribute("content") : null;
        return { token, header };
    }

    async apiGet(url) {
        const { token, header } = this.getCsrf();
        const headers = {};
        if (token && header) headers[header] = token;

        const res = await fetch(url, { headers, credentials: "same-origin" });
        if (res.status === 401) {
            this.toast("Bạn cần đăng nhập", "warning");
            return null;
        }
        return res.ok ? res.json() : null;
    }

    async apiPost(url) {
        const { token, header } = this.getCsrf();
        const headers = {};
        if (token && header) headers[header] = token;

        const res = await fetch(url, {
            method: "POST",
            headers,
            credentials: "same-origin"
        });
        if (res.status === 401) {
            this.toast("Bạn cần đăng nhập", "warning");
            return null;
        }
        return res.ok ? res.json() : await res.json().catch(() => null);
    }

    async apiPostJson(url, data) {
        const { token, header } = this.getCsrf();
        const headers = { "Content-Type": "application/json" };
        if (token && header) headers[header] = token;

        const res = await fetch(url, {
            method: "POST",
            headers,
            body: JSON.stringify(data),
            credentials: "same-origin",
        });
        if (res.status === 401) {
            this.toast("Bạn cần đăng nhập", "warning");
            return null;
        }
        return res.ok ? res.json() : await res.json().catch(() => null);
    }

    async apiPostForm(url, formData) {
        const { token, header } = this.getCsrf();
        const headers = {};
        if (token && header) headers[header] = token;

        const res = await fetch(url, {
            method: "POST",
            headers, // KHÔNG set Content-Type khi dùng FormData
            body: formData,
            credentials: "same-origin",
        });
        if (res.status === 401) {
            this.toast("Bạn cần đăng nhập", "warning");
            return null;
        }
        return res.ok ? res.json() : await res.json().catch(() => null);
    }

    async apiDelete(url) {
        const { token, header } = this.getCsrf();
        const headers = {};
        if (token && header) headers[header] = token;

        const res = await fetch(url, {
            method: "DELETE",
            headers,
            credentials: "same-origin",
        });
        if (res.status === 401) {
            this.toast("Bạn cần đăng nhập", "warning");
            return null;
        }
        return res.ok ? res.json() : await res.json().catch(() => null);
    }

    toggleFormState(form, disabled) {
        Array.from(form.elements).forEach((el) => (el.disabled = disabled));
        form.classList.toggle("is-loading", !!disabled);
    }

    toast(message, type = "info") {
        // Bạn có thể thay bằng lib toast. Tạm thời log + alert nhẹ
        console[type === "error" ? "error" : "log"]("[Toast]", message);
        // Optional mini toast
        const t = document.createElement("div");
        t.className = `toast toast-${type}`;
        t.textContent = message;
        Object.assign(t.style, {
            position: "fixed",
            right: "12px",
            bottom: "12px",
            padding: "10px 12px",
            background: type === "error" ? "#ef4444" : (type === "success" ? "#22c55e" : "#3b82f6"),
            color: "#fff",
            borderRadius: "8px",
            boxShadow: "0 6px 20px rgba(0,0,0,0.15)",
            zIndex: 9999,
            opacity: "0",
            transition: "opacity .2s ease",
        });
        document.body.appendChild(t);
        requestAnimationFrame(() => (t.style.opacity = "1"));
        setTimeout(() => {
            t.style.opacity = "0";
            setTimeout(() => t.remove(), 200);
        }, 1600);
    }

    escapeHtml(str) {
        return (str || "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;");
    }
}

// Khởi tạo khi DOM sẵn sàng
document.addEventListener("DOMContentLoaded", () => {
    window.enhancedPostManager = new EnhancedPostManager();
});

/* ====== GỢI Ý CSS TỐI THIỂU (tuỳ bạn đưa vào file .css) ======
.reaction-picker { position: absolute; display: flex; gap: 8px; padding: 6px; background:#fff; border:1px solid #eee; border-radius: 999px; box-shadow:0 10px 25px rgba(0,0,0,.1); transform: translateY(8px) scale(.95); opacity:0; pointer-events:none; transition:.12s; }
.reaction-picker.open { opacity:1; transform: translateY(0) scale(1); pointer-events:auto; }
.reaction-item { background:transparent; border:none; font-size:20px; cursor:pointer; }
.post-actions { display:flex; gap:10px; margin-top:8px; }
.post-stats { display:flex; gap:12px; font-size:.9rem; color:#555; margin:8px 0; }
.media-preview { margin-right:6px; border-radius:8px; }
=============================================================== */
