package com.bambutec.taskapi.service;

import com.bambutec.taskapi.dto.TaskRequest;
import com.bambutec.taskapi.model.Role;
import com.bambutec.taskapi.model.TaskStatus;
import com.bambutec.taskapi.model.entity.Task;
import com.bambutec.taskapi.model.entity.User;
import com.bambutec.taskapi.exception.ForbiddenException;
import com.bambutec.taskapi.exception.ResourceNotFoundException;
import com.bambutec.taskapi.repository.TaskRepository;
import com.bambutec.taskapi.service.imp.TaskServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImp taskServiceImp;

    @Test
    void createTask_WhenStatusIsNull_ShouldDefaultToPending() {
        User user = User.builder().id(1L).username("john").role(Role.USER).build();
        TaskRequest request = TaskRequest.builder()
                .title("New task")
                .description("Description")
                .status(null)
                .build();

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        var response = taskServiceImp.createTask(user, request);

        assertEquals(10L, response.getId());
        assertEquals(TaskStatus.PENDING, response.getStatus());
        assertEquals("john", response.getUsername());
    }

    @Test
    void getTasks_WhenAdminAndStatusProvided_ShouldUseFindByStatus() {
        User admin = User.builder().id(1L).username("admin").role(Role.ADMIN).build();
        User owner = User.builder().id(2L).username("john").role(Role.USER).build();
        Task task = Task.builder().id(1L).title("Task 1").status(TaskStatus.PENDING).user(owner).build();

        when(taskRepository.findByStatus(eq(TaskStatus.PENDING), any()))
                .thenReturn(new PageImpl<>(List.of(task)));

        var result = taskServiceImp.getTasks(admin, TaskStatus.PENDING, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(taskRepository, times(1)).findByStatus(eq(TaskStatus.PENDING), any());
    }

    @Test
    void getTasks_WhenAdminAndNoStatus_ShouldUseFindAll() {
        User admin = User.builder().id(1L).username("admin").role(Role.ADMIN).build();
        User owner = User.builder().id(2L).username("john").role(Role.USER).build();
        Task task = Task.builder().id(1L).title("Task 1").status(TaskStatus.PENDING).user(owner).build();

        when(taskRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(task)));

        var result = taskServiceImp.getTasks(admin, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(taskRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void getTasks_WhenUserAndStatusProvided_ShouldUseFindByUserAndStatus() {
        User user = User.builder().id(2L).username("john").role(Role.USER).build();
        Task task = Task.builder().id(1L).title("Task 1").status(TaskStatus.PENDING).user(user).build();

        when(taskRepository.findByUserIdAndStatus(eq(2L), eq(TaskStatus.PENDING), any()))
                .thenReturn(new PageImpl<>(List.of(task)));

        var result = taskServiceImp.getTasks(user, TaskStatus.PENDING, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(taskRepository, times(1)).findByUserIdAndStatus(eq(2L), eq(TaskStatus.PENDING), any());
    }

    @Test
    void getTasks_WhenUserAndNoStatus_ShouldUseFindByUserId() {
        User user = User.builder().id(2L).username("john").role(Role.USER).build();
        Task task = Task.builder().id(1L).title("Task 1").status(TaskStatus.PENDING).user(user).build();

        when(taskRepository.findByUserId(eq(2L), any())).thenReturn(new PageImpl<>(List.of(task)));

        var result = taskServiceImp.getTasks(user, null, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(taskRepository, times(1)).findByUserId(eq(2L), any());
    }

    @Test
    void getTaskById_WhenTaskNotFound_ShouldThrowNotFound() {
        User user = User.builder().id(1L).role(Role.USER).build();
        when(taskRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskServiceImp.getTaskById(100L, user));
    }

    @Test
    void getTaskById_WhenUserIsNotOwnerAndNotAdmin_ShouldThrowForbidden() {
        User owner = User.builder().id(1L).username("owner").role(Role.USER).build();
        User otherUser = User.builder().id(2L).username("other").role(Role.USER).build();
        Task task = Task.builder().id(1L).title("Task").status(TaskStatus.PENDING).user(owner).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(ForbiddenException.class, () -> taskServiceImp.getTaskById(1L, otherUser));
    }

    @Test
    void updateTask_WhenUserIsNotOwner_ShouldThrowForbidden() {
        User owner = User.builder().id(1L).username("owner").role(Role.USER).build();
        User otherUser = User.builder().id(2L).username("other").role(Role.USER).build();
        Task task = Task.builder().id(1L).title("Old").status(TaskStatus.PENDING).user(owner).build();
        TaskRequest request = TaskRequest.builder().title("New").description("Desc").status(TaskStatus.COMPLETED).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(ForbiddenException.class, () -> taskServiceImp.updateTask(1L, request, otherUser));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_WhenOwnerAndStatusIsNull_ShouldKeepCurrentStatus() {
        User owner = User.builder().id(1L).username("owner").role(Role.USER).build();
        Task task = Task.builder().id(1L).title("Old").description("Old desc").status(TaskStatus.IN_PROGRESS).user(owner).build();
        TaskRequest request = TaskRequest.builder().title("New").description("New desc").status(null).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = taskServiceImp.updateTask(1L, request, owner);

        assertEquals("New", response.getTitle());
        assertEquals("New desc", response.getDescription());
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void deleteTask_WhenUserIsOwner_ShouldDeleteTask() {
        User user = User.builder().id(1L).role(Role.USER).build();
        Task task = Task.builder().id(1L).title("Task 1").status(TaskStatus.PENDING).user(user).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskServiceImp.deleteTask(1L, user);
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void deleteTask_WhenUserIsNotOwner_ShouldThrowForbidden() {
        User owner = User.builder().id(1L).role(Role.USER).build();
        User otherUser = User.builder().id(2L).role(Role.USER).build();
        Task task = Task.builder().id(1L).title("Task 1").status(TaskStatus.PENDING).user(owner).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(ForbiddenException.class, () -> taskServiceImp.deleteTask(1L, otherUser));
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void deleteTask_WhenUserIsAdmin_ShouldDeleteTask() {
        User owner = User.builder().id(1L).role(Role.USER).build();
        User admin = User.builder().id(2L).role(Role.ADMIN).build();
        Task task = Task.builder().id(1L).title("Task 1").status(TaskStatus.PENDING).user(owner).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskServiceImp.deleteTask(1L, admin);
        verify(taskRepository, times(1)).delete(task);
    }
}
