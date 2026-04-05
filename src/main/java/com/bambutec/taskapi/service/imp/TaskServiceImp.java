package com.bambutec.taskapi.service.imp;


import com.bambutec.taskapi.model.entity.Task;
import com.bambutec.taskapi.model.TaskStatus;
import com.bambutec.taskapi.model.entity.User;
import com.bambutec.taskapi.exception.ForbiddenException;
import com.bambutec.taskapi.exception.ResourceNotFoundException;
import com.bambutec.taskapi.repository.TaskRepository;
import com.bambutec.taskapi.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskServiceImp implements TaskService {

    private final TaskRepository taskRepository;

    public Page<Task> getTasks(User currentUser, TaskStatus status, Pageable pageable) {
        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");

        if (isAdmin) {
            return status != null ? taskRepository.findByStatus(status, pageable)
                    : taskRepository.findAll(pageable);
        } else {
            return status != null ? taskRepository.findByUserIdAndStatus(currentUser.getId(), status, pageable)
                    : taskRepository.findByUserId(currentUser.getId(), pageable);
        }
    }

    public void deleteTask(Long taskId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));

        boolean isAdmin = currentUser.getRole().name().equals("ADMIN");
        boolean isOwner = task.getUser().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("No tienes permisos para eliminar esta tarea");
        }

        taskRepository.delete(task);
    }
}