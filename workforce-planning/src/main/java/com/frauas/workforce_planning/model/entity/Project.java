package com.frauas.workforce_planning.model.entity;

import com.frauas.workforce_planning.model.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 200)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String links;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProjectStatus status = ProjectStatus.PLANNED;

    @Column(nullable = false)
    private Boolean published = false;

    /**
     * DB default is now(). Let DB fill it.
     */
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * DB default is now(). But DB won't auto-update on UPDATE unless you add a trigger.
     * For now, keep DB-managed on insert; update it in code in @PreUpdate (below).
     */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (updatedAt == null) {
            updatedAt = OffsetDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private Set<StaffingRequest> staffingRequests = new HashSet<>();

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    private Set<Assignment> assignments = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project other = (Project) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
