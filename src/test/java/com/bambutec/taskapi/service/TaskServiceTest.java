package com.bambutec.taskapi.service;

import com.bambutec.taskapi.model.Role;
import com.bambutec.taskapi.model.entity.Task;
import com.bambutec.taskapi.model.entity.User;
import com.bambutec.taskapi.exception.ForbiddenException;
import com.bambutec.taskapi.repository.TaskRepository;
import com.bambutec.taskapi.service.imp.TaskServiceImp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImp taskServiceImp;

    @Test
    void deleteTask_WhenUserIsOwner_ShouldDeleteTask() {
        User user = User.builder().id(1L).role(Role.USER).build();
        Task task = Task.builder().id(1L).user(user).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskServiceImp.deleteTask(1L, user);
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void deleteTask_WhenUserIsNotOwner_ShouldThrowForbidden() {
        User owner = User.builder().id(1L).role(Role.USER).build();
        User otherUser = User.builder().id(2L).role(Role.USER).build();
        Task task = Task.builder().id(1L).user(owner).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(ForbiddenException.class, () -> taskServiceImp.deleteTask(1L, otherUser));
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void deleteTask_WhenUserIsAdmin_ShouldDeleteTask() {
        User owner = User.builder().id(1L).role(Role.USER).build();
        User admin = User.builder().id(2L).role(Role.ADMIN).build();
        Task task = Task.builder().id(1L).user(owner).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskServiceImp.deleteTask(1L, admin);
        verify(taskRepository, times(1)).delete(task);
    }
}