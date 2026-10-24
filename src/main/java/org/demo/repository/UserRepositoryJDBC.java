package org.demo.repository;

import org.demo.model.Post;
import org.demo.model.Tag;
import org.demo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class UserRepositoryJDBC {
    private final JdbcTemplate jdbcTemplate;

    public UserRepositoryJDBC(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private ResultSetExtractor<List<User>> userListExtractor = (rs) -> {

        Map<Integer, User> userMap = new LinkedHashMap<>();
        Map<Integer, Post> postMap = new HashMap<>();

        while (rs.next()) {
            Integer userId = rs.getInt("u_id");
            User user = userMap.get(userId);
            if (user == null) {
                user = new User();
                user.setId(userId);
                user.setName(rs.getString("u_name"));
                user.setAge(rs.getInt("u_age"));

                user.setPosts(new ArrayList<>());
                userMap.put(userId, user);
            }

            int postIdValue = rs.getInt("p_id");
            if (!rs.wasNull()) {
                Integer postId = postIdValue;
                Post post = postMap.get(postId);

                if (post == null) {
                    post = new Post();
                    post.setId(postId);
                    post.setTitle(rs.getString("p_title"));
                    post.setContent(rs.getString("p_content"));
                    post.setCreatedAt(rs.getObject("p_createdAt", LocalDateTime.class));

                    post.setUser(user);
                    post.setTags(new ArrayList<>());

                    postMap.put(postId, post);
                    user.getPosts().add(post);
                }

                int tagId = rs.getInt("t_id");
                if (!rs.wasNull()) {
                    Tag tag = new Tag();
                    tag.setId(tagId);
                    tag.setName(rs.getString("t_name"));

                    tag.setPost(post);
                    post.getTags().add(tag);
                }
            }
        }

        return new ArrayList<>(userMap.values());
    };

    public User createUser(User user) {
        String sql = "Insert into users(name,age) values (?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        PreparedStatementCreator psc = (conn) -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setInt(2, user.getAge());
            return ps;
        };

        jdbcTemplate.update(psc, keyHolder);

        // PostgreSQL returns all columns as generated keys, not just the id.
        // Using getKey() throws error when multiple keys are returned; use getKeys().get("id") instead.
        Map<String, Object> keys = keyHolder.getKeys();
        user.setId((Integer) keys.get("id"));
        return user;
    }

    // Single JOIN query to fetch all users with their posts and tags
    public List<User> findAllUsers() {
        String sql = """
                SELECT 
                    u.id AS u_id, u.name AS u_name, u.age AS u_age,
                    p.id AS p_id, p.title AS p_title, p.content AS p_content, p.created_at AS p_createdAt,
                    t.id AS t_id, t.name AS t_name
                FROM users u
                LEFT JOIN posts p ON u.id = p.user_id
                LEFT JOIN tags t ON p.id = t.post_id
                ORDER BY u.id, p.id, t.id
                """;
        return jdbcTemplate.query(sql, userListExtractor);
    }

    // Single JOIN query to fetch a single user with their posts and tags
    public User findById(Integer id) {
        String sql = """
                SELECT 
                    u.id AS u_id, u.name AS u_name, u.age AS u_age,
                    p.id AS p_id, p.title AS p_title, p.content AS p_content, p.created_at AS p_createdAt,
                    t.id AS t_id, t.name AS t_name
                FROM users u
                LEFT JOIN posts p ON u.id = p.user_id
                LEFT JOIN tags t ON p.id = t.post_id
                WHERE u.id = ?
                """;
        List<User> users = jdbcTemplate.query(sql, userListExtractor, id);
        return users.isEmpty() ? null : users.get(0);
    }

    public void update(User user) {
        String sql = "Update users set name=?, age=? where id=?";
        jdbcTemplate.update(sql, user.getName(), user.getAge(), user.getId());
    }

    public boolean delete(Integer id) {
        User deletedUser = findById(id);
        if (deletedUser == null) {
            return false;
        }

        // Clean up child tags and posts first to satisfy foreign key constraints in JDBC
        jdbcTemplate.update("delete from tags where post_id in (select id from posts where user_id = ?)", id);
        jdbcTemplate.update("delete from posts where user_id = ?", id);
        jdbcTemplate.update("delete from users where id = ?", id);

        return true;
    }
}
