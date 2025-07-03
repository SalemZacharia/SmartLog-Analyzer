SmartLog Analyzer

SmartLog Analyzer is a Spring Boot web application for analyzing log files (text and Excel formats), detecting anomalies, and visualizing key metrics through a user-friendly interface.

🌍 Features

🔢 Log Parsing & Storage

Supports .log and .xlsx file formats.

Automatically parses timestamp, log level (INFO, WARN, ERROR), and message.

Persists logs into an in-memory H2 database.

📊 Metrics & Analytics

Calculates:

Total logs.

Percentage of each log level.

First and last log timestamps.

Total session duration.

Logs per minute.

Average time between errors.

Visualizes log level distribution using Chart.js.

🔍 Anomaly Detection

Detects:

Critical errors followed by unexpected restarts.

Multiple consecutive warnings without intermediate info logs.

Frequent recurring errors after specific events.

Advanced rule-based anomaly detection with a modular rules engine.

🧠 Error Explanation (Optional AI)

When clicking ❓ on an error:

Simulates AI-generated explanations for possible root causes.

Example output: "This NullPointerException often occurs due to missing dependency injection."

🌐 User Interface

Simple and responsive layout with Thymeleaf.

Dashboard with statistics and anomaly list.

Logs table with colored levels and action buttons.

Navbar for navigation.

🛠️ Tech Stack

Backend: Spring Boot 3, Spring MVC, Spring Data JPA

Frontend: Thymeleaf, Chart.js, Bootstrap 5 (basic)

Database: H2 (in-memory)

Utilities: Apache POI (for Excel parsing)

🔄 Usage

1. Run the app

./mvnw spring-boot:run

2. Access the app

http://localhost:8080/

3. Upload a log file

Supports .log or .xlsx files.
