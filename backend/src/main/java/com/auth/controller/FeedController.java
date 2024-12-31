package com.auth.controller;

import com.auth.model.Tweet;
import com.auth.service.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feeds")
public class FeedController {

    @Autowired
    private FeedService feedService;

    /**
     * Add a tweet to feeds of followers and the author.
     * @param tweetId ID of the tweet to be added.
     * @return ResponseEntity with success status.
     */
    @PostMapping("/tweets/{tweetId}")
    public ResponseEntity<Void> addTweetToFeeds(@PathVariable Long tweetId) {
        feedService.addTweetToFeeds(new Tweet()); // Assuming Tweet constructor takes ID.
        return ResponseEntity.ok().build();
    }

    /**
     * Remove tweets from a user's feed when unfollowing another user.
     * @param followerId ID of the user performing the unfollow.
     * @param unfollowedUserId ID of the user being unfollowed.
     * @return ResponseEntity with success status.
     */
    @DeleteMapping("/users/{followerId}/unfollow/{unfollowedUserId}")
    public ResponseEntity<Void> removeUserTweetsFromFeed(@PathVariable Long followerId, @PathVariable Long unfollowedUserId) {
        feedService.removeUserTweetsFromFeed(followerId, unfollowedUserId);
        return ResponseEntity.ok().build();
    }

    /**
     * Get the feed for a specific user.
     * @param userId ID of the user whose feed is being retrieved.
     * @param pageable Pageable object for pagination.
     * @return Page of tweets.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<Tweet>> getFeedForUser(@PathVariable Long userId, Pageable pageable) {
        Page<Tweet> feed = feedService.getFeedForUser(userId, pageable);
        return ResponseEntity.ok(feed);
    }

    /**
     * Get a feed of all tweets from all users.
     * @param pageable Pageable object for pagination.
     * @return Page of tweets.
     */
    @GetMapping("/all")
    public ResponseEntity<Page<Tweet>> getAllFeeds(Pageable pageable) {
        Page<Tweet> feed = feedService.getAllFeeds(pageable);
        return ResponseEntity.ok(feed);
    }
}
