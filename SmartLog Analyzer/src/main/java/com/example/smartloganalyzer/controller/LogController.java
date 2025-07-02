package com.example.smartloganalyzer.controller;

import com.example.smartloganalyzer.model.LogEntry;
import com.example.smartloganalyzer.repository.LogEntryRepository;
import com.example.smartloganalyzer.service.LogService;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
public class LogController {

    private final LogService service;

    public LogController(LogService service) {
        this.service = service;
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
        model.addAttribute("logs", logs);
        model.addAttribute("logLevelCount", logLevelCount);
        return "stats";
    }

}
