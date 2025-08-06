// StatusService.java - Interface
package com.codegym.socialmedia.service.status;

import com.codegym.socialmedia.dto.*;
import org.springframework.data.domain.Page;

public interface StatusService {
    StatusResponseDto createStatus(StatusCreateDto dto);
    StatusResponseDto updateStatus(StatusUpdateDto dto);
    void deleteStatus(Long statusId);
    void togglePinStatus(Long statusId);
    StatusResponseDto getStatusById(Long statusId);
    Page<StatusResponseDto> getUserStatuses(Long userId, int page, int size);
    Page<StatusResponseDto> getNewsFeed(int page, int size);
    Page<StatusResponseDto> searchStatuses(StatusSearchDto searchDto);
}