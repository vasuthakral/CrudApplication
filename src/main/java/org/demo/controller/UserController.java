package org.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.demo.Mapper.PostMapper;
import org.demo.Mapper.ProfileMapper;
import org.demo.Mapper.TagMapper;
import org.demo.Mapper.UserMapper;
import org.demo.dto.PostRequest;
import org.demo.dto.TagRequest;
import org.demo.dto.UserRequest;
import org.demo.dto.UserResponse;
import org.demo.exception.ResourceNotFoundException;
import org.demo.model.Post;
import org.demo.model.Profile;
import org.demo.model.Tag;
import org.demo.model.User;
import org.demo.repository.PostRepository;
import org.demo.repository.TagRepository;
import org.demo.repository.UserRepository;
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
    private final ProfileMapper profileMapper;

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable(name = "id") Integer id) {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            throw new ResourceNotFoundException("User with id " + id + " not Found");
        }
        return ResponseEntity.ok().body(userMapper.toUserResponse(user));
    }

    /*
     * One-to-Many:
     * We attach the parent Post to the child Tag because the tags table
     * contains a post_id foreign key referencing its parent Post.
     *
     * Many-to-Many:
     * We don't use tag.setPost(savedPost) because a Tag can belong to
     * multiple Posts. Instead, a separate post_tags join table stores
     * the relationships using post_id and tag_id.
     * With JDBC, we must insert these associations into the join table
     * explicitly; setting the Java object's list alone is not enough.
     */

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        User user = userMapper.toUser(userRequest); // {name,age}
        List<Post> listOfPost = new ArrayList<>();

        if (userRequest.getPosts() != null && !userRequest.getPosts().isEmpty()) {
            for (PostRequest postRequest : userRequest.getPosts()) {
                Post post = postMapper.toPost(postRequest); // {title,content,createdAt}
                post.setUser(user); // {user_id will be populated later by Hibernate in postObject as well}
                List<Tag> listOfTag = new ArrayList<>();

                if (postRequest.getTags() != null && !postRequest.getTags().isEmpty()) {
                    for (TagRequest tagRequest : postRequest.getTags()) {
                        Tag tag = tagMapper.toTag(tagRequest); // {name}
                        listOfTag.add(tag);
                    }
                }
                post.setTags(listOfTag); // Post is owning side of ManyToMany, so just set tags on post
                listOfPost.add(post);
            }
        }
        user.setPosts(listOfPost);

        // Handle profile (OneToOne)
        if (userRequest.getProfile() != null) {
            Profile profile = profileMapper.toProfile(userRequest.getProfile());
            profile.setUser(user);
            user.setProfile(profile);
        }

        User savedUser = userRepository.save(user);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();

        return ResponseEntity.created(uri).body(userMapper.toUserResponse(savedUser));
    }

    @PutMapping
    public ResponseEntity<UserResponse> replaceUser(@RequestParam(name = "id") Integer userId,
                                                    @Valid @RequestBody UserRequest userRequest) {
        User user = userMapper.toUser(userRequest);
        user.setId(userId);

        userRepository.save(user);
        return ResponseEntity.ok().body(userMapper.toUserResponse(user));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUser(@RequestParam(name = "id") Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException("User with id " + userId + " not Found");
        }

        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }
}
