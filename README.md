# Java Book Library

## About
This is a Java application designed to demonstrate a simple library management system. It utilizes the latest features of **Java 21** and is built with **Spring Boot** for rapid application development.

The application supports functionalities such as managing books, sending email notifications (using SMTP), and more.

---

## Prerequisites

Before running this application, ensure the following are installed on your system:
- **Java 21** or higher
- **Maven** (or use the provided Maven wrapper)
- SMTP credentials for sending email notifications

---

## Setup Guide

### 1. Clone the Repository
Clone this repository to your local machine:
```bash
git clone https://github.com/tulbadex/java-book-library
cd java-book-library
```

### 2. Configure Application Properties
The application requires configuration for database connection and email (SMTP) settings.
1. Navigate to the src/main/resources directory.
2. Copy the application-example.properties file and rename it to application.properties:
```bash
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

### 3. Edit the application.properties file and provide your SMTP details (e.g., host, port, username, and password).

Example:
```bash
spring.mail.host=smtp.your-email-provider.com
spring.mail.port=587
spring.mail.username=your-email@example.com
spring.mail.password=your-email-password
spring.mail.protocol=smtp
```

## How to Run the Application
Option 1: Using Maven Wrapper

```bash
./mvnw spring-boot:run
```

Option 2: Using Maven Directly

```bash
mvn spring-boot:run
```

The application will start on the default port 8080. You can access it in your browser at:

```bash
http://localhost:8080
```

## Features

- Library Management: Add, update, delete, and view books.
- Email Notifications: Automated email notifications for specific actions (requires SMTP configuration).
- Spring Boot Powered: Leverages the power of Spring Boot for rapid development and scalability.

## Contribution Guidelines

Contributions are welcome! If you'd like to contribute, please:

1. Fork the repository.
2. Create a feature branch: git checkout -b feature-name.
3. Commit your changes: git commit -m "Add a feature".
4. Push the branch: git push origin feature-name.
5. Open a pull request.

## Questions or Support

For any questions or issues, please (open an issue) [https://github.com/tulbadex/java-book-library/issues] on GitHub.

```bash
You can directly paste this into your `README.md` file. Let me know if there's anything else you'd like to add!
```