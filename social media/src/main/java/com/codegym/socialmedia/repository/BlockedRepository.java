// BlockedUsersRepository.java
package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.social_action.BlockedUsers;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockedRepository extends JpaRepository<BlockedUsers, Long> {
}
