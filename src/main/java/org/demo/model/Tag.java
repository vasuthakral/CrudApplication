package org.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;

    @ManyToMany(mappedBy = "tags")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Post> posts;

    public Tag(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
