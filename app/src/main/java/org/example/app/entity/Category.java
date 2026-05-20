package org.example.app.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String icon;

    @Enumerated(EnumType.STRING)
    private CategoryType type;

    @ManyToOne
    private User user; // null = system category
}
