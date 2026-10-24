package org.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.Mapper.PostMapper;
import org.demo.Mapper.TagMapper;
import org.demo.dto.PostRequest;
import org.demo.dto.PostResponse;
import org.demo.dto.TagRequest;
import org.demo.exception.ResourceNotFoundException;
import org.demo.model.Post;
import org.demo.model.Tag;
import org.demo.model.User;
import org.demo.repository.PostRepositoryJDBC;
import org.demo.repository.TagRepositoryJDBC;
import org.demo.repository.UserRepositoryJDBC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostRepositoryJDBC postRepositoryJDBC;
    private final UserRepositoryJDBC userRepositoryJDBC;
    private final TagRepositoryJDBC tagRepositoryJDBC;
    private final PostMapper postMapper;
    private final TagMapper tagMapper;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestParam(name = "user_id") Integer userId,
                                                   @Valid @RequestBody PostRequest postRequest) {
        User user = userRepositoryJDBC.findById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User with " + userId + " Not found");
        }

        Post post = postMapper.toPost(postRequest);
        post.setUser(user);

        post = postRepositoryJDBC.createPost(post, userId);
        if (postRequest.getTags() != null && !postRequest.getTags().isEmpty()) {
            List<Tag> listOfTags = new ArrayList<>();

            for (TagRequest tagRequest : postRequest.getTags()) {
                Tag tag = tagMapper.toTag(tagRequest);
                tag.setPost(post);

                tag = tagRepositoryJDBC.createTag(tag, post.getId());
                listOfTags.add(tag);
            }

            post.setTags(listOfTags);
        }

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(post.getId())
                .toUri();

        PostResponse postResponse = postMapper.toPostResponse(post);

        return ResponseEntity.created(uri).body(postResponse);
    }

    @GetMapping("/user/{userId}")
    public List<PostResponse> getPostByUserId(@PathVariable Integer userId) {
        User user = userRepositoryJDBC.findById(userId);

        if (user == null) {
            throw new ResourceNotFoundException("User with " + userId + " Not found");
        }

        return postRepositoryJDBC.findByUserId(userId)
                .stream()
                .map(postMapper::toPostResponse)
                .toList();
    }
}

