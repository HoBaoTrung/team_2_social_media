package com.codegym.socialmedia.service.status;

import com.codegym.socialmedia.component.CloudinaryService;
import com.codegym.socialmedia.dto.*;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Status;
import com.codegym.socialmedia.repository.StatusRepository;
import com.codegym.socialmedia.service.user.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class StatusServiceImpl implements StatusService {

    @Autowired(required = false)
    private StatusRepository statusRepository;

    @Autowired
    private UserService userService;

    @Autowired(required = false)
    private CloudinaryService cloudinaryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public StatusResponseDto createStatus(StatusCreateDto dto) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Người dùng chưa đăng nhập");
        }

        StatusResponseDto response = new StatusResponseDto();
        response.setId(System.currentTimeMillis());
        response.setContent(dto.getContent());
        response.setPrivacyLevel(dto.getPrivacyLevel());
        response.setStatusType(Status.StatusType.TEXT);
        response.setLikeCount(0);
        response.setCommentCount(0);
        response.setShareCount(0);
        response.setLiked(false);
        response.setPinned(false);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        response.setUserId(currentUser.getId());
        response.setUserFullName(currentUser.getFirstName() + " " + currentUser.getLastName());
        response.setUserAvatar(currentUser.getProfilePicture());
        response.setUsername(currentUser.getUsername());

        if (dto.getImages() != null && !dto.getImages().isEmpty() && cloudinaryService != null) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : dto.getImages()) {
                if (!image.isEmpty()) {
                    String imageUrl = cloudinaryService.upload(image);
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl);
                    }
                }
            }
            response.setImageUrls(imageUrls);
            if (!imageUrls.isEmpty()) {
                response.setStatusType(Status.StatusType.IMAGE);
            }
        }

        if (dto.getVideo() != null && !dto.getVideo().isEmpty() && cloudinaryService != null) {
            String videoUrl = cloudinaryService.upload(dto.getVideo());
            response.setVideoUrl(videoUrl);
            response.setStatusType(Status.StatusType.VIDEO);
        }

        return response;
    }

    @Override
    public StatusResponseDto updateStatus(StatusUpdateDto dto) {
        StatusResponseDto response = getStatusById(dto.getId());
        response.setContent(dto.getContent());
        response.setPrivacyLevel(dto.getPrivacyLevel());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }

    @Override
    public void deleteStatus(Long statusId) {
        System.out.println("Status deleted: " + statusId);
    }

    @Override
    public void togglePinStatus(Long statusId) {
        System.out.println("Status pin toggled: " + statusId);
    }

    @Override
    public StatusResponseDto getStatusById(Long statusId) {
        User currentUser = userService.getCurrentUser();

        StatusResponseDto response = new StatusResponseDto();
        response.setId(statusId);
        response.setContent("Đây là nội dung mẫu của bài viết #" + statusId);
        response.setPrivacyLevel(Status.PrivacyLevel.PUBLIC);
        response.setStatusType(Status.StatusType.TEXT);
        response.setLikeCount(5);
        response.setCommentCount(2);
        response.setShareCount(1);
        response.setLiked(false);
        response.setPinned(false);
        response.setCreatedAt(LocalDateTime.now().minusHours(2));
        response.setUpdatedAt(LocalDateTime.now().minusHours(2));

        if (currentUser != null) {
            response.setUserId(currentUser.getId());
            response.setUserFullName(currentUser.getFirstName() + " " + currentUser.getLastName());
            response.setUserAvatar(currentUser.getProfilePicture());
            response.setUsername(currentUser.getUsername());
        }

        return response;
    }

    @Override
    public Page<StatusResponseDto> getUserStatuses(Long userId, int page, int size) {
        List<StatusResponseDto> mockStatuses = createMockStatuses(userId, size * 3);
        Pageable pageable = PageRequest.of(page, size);

        int start = Math.min((int) pageable.getOffset(), mockStatuses.size());
        int end = Math.min((start + pageable.getPageSize()), mockStatuses.size());

        return new PageImpl<>(mockStatuses.subList(start, end), pageable, mockStatuses.size());
    }

    @Override
    public Page<StatusResponseDto> getNewsFeed(int page, int size) {
        List<StatusResponseDto> mockStatuses = createMockNewsFeed(size * 3);
        Pageable pageable = PageRequest.of(page, size);

        int start = Math.min((int) pageable.getOffset(), mockStatuses.size());
        int end = Math.min((start + pageable.getPageSize()), mockStatuses.size());

        return new PageImpl<>(mockStatuses.subList(start, end), pageable, mockStatuses.size());
    }

    @Override
    public Page<StatusResponseDto> searchStatuses(StatusSearchDto searchDto) {
        List<StatusResponseDto> mockStatuses = createMockNewsFeed(50);
        List<StatusResponseDto> filteredStatuses = mockStatuses.stream()
                .filter(status -> status.getContent().toLowerCase()
                        .contains(searchDto.getQuery().toLowerCase()))
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize());

        int start = Math.min((int) pageable.getOffset(), filteredStatuses.size());
        int end = Math.min((start + pageable.getPageSize()), filteredStatuses.size());

        return new PageImpl<>(filteredStatuses.subList(start, end), pageable, filteredStatuses.size());
    }

    // === MOCK DATA GENERATORS ===

    private List<StatusResponseDto> createMockStatuses(Long userId, int count) {
        List<StatusResponseDto> statuses = new ArrayList<>();
        User user = getUserById(userId);

        for (int i = 1; i <= count; i++) {
            StatusResponseDto status = new StatusResponseDto();
            status.setId((long) i);
            status.setContent("Bài viết mẫu số " + i + " của " + user.getFirstName());
            status.setPrivacyLevel(Status.PrivacyLevel.PUBLIC);
            status.setStatusType(Status.StatusType.TEXT);
            status.setLikeCount((int) (Math.random() * 10));
            status.setCommentCount((int) (Math.random() * 5));
            status.setShareCount((int) (Math.random() * 3));
            status.setLiked(Math.random() > 0.5);
            status.setPinned(i == 1);
            status.setCreatedAt(LocalDateTime.now().minusHours(i));
            status.setUpdatedAt(LocalDateTime.now().minusHours(i));
            status.setUserId(user.getId());
            status.setUserFullName(user.getFirstName() + " " + user.getLastName());
            status.setUserAvatar(user.getProfilePicture());
            status.setUsername(user.getUsername());

            if (i % 3 == 0) {
                status.setImageUrls(List.of(
                        "https://picsum.photos/400/300?random=" + i,
                        "https://picsum.photos/400/300?random=" + (i + 100)
                ));
                status.setStatusType(Status.StatusType.IMAGE);
            }

            statuses.add(status);
        }

        return statuses;
    }

    private List<StatusResponseDto> createMockNewsFeed(int count) {
        List<StatusResponseDto> statuses = new ArrayList<>();
        List<User> allUsers = userService.getAllUsers();

        if (allUsers.isEmpty()) {
            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setFirstName("John");
            mockUser.setLastName("Doe");
            mockUser.setUsername("johndoe");
            mockUser.setProfilePicture("https://picsum.photos/100/100?random=1");
            allUsers = List.of(mockUser);
        }

        for (int i = 1; i <= count; i++) {
            User randomUser = allUsers.get((int) (Math.random() * allUsers.size()));

            StatusResponseDto status = new StatusResponseDto();
            status.setId((long) i);
            status.setContent(generateMockContent(i));
            status.setPrivacyLevel(Status.PrivacyLevel.PUBLIC);
            status.setStatusType(Status.StatusType.TEXT);
            status.setLikeCount((int) (Math.random() * 20));
            status.setCommentCount((int) (Math.random() * 10));
            status.setShareCount((int) (Math.random() * 5));
            status.setLiked(Math.random() > 0.7);
            status.setPinned(false);
            status.setCreatedAt(LocalDateTime.now().minusHours((int) (Math.random() * 24)));
            status.setUpdatedAt(status.getCreatedAt());
            status.setUserId(randomUser.getId());
            status.setUserFullName(randomUser.getFirstName() + " " + randomUser.getLastName());
            status.setUserAvatar(randomUser.getProfilePicture());
            status.setUsername(randomUser.getUsername());

            if (i % 4 == 0) {
                List<String> images = new ArrayList<>();
                for (int j = 0; j < (int) (Math.random() * 3) + 1; j++) {
                    images.add("https://picsum.photos/400/300?random=" + (i * 10 + j));
                }
                status.setImageUrls(images);
                status.setStatusType(Status.StatusType.IMAGE);
            }

            if (i % 7 == 0) {
                status.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
                status.setStatusType(Status.StatusType.VIDEO);
            }

            statuses.add(status);
        }

        statuses.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return statuses;
    }

    private String generateMockContent(int index) {
        String[] contents = {
                "Hôm nay thật là một ngày tuyệt vời!",
                "Vừa thưởng thức một tách cà phê ngon tuyệt!",
                "Đang học Spring Boot, framework rất mạnh mẽ!",
                "Cuối tuần rồi, ai có kế hoạch gì không?",
                "Chia sẻ kinh nghiệm lập trình Java...",
                "Thời tiết hôm nay thật dễ chịu.",
                "Vừa hoàn thành một dự án mới!",
                "Tìm quán ăn ngon ở Sài Gòn!",
                "Đang xem phim Marvel mới, tuyệt vời!",
                "Học mỗi ngày là điều quan trọng nhất!"
        };
        return contents[index % contents.length] + " #" + index;
    }

    private User getUserById(Long userId) {
        return userService.getAllUsers().stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElseGet(() -> {
                    User currentUser = userService.getCurrentUser();
                    if (currentUser != null) return currentUser;
                    User mockUser = new User();
                    mockUser.setId(userId);
                    mockUser.setFirstName("Mock");
                    mockUser.setLastName("User");
                    mockUser.setUsername("mockuser");
                    mockUser.setProfilePicture("https://picsum.photos/100/100?random=" + userId);
                    return mockUser;
                });
    }

    private String listToJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<String> jsonToList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}
