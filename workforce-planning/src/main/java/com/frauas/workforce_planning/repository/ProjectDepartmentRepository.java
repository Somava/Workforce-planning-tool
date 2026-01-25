package com.frauas.workforce_planning.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.frauas.workforce_planning.model.entity.ProjectDepartment;

@Repository
public interface ProjectDepartmentRepository extends JpaRepository<ProjectDepartment, Long> {

    //  Find all Department IDs where the user is either the Head or the Planner
    @Query("SELECT DISTINCT pd.department.id FROM ProjectDepartment pd " +
           "WHERE pd.departmentHeadUser.id = :userId OR pd.resourcePlannerUser.id = :userId")
    List<Long> findDepartmentIdsByUserId(@Param("userId") Long userId);

    ProjectDepartment findByProject_IdAndDepartment_Id(Long projectId, Long departmentId);

    Optional<ProjectDepartment> findByProject_IdAndDepartmentHeadUser_Id(Long projectId, Long deptHeadUserId);

    Optional<ProjectDepartment> findByProject_IdAndResourcePlannerUser_Id(Long projectId, Long resourcePlannerUserId);
}
