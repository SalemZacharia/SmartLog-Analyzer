package com.example.smartloganalyzer.service;

import com.example.smartloganalyzer.model.LogEntry;
import com.example.smartloganalyzer.repository.LogEntryRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

@Service
public class LogService {

    private final LogEntryRepository repository;

    public LogService(LogEntryRepository repository) {
        this.repository = repository;
    }


    public void parseAndSaveLogs(InputStream inputStream, String filename) {
        try {
            if (filename.endsWith(".log")) {
                parseLogText(inputStream);
            } else if (filename.endsWith(".xlsx")) {
                parseLogExcel(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void parseLogText(InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
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
    }


    private void parseLogExcel(InputStream inputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // ignore header

            Cell timestampCell = row.getCell(0);
            Cell levelCell = row.getCell(1);
            Cell messageCell = row.getCell(2);

            if (timestampCell == null || levelCell == null || messageCell == null) continue;

            String timestampStr = timestampCell.getStringCellValue().trim();
            String level = levelCell.getStringCellValue().trim();
            String message = messageCell.getStringCellValue().trim();

            LocalDateTime timestamp = LocalDateTime.parse(timestampStr, formatter);
            repository.save(new LogEntry(timestamp, level, message));
        }

        workbook.close();
    }

    public List<LogEntry> getAllLogs() {
        return repository.findAll();
    }


    public Map<String, Long> getLogLevelCount() {
        List<LogEntry> all = repository.findAll();
        Map<String, Long> countMap = new HashMap<>();
        for (LogEntry log : all) {
            countMap.put(log.getLevel(), countMap.getOrDefault(log.getLevel(), 0L) + 1);
        }
        return countMap;
    }
    public Map<String, Object> getLogMetrics() {
        List<LogEntry> logs = repository.findAll().stream()
                .sorted(Comparator.comparing(LogEntry::getTimestamp))
                .collect(Collectors.toList());

        Map<String, Object> metrics = new LinkedHashMap<>();

        if (logs.isEmpty()) return metrics;

        LocalDateTime start = logs.get(0).getTimestamp();
        LocalDateTime end = logs.get(logs.size() - 1).getTimestamp();
        long total = logs.size();
        long errors = logs.stream().filter(l -> l.getLevel().equals("ERROR")).count();
        long warnings = logs.stream().filter(l -> l.getLevel().equals("WARN")).count();
        long infos = logs.stream().filter(l -> l.getLevel().equals("INFO")).count();

        double errorRate = total > 0 ? (double) errors / total * 100 : 0;
        double warnRate = total > 0 ? (double) warnings / total * 100 : 0;

        // Moyenne d'intervalle entre erreurs
        List<LocalDateTime> errorTimes = logs.stream()
                .filter(l -> l.getLevel().equals("ERROR"))
                .map(LogEntry::getTimestamp)
                .toList();

        long avgIntervalBetweenErrors = 0;
        if (errorTimes.size() > 1) {
            List<Long> intervals = new ArrayList<>();
            for (int i = 1; i < errorTimes.size(); i++) {
                intervals.add(Duration.between(errorTimes.get(i - 1), errorTimes.get(i)).toSeconds());
            }
            avgIntervalBetweenErrors = (long) intervals.stream().mapToLong(Long::longValue).average().orElse(0);
        }

        Duration duration = Duration.between(start, end);
        long minutes = Math.max(duration.toMinutes(), 1);

        metrics.put("totalLogs", total);
        metrics.put("infoLogs", infos);
        metrics.put("warnLogs", warnings);
        metrics.put("errorLogs", errors);
        metrics.put("errorRate", String.format("%.2f", errorRate));
        metrics.put("warnRate", String.format("%.2f", warnRate));
        metrics.put("startTime", start);
        metrics.put("endTime", end);
        metrics.put("durationInMinutes", minutes);
        metrics.put("logPerMinute", total / minutes);
        metrics.put("avgIntervalBetweenErrorsInSec", avgIntervalBetweenErrors);

        return metrics;
    }
    
}
