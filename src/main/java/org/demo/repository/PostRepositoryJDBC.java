package org.demo.repository;

import org.demo.model.Post;
import org.demo.model.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PostRepositoryJDBC {
    private final JdbcTemplate jdbcTemplate;

    PostRepositoryJDBC(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    RowMapper<Post> postRowMapper = (rs, rn) -> {
        return new Post(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getObject("created_at", LocalDateTime.class)
        );
    };

    private final ResultSetExtractor<List<Post>> postListExtractor = (rs) -> {
        Map<Integer, Post> postMap = new LinkedHashMap<>();

        while (rs.next()) {
            int postId = rs.getInt("p_id");
            Post post = postMap.get(postId);

            if (post == null) {
                post = new Post();
                post.setId(postId);
                post.setTitle(rs.getString("p_title"));
                post.setContent(rs.getString("p_content"));
                post.setCreatedAt(rs.getObject("p_createdAt", LocalDateTime.class));
                post.setTags(new ArrayList<>());
                postMap.put(postId, post);
            }

            int tagId = rs.getInt("t_id");
            if (!rs.wasNull()) {
                Tag tag = new Tag();
                tag.setId(tagId);
                tag.setName(rs.getString("t_name"));
                post.getTags().add(tag);
            }
        }

        return new ArrayList<>(postMap.values());
    };

    // Fetch all the {post and the tag}
    public List<Post> findByUserId(Integer userId) {
        String sql = """
                select p.id as p_id,p.title as p_title,p.content as p_content,p.created_at as p_createdAt,
                t.id as t_id,t.name as t_name
                from posts p
                left join tags t
                on p.id=t.post_id
                where p.user_id=?
                order by p.id
                """;

        return jdbcTemplate.query(sql, postListExtractor, userId);
    }

    // There is no tag field in post table
    public Post createPost(Post post,Integer userId) {
        String sql = "Insert into posts(title, content, created_at, user_id) values( ?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        PreparedStatementCreator psc = (conn) -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getContent());
            ps.setObject(3, post.getCreatedAt());
            ps.setInt(4, userId);
            return ps;
        };

        jdbcTemplate.update(psc, keyHolder);

        Map<String, Object> keys = keyHolder.getKeys();
        post.setId((Integer) keys.get("id"));
        return post;
    }
}
