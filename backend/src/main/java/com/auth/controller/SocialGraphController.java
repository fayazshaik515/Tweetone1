package com.auth.controller;

import com.auth.model.User;
import com.auth.service.SocialGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/social")
@CrossOrigin(origins = "*")
public class SocialGraphController {
    private static final Logger logger = LoggerFactory.getLogger(SocialGraphController.class);
    
    private final SocialGraphService socialGraphService;

    @Autowired
    public SocialGraphController(SocialGraphService socialGraphService) {
        this.socialGraphService = socialGraphService;
    }

    @PostMapping("/follow/{followingId}")
    public ResponseEntity<?> followUser(
            @RequestAttribute("userId") Long followerId,
            @PathVariable Long followingId) {
        try {
            socialGraphService.followUser(followerId, followingId);
            return ResponseEntity.ok().build();
                } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/unfollow/{followingId}")
    public ResponseEntity<?> unfollowUser(
            @RequestAttribute("userId") Long followerId,
            @PathVariable Long followingId) {
        logger.info("⚡ Unfollow request - {} -> {}", followerId, followingId);
        try {
            socialGraphService.unfollowUser(followerId, followingId);
            logger.info("✅ Unfollow successful");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("❌ Unfollow failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/followers/{userId}")
    public ResponseEntity<List<User>> getFollowers(@PathVariable Long userId) {
        try {
            List<User> followers = socialGraphService.getFollowersList(userId);
            return ResponseEntity.ok(followers);
        } catch (IllegalArgumentException e) {
            logger.error("Error getting followers: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/following/{userId}")
    public ResponseEntity<List<User>> getFollowing(@PathVariable Long userId) {
        try {
            List<User> following = socialGraphService.getFollowingList(userId);
            return ResponseEntity.ok(following);
        } catch (IllegalArgumentException e) {
            logger.error("Error getting following: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/suggestions/{userId}")
    public ResponseEntity<Set<User>> getSuggestedUsers(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int maxSuggestions) {
        try {
            Set<User> suggestions = socialGraphService.getSuggestedUsers(userId, maxSuggestions);
            return ResponseEntity.ok(suggestions);
        } catch (IllegalArgumentException e) {
            logger.error("Error getting suggestions: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/feed-users/{userId}")
    public ResponseEntity<Set<User>> getFeedUsers(@PathVariable Long userId) {
        try {
            Set<User> feedUsers = socialGraphService.getFeedUsers(userId);
            return ResponseEntity.ok(feedUsers);
        } catch (IllegalArgumentException e) {
            logger.error("Error getting feed users: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/mutual/{user1Id}/{user2Id}")
    public ResponseEntity<Set<User>> getMutualConnections(
            @PathVariable Long user1Id,
            @PathVariable Long user2Id) {
        try {
            Set<User> mutualConnections = socialGraphService.getMutualConnections(user1Id, user2Id);
            return ResponseEntity.ok(mutualConnections);
        } catch (IllegalArgumentException e) {
            logger.error("Error getting mutual connections: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
