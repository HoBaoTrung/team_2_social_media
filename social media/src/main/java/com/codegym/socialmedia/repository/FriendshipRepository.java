package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.model.social_action.FriendshipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, FriendshipId> {

    @Query("""
            SELECT u FROM User u
            WHERE u.id IN (
                SELECT f.requester.id FROM Friendship f
                WHERE f.status = 'ACCEPTED'
                  AND f.addressee.id = :targetUserId
                  AND f.requester.id <> :viewerId
                UNION
                SELECT f.addressee.id FROM Friendship f
                WHERE f.status = 'ACCEPTED'
                  AND f.requester.id = :targetUserId
                  AND f.addressee.id <> :viewerId
            )
            """)
    List<User> findFriendsOfUserExcludingViewer(@Param("targetUserId") Long targetUserId,
                                                @Param("viewerId") Long viewerId);

    @Query("""
                SELECT f FROM Friendship f
                WHERE f.status = 'ACCEPTED'
                  AND (f.requester.id = :userId OR f.addressee.id = :userId)
            """)
    List<Friendship> findAllFriendshipsOfUser(@Param("userId") Long userId);

    @Query("""
                SELECT COUNT(f)
                FROM Friendship f
                WHERE f.status = 'ACCEPTED'
                  AND (f.requester.id = :userId OR f.addressee.id = :userId)
            """)
    long countFriendsByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Friendship f WHERE ((f.requester = :a AND f.addressee = :b) OR (f.requester = :b AND f.addressee = :a)) AND f.status = 'ACCEPTED'")
    Optional<Friendship> findAcceptedFriendshipBetween(@Param("a") User a, @Param("b") User b);
}
