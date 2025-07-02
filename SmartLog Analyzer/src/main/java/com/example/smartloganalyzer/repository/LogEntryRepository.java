package com.example.smartloganalyzer.repository;

import com.example.smartloganalyzer.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {
}