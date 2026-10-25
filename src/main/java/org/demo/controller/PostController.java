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
import org.demo.repository.PostRepository;
import org.demo.repository.UserRepository;
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
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;
    private final TagMapper tagMapper;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestParam(name = "user_id") Integer userId,
            @Valid @RequestBody PostRequest postRequest) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException("User with " + userId + " Not found");
        }

        Post post = postMapper.toPost(postRequest); // {title,content}
        post.setUser(user);

        List<Tag> listOfTags = new ArrayList<>();
        if (postRequest.getTags() != null && !postRequest.getTags().isEmpty()) {
            for (TagRequest tagRequest : postRequest.getTags()) {
                Tag tag = tagMapper.toTag(tagRequest); // {name}
                listOfTags.add(tag);
            }
        }
        post.setTags(listOfTags); // only add for post side(owning side)
        Post savedPost = postRepository.save(post);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedPost.getId())
                .toUri();

        return ResponseEntity.created(uri).body(postMapper.toPostResponse(savedPost));
    }

    @GetMapping("/user/{userId}")
    public List<PostResponse> getPostByUserId(@PathVariable Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException("User with " + userId + " Not found");
        }

        return postRepository.findByUserId(userId)
                .stream()
                .map(postMapper::toPostResponse)
                .toList();
    }
}
