package com.auth.service;

import com.auth.model.Tweet;
import com.auth.model.User;
import com.auth.model.UserFeed;
import com.auth.repository.UserFeedRepository;
import com.auth.repository.UserRepository;
import com.auth.service.UserRelationshipService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedService {
    private static final Logger logger = LoggerFactory.getLogger(FeedService.class);

    @Autowired
    private UserFeedRepository userFeedRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRelationshipService userRelationshipService;

    @Transactional
    public void addTweetToFeeds(Tweet tweet) {
        logger.info("Adding tweet {} to feeds", tweet.getId());
        
        // Get all followers of the tweet's author
        List<User> followers = userRelationshipService.getFollowers(tweet.getUser().getId());
        logger.info("Found {} followers for user {}", followers.size(), tweet.getUser().getId());
        
        // Add tweet to each follower's feed
        for (User follower : followers) {
            UserFeed feedItem = new UserFeed();
            feedItem.setUser(follower);
            feedItem.setTweet(tweet);
            feedItem.setCreatedAt(LocalDateTime.now());
            userFeedRepository.save(feedItem);
        }
        logger.info("Added tweet {} to {} follower feeds", tweet.getId(), followers.size());

        // Also add to the author's own feed
        UserFeed authorFeed = new UserFeed();
        authorFeed.setUser(tweet.getUser());
        authorFeed.setTweet(tweet);
        authorFeed.setCreatedAt(LocalDateTime.now());
        userFeedRepository.save(authorFeed);
        logger.info("Added tweet {} to author's feed", tweet.getId());
    }

    @Transactional(noRollbackFor = Exception.class)
    public void removeUserTweetsFromFeed(Long followerId, Long unfollowedUserId) {
        logger.info("Removing tweets from follower {} feed for user {}", followerId, unfollowedUserId);
        
        try {
            // First find the feed entries to delete
            List<UserFeed> feedEntries = userFeedRepository.findByUserIdAndTweetUserId(followerId, unfollowedUserId);
            logger.info("Found {} feed entries to remove", feedEntries.size());
            
            // Delete them one by one to avoid complex joins
            for (UserFeed entry : feedEntries) {
                userFeedRepository.delete(entry);
            }
            
            logger.info("Successfully removed {} tweets from feed", feedEntries.size());
        } catch (Exception e) {
            // Log error but don't rethrow - feed cleanup failure shouldn't prevent unfollow
            logger.error("Feed cleanup failed: {}. This is non-critical and unfollow will proceed.", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<Tweet> getFeedForUser(Long userId, Pageable pageable) {
        logger.info("Fetching feed for user {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
                
        Page<UserFeed> userFeeds = userFeedRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        List<Tweet> tweets = userFeeds.stream()
                .map(UserFeed::getTweet)
                .collect(Collectors.toList());
                
        logger.info("Found {} tweets in feed", tweets.size());
        return new PageImpl<>(tweets, pageable, userFeeds.getTotalElements());
    }
        @Transactional(readOnly = true)
    public Page<Tweet> getAllFeeds(Pageable pageable) {
        logger.info("Fetching feed for all users");
        Page<UserFeed> allFeeds = userFeedRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<Tweet> tweets = allFeeds.stream()
                .map(UserFeed::getTweet)
                .collect(Collectors.toList());
        logger.info("Found {} tweets in all users' feed", tweets.size());
        return new PageImpl<>(tweets, pageable, allFeeds.getTotalElements());
    }
}
