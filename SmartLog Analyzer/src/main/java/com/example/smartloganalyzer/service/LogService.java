package com.example.smartloganalyzer.service;


import com.example.smartloganalyzer.model.LogEntry;
import com.example.smartloganalyzer.repository.LogEntryRepository;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.*;
import java.util.List;

@Service
public class LogService {

    private final LogEntryRepository repository;

    public LogService(LogEntryRepository repository) {
        this.repository = repository;
    }

    public void parseAndSaveLogs(java.io.InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})\\s+(INFO|ERROR|WARN)\\s+(.*)");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    LocalDateTime timestamp = LocalDateTime.parse(matcher.group(1), formatter);
                    String level = matcher.group(2);
                    String message = matcher.group(3);

                    repository.save(new LogEntry(timestamp, level, message));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<LogEntry> getAllLogs() {
        return repository.findAll();
    }
}