package com.bambutec.taskapi.service;

import com.bambutec.taskapi.model.TaskStatus;
import com.bambutec.taskapi.model.entity.Task;
import com.bambutec.taskapi.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {
    public Page<Task> getTasks(User currentUser, TaskStatus status, Pageable pageable);

    public void deleteTask(Long taskId, User currentUser);
}
