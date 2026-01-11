package com.frauas.workforce_planning.model.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.List;
import java.math.BigDecimal;


@Entity
@Table(
    name = "external_employees",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_external_employee",
            columnNames = {"provider", "external_employee_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
public class ExternalEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_employee_id", nullable = false, length = 150)
    private String externalEmployeeId;

    @Column(nullable = false, length = 150)
    private String provider;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 255)
    private String email;

    @Type(JsonType.class)
    @Column(name = "skills", columnDefinition = "jsonb")
    private List<String> skills;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "wage_per_hour")
    private BigDecimal wagePerHour;
    // keep as IDs (fine)
    @Column(name = "staffing_request_id")
    private Long staffingRequestId;

    @Column(name = "project_id")
    private Long projectId;

    // let DB fill DEFAULT now()
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalEmployee other = (ExternalEmployee) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
