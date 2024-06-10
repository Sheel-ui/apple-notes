package com.example.todo_app.controllers;

import com.example.todo_app.models.TodoItem;
import com.example.todo_app.services.TodoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
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
    public ResponseEntity<TodoItem> getTodoItem(@PathVariable("id") Long id) {
        Optional<TodoItem> todoItem = todoItemService.getById(id);
        return todoItem.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
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
}
