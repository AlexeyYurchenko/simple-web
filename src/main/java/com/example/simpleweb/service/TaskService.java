package com.example.simpleweb.service;

import com.example.simpleweb.model.Task;

import java.util.List;

public interface TaskService {

    List<Task> findAll();

    Task findById(Long id);

    Task save(Task task);

    Task update(Task task);

    void delete(Long id);

    void batchInsert(List<Task> tasks);
}
