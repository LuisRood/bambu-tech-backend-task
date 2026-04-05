package com.bambutec.taskapi.controller;

import com.bambutec.taskapi.dto.TaskResponse;
import com.bambutec.taskapi.exception.GlobalExceptionHandler;
import com.bambutec.taskapi.model.TaskStatus;
import com.bambutec.taskapi.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createTask_WhenRequestIsValid_ShouldReturnTask() throws Exception {
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Task 1")
                .description("Desc")
                .status(TaskStatus.PENDING)
                .userId(10L)
                .username("john")
                .build();

        when(taskService.createTask(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", "Task 1",
                                "description", "Desc",
                                "status", "PENDING"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Task 1"));
    }

    @Test
    void getTasks_ShouldReturnPagedTasks() {
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Task 1")
                .description("Desc")
                .status(TaskStatus.PENDING)
                .userId(10L)
                .username("john")
                .build();

        when(taskService.getTasks(any(), eq(TaskStatus.PENDING), any()))
                .thenReturn(new PageImpl<>(List.of(response)));

        var result = taskController.getTasks(null, TaskStatus.PENDING, 0, 10);

        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().getContent().size());
        assertEquals("Task 1", result.getBody().getContent().get(0).getTitle());
    }

    @Test
    void getTaskById_ShouldReturnTask() throws Exception {
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Task 1")
                .description("Desc")
                .status(TaskStatus.PENDING)
                .userId(10L)
                .username("john")
                .build();

        when(taskService.getTaskById(eq(1L), any())).thenReturn(response);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Task 1"));
    }

    @Test
    void updateTask_WhenRequestIsValid_ShouldReturnUpdatedTask() throws Exception {
        TaskResponse response = TaskResponse.builder()
                .id(1L)
                .title("Task Updated")
                .description("Desc Updated")
                .status(TaskStatus.IN_PROGRESS)
                .userId(10L)
                .username("john")
                .build();

        when(taskService.updateTask(eq(1L), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "title", "Task Updated",
                                "description", "Desc Updated",
                                "status", "IN_PROGRESS"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Task Updated"));
    }

    @Test
    void deleteTask_ShouldReturnNoContent() throws Exception {
        doNothing().when(taskService).deleteTask(eq(1L), any());

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createTask_WhenTitleIsMissing_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "description", "Desc",
                                "status", "PENDING"
                        ))))
                .andExpect(status().isBadRequest());
    }
}
