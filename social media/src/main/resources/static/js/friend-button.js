
document.addEventListener("DOMContentLoaded", () => {
    // Lấy tất cả các nút và chuyển thành mảng
    const buttons = {
        addFriend: Array.from(document.getElementsByClassName("addFriendBtn")),
        cancelRequest: Array.from(document.getElementsByClassName("cancelFriendRequestBtn")),
        reflectFriend: Array.from(document.getElementsByClassName("reflectFriendBtn")),
        deleteFriend: Array.from(document.getElementsByClassName("deleteFriendBtn")),
        acceptFriend: Array.from(document.getElementsByClassName("acceptFriendBtn"))
    };

    // Hàm gửi request chung
    const sendRequest = async (url, method, username, successMessage, errorMessage) => {
        try {
            const response = await fetch(url, {
                method,
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams({ user_name: username })
            });

            if (response.ok) {
                location.reload();
            } else {
                throw new Error(errorMessage);
            }
        } catch (error) {
            console.error('Lỗi:', error);
            alert(errorMessage);
        }
    };

    // Cấu hình các endpoint và thông báo
    const buttonConfigs = {
        addFriend: {
            url: '/addFriend',
            method: 'POST',
            errorMessage: 'Có lỗi xảy ra khi gửi lời mời!'
        },
        acceptFriend: {
            url: '/acceptFriend',
            method: 'PUT',
            errorMessage: 'Có lỗi xảy ra khi chấp nhận lời mời!'
        },
        deleteActions: {
            url: '/deleteFriend',
            method: 'DELETE',
            errorMessage: 'Có lỗi xảy ra khi xóa bạn!'
        }
    };

    // Gắn sự kiện cho nút thêm bạn
    buttons.addFriend.forEach(btn => {
        btn.addEventListener('click', () => {
            const username = btn.dataset.username;
            sendRequest(
                buttonConfigs.addFriend.url,
                buttonConfigs.addFriend.method,
                username,
                'Thêm bạn thành công',
                buttonConfigs.addFriend.errorMessage
            );
        });
    });

    // Gắn sự kiện cho nút chấp nhận bạn
    buttons.acceptFriend.forEach(btn => {
        btn.addEventListener('click', () => {
            const username = btn.dataset.username;
            sendRequest(
                buttonConfigs.acceptFriend.url,
                buttonConfigs.acceptFriend.method,
                username,
                'Chấp nhận bạn thành công',
                buttonConfigs.acceptFriend.errorMessage
            );
        });
    });

    // Gắn sự kiện cho các nút hủy/từ chối/xóa bạn
    [...buttons.cancelRequest, ...buttons.reflectFriend, ...buttons.deleteFriend].forEach(btn => {
        btn.addEventListener('click', () => {
            const username = btn.dataset.username;
            sendRequest(
                buttonConfigs.deleteActions.url,
                buttonConfigs.deleteActions.method,
                username,
                'Xóa thành công',
                buttonConfigs.deleteActions.errorMessage
            );
        });
    });
});