package com.codegym.socialmedia.service.status;

import com.codegym.socialmedia.dto.status.StatusDTO;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.LikeStatus;
import com.codegym.socialmedia.model.social_action.LikeStatusId;
import com.codegym.socialmedia.model.social_action.Status;
import com.codegym.socialmedia.repository.IUserRepository;
import com.codegym.socialmedia.repository.LikeStatusRepository;
import com.codegym.socialmedia.repository.StatusRepository;
import com.codegym.socialmedia.service.notification.LikeNotificationService;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatusServiceImpl implements StatusService {
    @Autowired
    private StatusRepository statusRepository;
//    private  UserFriendRepository userFriendRepository;

    @Autowired
    private LikeStatusRepository likeStatusRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private  UserService userService;

    @Override
    public List<StatusDTO> getFeeds() {
        List<Status> statuses = statusRepository.findAll();
        List<StatusDTO> dtos = new ArrayList<StatusDTO>();
        for (Status status : statuses) {
            int statusId = status.getId();
            StatusDTO dto = new StatusDTO();
            dto.setContent(status.getContent());
            dto.setLikeCount(getLikeCount(status.getId()));
            dto.setId(statusId);
            dto.setCurrentUserIsLiked(likeStatusRepository.existsById_UserIdAndId_StatusId(userService.getCurrentUser().getId(), statusId));
            dtos.add(dto);
        }
        return dtos;
    }

    @Autowired
    private LikeNotificationService likeNotificationService;

    @Override
    public boolean toggleLikeStatus(Integer statusId, Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        Status status = statusRepository.findById(statusId).orElse(null);
        LikeStatusId likeStatusId = new LikeStatusId();
        likeStatusId.setStatusId(statusId);
        likeStatusId.setUserId(userId);
        boolean isLiked = likeStatusRepository.findById(likeStatusId).isPresent();
        if (isLiked) {
            // Nếu đã like thì unlike
            likeStatusRepository.delete(likeStatusRepository.findById(likeStatusId).get());

            likeNotificationService.notifyLikeStatusChanged(statusId, getLikeCount(statusId), false);


            return false;
        } else {
            // Nếu chưa like thì thêm like
            LikeStatus like = new LikeStatus();
            like.setId(new LikeStatusId(userId, statusId));
            like.setUser(user);
            like.setStatus(status);

            likeStatusRepository.save(like);
            likeNotificationService.notifyLikeStatusChanged(statusId, getLikeCount(statusId), true);
            return true;
        }
    }


    @Override
    public int getLikeCount(Integer id) {
        return likeStatusRepository.countByStatusId(id);
    }
}
