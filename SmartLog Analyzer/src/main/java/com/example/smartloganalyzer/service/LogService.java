package com.example.smartloganalyzer.service;

import com.example.smartloganalyzer.model.LogEntry;
import com.example.smartloganalyzer.repository.LogEntryRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

@Service
public class LogService {

    private final LogEntryRepository repository;

    public LogService(LogEntryRepository repository) {
        this.repository = repository;
    }

    // 🔁 Nouvelle méthode générique
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

    // 🔤 Parsing d’un fichier .log
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

    // 📊 Parsing d’un fichier .xlsx
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

    // 🆕 Utile pour Chart.js plus tard
    public Map<String, Long> getLogLevelCount() {
        List<LogEntry> all = repository.findAll();
        Map<String, Long> countMap = new HashMap<>();
        for (LogEntry log : all) {
            countMap.put(log.getLevel(), countMap.getOrDefault(log.getLevel(), 0L) + 1);
        }
        return countMap;
    }
}
