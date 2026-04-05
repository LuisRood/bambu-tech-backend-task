package com.bambutec.taskapi.service.imp;


import com.bambutec.taskapi.dto.TaskRequest;
import com.bambutec.taskapi.dto.TaskResponse;
import com.bambutec.taskapi.model.Role;
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

    public TaskResponse createTask(User currentUser, TaskRequest request) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() == null ? TaskStatus.PENDING : request.getStatus())
                .user(currentUser)
                .build();

        return toResponse(taskRepository.save(task));
    }

    public Page<TaskResponse> getTasks(User currentUser, TaskStatus status, Pageable pageable) {
        boolean isAdmin = isAdmin(currentUser);
        Page<Task> tasks;

        if (isAdmin) {
            tasks = status != null ? taskRepository.findByStatus(status, pageable)
                    : taskRepository.findAll(pageable);
        } else {
            tasks = status != null ? taskRepository.findByUserIdAndStatus(currentUser.getId(), status, pageable)
                    : taskRepository.findByUserId(currentUser.getId(), pageable);
        }

        return tasks.map(this::toResponse);
    }

    public TaskResponse getTaskById(Long taskId, User currentUser) {
        Task task = findTaskOrThrow(taskId);

        if (!isAdmin(currentUser) && !isOwner(task, currentUser)) {
            throw new ForbiddenException("No estas autorizado para ver esta tarea");
        }

        return toResponse(task);
    }

    public TaskResponse updateTask(Long taskId, TaskRequest request, User currentUser) {
        Task task = findTaskOrThrow(taskId);

        if (!isOwner(task, currentUser)) {
            throw new ForbiddenException("No estas autorizado para editar esta tarea");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() == null ? task.getStatus() : request.getStatus());

        return toResponse(taskRepository.save(task));
    }

    public void deleteTask(Long taskId, User currentUser) {
        Task task = findTaskOrThrow(taskId);

        boolean isAdmin = isAdmin(currentUser);
        boolean isOwner = isOwner(task, currentUser);

        if (!isAdmin && !isOwner) {
            throw new ForbiddenException("No estas autorizado para eliminar esta tarea");
        }

        taskRepository.delete(task);
    }

    private Task findTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
    }

    private boolean isOwner(Task task, User currentUser) {
        return task.getUser().getId().equals(currentUser.getId());
    }

    private boolean isAdmin(User user) {
        return user.getRole() == Role.ADMIN;
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .userId(task.getUser().getId())
                .username(task.getUser().getUsername())
                .build();
    }
}