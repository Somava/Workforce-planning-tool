package com.frauas.workforce_planning.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    /**
     * FK: departments.department_head_user_id -> users.id
     * This is the column you WRITE to.
     */
    @Column(name = "department_head_user_id")
    private Long departmentHeadUserId;

    /**
     * Read-only object mapping (convenience).
     * Uses the same column, but Hibernate will NOT insert/update via this field.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_head_user_id", insertable = false, updatable = false)
    private User departmentHead;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department other = (Department) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
