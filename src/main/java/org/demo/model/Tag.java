package org.demo.model;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne
    @JoinColumn(name = "post_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Post post;

    public Tag(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
