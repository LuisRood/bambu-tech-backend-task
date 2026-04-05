package com.bambutec.taskapi.service;

import com.bambutec.taskapi.dto.TaskRequest;
import com.bambutec.taskapi.dto.TaskResponse;
import com.bambutec.taskapi.model.TaskStatus;
import com.bambutec.taskapi.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    TaskResponse createTask(User currentUser, TaskRequest request);

    Page<TaskResponse> getTasks(User currentUser, TaskStatus status, Pageable pageable);

    TaskResponse getTaskById(Long taskId, User currentUser);

    TaskResponse updateTask(Long taskId, TaskRequest request, User currentUser);

    void deleteTask(Long taskId, User currentUser);
}
