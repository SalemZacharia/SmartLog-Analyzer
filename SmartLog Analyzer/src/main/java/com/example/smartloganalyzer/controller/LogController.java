package com.example.smartloganalyzer.controller;

import com.example.smartloganalyzer.model.LogEntry;
import com.example.smartloganalyzer.repository.LogEntryRepository;
import com.example.smartloganalyzer.service.LogService;
import org.springframework.ui.Model;
import java.util.Map;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
public class LogController {

    private final LogService service;
    private final LogEntryRepository logEntryRepository;
    private final LogService logService;

    public LogController(LogService service, LogEntryRepository logEntryRepository, LogService logService) {
        this.service = service;
        this.logEntryRepository = logEntryRepository;
        this.logService = logService;
    }

    @GetMapping("/")
    public String home() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            service.parseAndSaveLogs(file.getInputStream(), file.getOriginalFilename());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/logs";
    }

    @GetMapping("/logs")
    public String showLogs(Model model) {
        List<LogEntry> logs = service.getAllLogs();
        Map<String, Long> logLevelCount = service.getLogLevelCount();
        Map<String, Object> metrics = service.getLogMetrics();

        model.addAttribute("logs", logs);
        model.addAttribute("logLevelCount", logLevelCount);
        model.addAttribute("metrics", metrics);
        List<String> anomalies = service.detectAnomalies();
        model.addAttribute("anomalies", anomalies);

        return "stats";
    }
    @GetMapping("/logs/explain/{id}")
    @ResponseBody
    public String explainError(@PathVariable Long id) {
        return logEntryRepository.findById(id)
                .filter(e -> e.getLevel().equalsIgnoreCase("ERROR"))
                .map(e -> logService.explainErrorMessage(e.getMessage()))
                .orElse("Not an error or not found.");
    }
}
