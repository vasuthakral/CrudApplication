package org.demo.repository;

import org.demo.model.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

@Repository
public class TagRepositoryJDBC {
    private final JdbcTemplate jdbcTemplate;

    TagRepositoryJDBC(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    RowMapper<Tag> tagRowMapper = (rs, rn) -> {
        return new Tag(
                rs.getInt("id"),
                rs.getString("name")
        );
    };

    public Tag createTag(Tag tag, Integer postId) {
        String sql = "Insert into tags(name,post_id) values(?,?) ";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        PreparedStatementCreator psc = (conn) -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, tag.getName());
            ps.setInt(2, postId);
            return ps;
        };

        jdbcTemplate.update(psc, keyHolder);
        // PostgreSQL returns all columns as generated keys, not just the id.
        // Using getKey() throws error when multiple keys are returned; use getKeys().get("id") instead.
        Map<String, Object> keys = keyHolder.getKeys();
        tag.setId((Integer) keys.get("id"));

        return tag;
    }
}
