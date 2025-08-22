package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.model.account.User;
import org.springframework.data.repository.Repository;

interface UserRepository extends Repository<User, Long> {
}
