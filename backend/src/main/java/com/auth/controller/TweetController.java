package com.auth.controller;

import com.auth.model.Tweet;
import com.auth.service.FeedService;
import com.auth.service.TweetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tweets")
@CrossOrigin(origins = "http://localhost:3000")
public class TweetController {

    @Autowired
    private TweetService tweetService;

    @Autowired
    private FeedService feedService;

    @PostMapping
    public ResponseEntity<Tweet> createTweet(
            @RequestAttribute("userId") Long userId,
            @RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Tweet tweet = tweetService.createTweet(userId, content);
        return ResponseEntity.ok(tweet);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Tweet>> getUserTweets(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tweetService.getUserTweets(userId, page, size));
    }

    @GetMapping("/feed")
    // public ResponseEntity<Page<Tweet>> getFeed(
    //         @RequestAttribute("userId") Long userId,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "10") int size) {
    //     return ResponseEntity.ok(tweetService.getFeedForUser(userId, page, size));
    public ResponseEntity<Page<Tweet>> getAllFeeds(Pageable pageable) {
        Page<Tweet> feed = feedService.getAllFeeds(pageable);
        return ResponseEntity.ok(feed);
    }

    @DeleteMapping("/{tweetId}")
    public ResponseEntity<Void> deleteTweet(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long tweetId) {
        tweetService.deleteTweet(userId, tweetId);
        return ResponseEntity.ok().build();
    }
}
