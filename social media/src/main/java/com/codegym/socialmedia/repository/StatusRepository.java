package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.admin.Admin;
import com.codegym.socialmedia.model.social_action.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Status, Integer> {
}
