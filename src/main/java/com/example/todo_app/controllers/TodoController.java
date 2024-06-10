package com.example.todo_app.controllers;

import com.example.todo_app.models.TodoItem;
import com.example.todo_app.services.TodoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.time.format.DateTimeFormatter;

import java.util.Optional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/")
public class TodoController {

    @Autowired
    private TodoService todoItemService;

    @PostMapping("/create")
    public ResponseEntity<TodoItem> createTodoItem(@Valid @RequestBody TodoItem todoItem, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        TodoItem savedItem = todoItemService.save(todoItem);
        return ResponseEntity.ok(savedItem);
    }

    @DeleteMapping("/item/{id}")
    public ResponseEntity<Void> deleteTodoItem(@PathVariable("id") Long id) {
        Optional<TodoItem> todoItem = todoItemService.getById(id);
        if (!todoItem.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        todoItemService.delete(todoItem.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/item/{id}")
    public ResponseEntity<Map<String, Object>> getTodoItem(@PathVariable("id") Long id) {
        Optional<TodoItem> todoItemOptional = todoItemService.getById(id);
        if (todoItemOptional.isPresent()) {
            TodoItem todoItem = todoItemOptional.get();

            // Create a new map to represent the formatted TodoItem
            Map<String, Object> todoMap = new HashMap<>();
            todoMap.put("id", todoItem.getId());
            todoMap.put("title", todoItem.getTitle());
            todoMap.put("description", todoItem.getDescription());

            // Format the updatedAt field
            Instant updatedAtInstant = todoItem.getCreatedAt();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a")
                    .withZone(ZoneId.systemDefault());
            String formattedUpdatedAt = formatter.format(updatedAtInstant);
            todoMap.put("updatedAt", formattedUpdatedAt);

            return ResponseEntity.ok(todoMap);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/item/{id}")
    public ResponseEntity<TodoItem> updateTodoItem(@PathVariable("id") Long id, @Valid @RequestBody TodoItem todoItem, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<TodoItem> existingItem = todoItemService.getById(id);
        if (!existingItem.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        TodoItem item = existingItem.get();

        // Update fields only if non-null values are provided in the request body
        if (todoItem.getDescription() != null) {
            item.setDescription(todoItem.getDescription());
        }
        if (todoItem.getTitle() != null) {
            item.setTitle(todoItem.getTitle());
        }
        if (todoItem.getCreatedAt() != null) {
            item.setCreatedAt(todoItem.getCreatedAt());
        }
        if (todoItem.getUpdatedAt() != null) {
            item.setUpdatedAt(todoItem.getUpdatedAt());
        }

        TodoItem updatedItem = todoItemService.save(item);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/")
    public Iterable<TodoItem> getAllTodoItems() {
        return todoItemService.getAll();
    }

    @GetMapping("/segregated-todos")
    public Map<String, List<Map<String, Object>>> getSegregatedTodos() {
        Iterable<TodoItem> allTodos = todoItemService.getAll();
        Map<String, List<Map<String, Object>>> segregatedTodos = new LinkedHashMap<>(); // Using LinkedHashMap to maintain insertion order

        segregatedTodos.put("today", new ArrayList<>());
        segregatedTodos.put("yesterday", new ArrayList<>());
        segregatedTodos.put("last 7 days", new ArrayList<>());
        segregatedTodos.put("last 30 days", new ArrayList<>());
        segregatedTodos.put("last one year", new ArrayList<>());

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate sevenDaysAgo = today.minusDays(7);
        LocalDate thirtyDaysAgo = today.minusDays(30);
        LocalDate oneYearAgo = today.minusYears(1);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd");

        for (TodoItem todo : allTodos) {
            LocalDate todoDate = Instant.ofEpochMilli(todo.getCreatedAt().toEpochMilli()).atZone(ZoneId.systemDefault()).toLocalDate();

            Map<String, Object> todoMap = new HashMap<>();
            todoMap.put("id", todo.getId());
            todoMap.put("title", todo.getTitle());
            todoMap.put("description", todo.getDescription());

            if (todoDate.equals(today)) {
                String formattedTime = Instant.ofEpochMilli(todo.getCreatedAt().toEpochMilli())
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
                        .format(timeFormatter);
                todoMap.put("createdAt", formattedTime);
                List<Map<String, Object>> todayList = segregatedTodos.get("today");
                // Add the todoMap to the beginning of the list to maintain descending order of ID
                todayList.add(0, todoMap);
                segregatedTodos.put("today", todayList);
            } else if (todoDate.equals(yesterday)) {
                String formattedTime = Instant.ofEpochMilli(todo.getCreatedAt().toEpochMilli())
                        .atZone(ZoneId.systemDefault())
                        .toLocalTime()
                        .format(timeFormatter);
                todoMap.put("createdAt", formattedTime);
                segregatedTodos.get("yesterday").add(todoMap);
            } else if (todoDate.isAfter(sevenDaysAgo)) {
                String formattedDay = todoDate.format(dayFormatter);
                todoMap.put("createdAt", formattedDay);
                segregatedTodos.get("last 7 days").add(todoMap);
            } else if (todoDate.isAfter(thirtyDaysAgo)) {
                String formattedDate = todoDate.format(dateFormatter);
                todoMap.put("createdAt", formattedDate);
                segregatedTodos.get("last 30 days").add(todoMap);
            } else if (todoDate.isAfter(oneYearAgo)) {
                String formattedDate = todoDate.format(dateFormatter);
                todoMap.put("createdAt", formattedDate);
                segregatedTodos.get("last one year").add(todoMap);
            }
        }

        return segregatedTodos;
    }
}
