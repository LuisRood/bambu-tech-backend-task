package com.bambutec.taskapi.controller;

import com.bambutec.taskapi.model.entity.Task;
import com.bambutec.taskapi.model.TaskStatus;
import com.bambutec.taskapi.model.entity.User;
import com.bambutec.taskapi.service.imp.TaskServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskServiceImp taskService;

    @GetMapping
    public ResponseEntity<Page<Task>> getTasks(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Task> tasks = taskService.getTasks(currentUser, status, PageRequest.of(page, size));
        return ResponseEntity.ok(tasks);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        taskService.deleteTask(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}