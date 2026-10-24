package org.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.Mapper.PostMapper;
import org.demo.Mapper.TagMapper;
import org.demo.Mapper.UserMapper;
import org.demo.dto.PostRequest;
import org.demo.dto.TagRequest;
import org.demo.dto.UserRequest;
import org.demo.dto.UserResponse;
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
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final TagMapper tagMapper;

    private final UserRepositoryJDBC userRepositoryJDBC;
    private final PostRepositoryJDBC postRepositoryJDBC;
    private final TagRepositoryJDBC tagRepositoryJDBC;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> users = userRepositoryJDBC.findAllUsers()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable(name = "id") Integer id) {
        User user = userRepositoryJDBC.findById(id);

        if (user == null) {
            throw new ResourceNotFoundException("User with id " + id + " not Found");
        }

        return ResponseEntity.ok().body(userMapper.toUserResponse(user));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        // both {user and savedUser} refer to same object
        User user = userMapper.toUser(userRequest); // {id,posts} are ignored in {User} Object
        user = userRepositoryJDBC.createUser(user);

        if (userRequest.getPosts() != null && !userRequest.getPosts().isEmpty()) {
            List<Post> listOfPost = new ArrayList<>();

            for (PostRequest postRequest : userRequest.getPosts()) {
                Post post = postMapper.toPost(postRequest);
                post.setUser(user);

                // Save the post to get an ID{both variables(savedPost and post) reference the same object}->check Impl
                Post savedPost = postRepositoryJDBC.createPost(post, user.getId());

                if (postRequest.getTags() != null && !postRequest.getTags().isEmpty()) {
                    List<Tag> savedTags = new ArrayList<>();
                    for (TagRequest tagRequest : postRequest.getTags()) {
                        Tag tag = tagMapper.toTag(tagRequest);
                        tag.setPost(savedPost);
                        savedTags.add(tagRepositoryJDBC.createTag(tag, savedPost.getId()));
                    }
                    savedPost.setTags(savedTags); // attach tags to this post
                }

                listOfPost.add(savedPost);
            }
            user.setPosts(listOfPost);
        }

        UserResponse userResponse = userMapper.toUserResponse(user);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(userResponse.getId())
                .toUri();

        return ResponseEntity.created(uri).body(userResponse);
    }

    @PutMapping
    public ResponseEntity<UserResponse> replaceUser(@RequestParam(name = "id") Integer userId, @Valid @RequestBody UserRequest userRequest) {
        User user = userMapper.toUser(userRequest);
        user.setId(userId);

        userRepositoryJDBC.update(user);
        return ResponseEntity.ok().body(userMapper.toUserResponse(user));
    }

    @PatchMapping
    public ResponseEntity<UserResponse> updateUser(@RequestParam(name = "id") Integer userId, @Valid @RequestBody UserRequest userRequest) {
        User exsistingUser = userRepositoryJDBC.findById(userId);
        User newUser = userMapper.toUser(userRequest);

        if (newUser.getAge() > 0) {
            exsistingUser.setAge(newUser.getAge());
        }

        if (newUser.getName() != null && !newUser.getName().isBlank()) {
            exsistingUser.setName(newUser.getName());
        }

        userRepositoryJDBC.update(exsistingUser);
        return ResponseEntity.ok().body(userMapper.toUserResponse(exsistingUser));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestParam(name = "id") Integer userId) {
        if (!userRepositoryJDBC.delete(userId)) {
            throw new ResourceNotFoundException("User with id " + userId + " not Found");
        }

        return ResponseEntity.noContent().build();
    }
}


