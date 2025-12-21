package com.frauas.workforce_planning.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frauas.workforce_planning.dto.WorkforceRequestDTO;
import com.frauas.workforce_planning.model.entity.RequestEntity;
import com.frauas.workforce_planning.model.enums.RequestStatus;
import com.frauas.workforce_planning.repository.RequestRepository;


@RestController
@RequestMapping("/api/requests")
public class WorkforceController {

    @Autowired
    private RequestRepository repository; // This connects to Postgres

    @PostMapping("/submit")
    public String submitRequest(@RequestBody WorkforceRequestDTO dto) {
        RequestEntity request = new RequestEntity();
        request.setPositionName(dto.positionName());
        request.setHeadCount(dto.headCount());
        request.setProjectId(dto.projectId()); // Pass the ID here
        request.setStatus(RequestStatus.DRAFT);

        repository.save(request);
        return "Saved request to project " + dto.projectId();
    }
}