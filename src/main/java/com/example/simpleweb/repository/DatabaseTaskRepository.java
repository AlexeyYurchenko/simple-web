package com.example.simpleweb.repository;

import com.example.simpleweb.exception.TaskNotFoundException;
import com.example.simpleweb.model.Task;
import com.example.simpleweb.repository.mapper.TaskRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class DatabaseTaskRepository implements TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Task> findAll() {
        log.debug("Calling DatabaseTaskRepository -> findAll");

        String sql = "SELECT * FROM task";

        return jdbcTemplate.query(sql, new TaskRowMapper());
    }

    @Override
    public Optional<Task> findById(Long id) {
        log.debug("Calling DatabaseTaskRepository -> findAll with ID: {}",id);

        String sql = "SELECT * FROM task WHERE ID = ?";
        Task task = DataAccessUtils.singleResult(
                jdbcTemplate.query(
                        sql,
                        new ArgumentPreparedStatementSetter(new Object[] {id}),
                        new RowMapperResultSetExtractor<>(new TaskRowMapper(),1)
                )
        );
        return Optional.ofNullable(task);
    }

    @Override
    public Task save(Task task) {
        log.debug("Calling DatabaseTaskRepository -> save with Task: {}",task);

        task.setId(System.currentTimeMillis());
        String sql = "INSERT INTO task (title, description, priority, id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, task.getTitle(),task.getPriority(),task.getPriority(),task.getId());

        return task;
    }

    @Override
    public Task update(Task task) {
        log.debug("Calling DatabaseTaskRepository -> update with Task: {}",task);
        Task existedTask = findById(task.getId()).orElse(null);
        if (existedTask != null) {
            String sql = "UPDATE task SET title = ?, description = ?, priority = ? WHERE id = ?";
            jdbcTemplate.update(sql,task.getTitle(),task.getPriority(),task.getPriority(),task.getId());
            return task;
        }
        log.warn("Task with ID {} not found", task.getId());
        throw new TaskNotFoundException("Task for update not found ID" + task.getId());
    }

    @Override
    public void delete(Long id) {
        log.debug("Calling DatabaseTaskRepository -> delete with ID: {}", id);
        String sql = "DELETE FROM task WHERE id = ?";
        jdbcTemplate.update(sql,id);

    }

    @Override
    public void batchInsert(List<Task> tasks) {
        log.debug("Calling DatabaseTaskRepository -> batchInsert");
        String sql = "INSERT INTO task (title, description, priority, id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Task task = tasks.get(i);
                ps.setString(1,task.getTitle());
                ps.setString(2,task.getDescription());
                ps.setInt(3,task.getPriority());
                ps.setLong(4,task.getId());

            }

            @Override
            public int getBatchSize() {
                return tasks.size();
            }
        });
    }
}
