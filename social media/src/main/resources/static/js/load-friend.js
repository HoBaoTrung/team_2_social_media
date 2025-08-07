
let page = 1;
let isLoading = false;
let hasMoreData = true; // Biến theo dõi xem còn dữ liệu để tải hay không
const size = 10;
// const currentUserId = /*[[${currentUserId}]]*/ 0; // Thymeleaf inline
const targetUserId = /*[[${targetUserId}]]*/ 0; // Thymeleaf inline
const initialFilter = /*[[${filter}]]*/ 'all'; // Lấy giá trị filter từ Thymeleaf
let currentFilter = initialFilter;

$(document).ready(function () {
    // Thiết lập currentFilter từ giá trị server
    currentFilter = /*[[${filter}]]*/ 'all';
    console.log('Initial filter from server:', currentFilter);

    // Đảm bảo nav-link tương ứng có lớp active
    $('.sidebar .nav-link').removeClass('active');
    $(`.sidebar .nav-link[data-filter="${currentFilter}"]`).addClass('active');

    // Xử lý click vào sidebar
    $('.sidebar .nav-link').click(function (e) {
        $('.sidebar .nav-link').removeClass('active');
        $(this).addClass('active');

        currentFilter = $(this).data('filter') || 'all';

        page = 0;
        hasMoreData = true;
        $('#friends-list').empty();
        console.log('Filter changed to:', currentFilter);
        loadMoreFriends();
    });

    // Infinite scroll
    $(window).scroll(function () {
        if (hasMoreData && !isLoading &&
            $(window).scrollTop() + $(window).height() >= $(document).height() - 100) {
            loadMoreFriends();
        }
    });

    // Tải dữ liệu ban đầu nếu nội dung không đủ để cuộn
    if ($(document).height() <= $(window).height()) {
        loadMoreFriends();
    }

});

function loadMoreFriends() {
    if (!hasMoreData) return; // Không tải nếu đã hết dữ liệu

    isLoading = true;
    $('#loading').show();

    let url;
    switch (currentFilter) {
        case 'all':
            url = `/api/friends?targetUserId=${targetUserId}&page=${page}&size=${size}`;
            break;
        case 'mutual':
            url = `/api/friends/mutual?targetUserId=${targetUserId}&page=${page}&size=${size}`;
            break;
        case 'non-friends':
            url = `/api/friends/non-friends?page=${page}&size=${size}`;
            break;
        case 'sent-requests':
            url = `/api/friends/sent-requests?page=${page}&size=${size}`;
            break;
        case 'received-requests':
            url = `/api/friends/received-requests?page=${page}&size=${size}`;
            break;
        default:
            url = `/api/friends?targetUserId=${targetUserId}&page=${page}&size=${size}`;
    }

    $.ajax({
        url: url,
        method: 'GET',
        success: function (data) {
            if (!data.content || data.content.length === 0 || page >= data.totalPages) {
                hasMoreData = false; // Đánh dấu không còn dữ liệu
                $('#loading').hide();
                isLoading = false;
                return;
            }

            data.content.forEach(function (friend) {
                // Tạo HTML cho friendCard và friend-button-group
                const friendCardHtml = `
                    <div class="col-md-3">
                        <div class="friend-action">
                            <a href="/profile/${friend.username}" class="friend-card mt-2 mb-2 justify-content-center d-flex">
                                <img class="mb-3" src="${friend.avatarUrl || '/images/default-avatar.jpg'}" alt="Avatar" />
                                <div class="friend-info">
                                    <h5 class="name-list-friend" title="${friend.fullName}">${friend.fullName}</h5>
                                    <h7>${friend.mutualFriends} bạn chung</h7>
                                </div>
                            </a>
                            <div class="friend-button-group">
                                ${generateButtonGroup(friend)}
                            </div>
                        </div>
                    </div>
                `;
                $('#friends-list').append(friendCardHtml);
            });

            page++;
            isLoading = false;
            $('#loading').hide();
        },
        error: function (xhr) {
            isLoading = false;
            $('#loading').hide();
            hasMoreData = false; // Dừng tải nếu có lỗi
            console.error('Lỗi khi tải danh sách:', xhr.status, xhr.statusText);
            alert(`Lỗi khi tải danh sách: ${xhr.status} - ${xhr.statusText}`);
        }
    });
}

// Hàm tạo HTML cho friend-button-group
function generateButtonGroup(friend) {
    let buttons = '';

    if (currentFilter === 'all') {
        buttons = `
            <div id="acceptedBtnGroup" class="dropdown">
                <button class="btn btn-light dropdown-toggle" type="button" id="friendDropdownBtn-${friend.username}"
                        data-bs-toggle="dropdown" aria-expanded="false">
                    <i class="fa-solid fa-user-check"></i> Bạn bè
                </button>
                <ul class="dropdown-menu" aria-labelledby="friendDropdownBtn-${friend.username}">
                    <li>
                        <button class="btn btn-danger deleteFriendBtn" data-username="${friend.username}">
                            <i class="fa-solid fa-user-xmark"></i> Hủy kết bạn
                        </button>
                    </li>
                </ul>
            </div>
        `;
    } else if (currentFilter === 'sent-requests') {
        buttons = `
            <div id="pendingBtnGroup">
                <button class="btn btn-warning cancelFriendRequestBtn" data-username="${friend.username}">
                    <i class="fa-solid fa-user-minus"></i> Hủy lời mời
                </button>
            </div>
        `;
    } else if (currentFilter === 'received-requests') {
        buttons = `
            <div id="pendingBtnGroup">
                <button class="btn btn-success acceptFriendBtn" data-username="${friend.username}">
                    <i class="fa-solid fa-user-check"></i> Chấp nhận
                </button>
                <button class="btn btn-danger reflectFriendBtn mt-3" data-username="${friend.username}">
                    <i class="fa-solid fa-user-xmark"></i> Từ chối
                </button>
            </div>
        `;
    } else if (currentFilter === 'non-friends' && friend.allowFriendRequests) {
        buttons = `
            <div id="noneBtnGroup">
                <button class="btn btn-success addFriendBtn" data-username="${friend.username}">
                    <i class="fa-solid fa-user-plus"></i> Thêm bạn
                </button>
            </div>
        `;
    }else if (currentFilter === 'non-friends' && !friend.allowFriendRequests) {
        buttons = `
            <div id="noneBtnGroup">
                <button class="btn btn-secondary" disabled>Người này không bật chế độ kết bạn</button>
            </div>
        `;
    }

    return buttons;
}