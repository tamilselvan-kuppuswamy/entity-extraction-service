
# 📦 Entity Extraction Service (Java + Spring Boot)

A Spring Boot microservice that extracts structured shipment entities (sender, receiver, address, package weight, delivery type, etc.) from text using **Azure OpenAI function calling**. Designed for conversational bots, mobile apps, and logistics platforms.

---

## 🚀 Features

- YAML-driven per-use-case field schemas (required/allowed fields for each business use case)
- Extracts entities from free-form text using Azure OpenAI (GPT-4 function-calling)
- SLF4J logging with `X-Correlation-ID` propagation for traceability
- Centralized global exception handling
- Supports easy integration with bot orchestrators and dialog systems
- OpenAPI/Swagger UI for instant API testing
- Multilingual-ready plumbing (future extensibility)

---

## 🛠️ Tech Stack

- Java 21
- Spring Boot 3.5.3
- Lombok
- Azure OpenAI Java SDK (`azure-ai-openai`)
- Jackson (JSON)
- Springdoc OpenAPI (Swagger UI)
- Maven

---

## ⚙️ Configuration

All configs are managed in `src/main/resources/application.yaml`.

```yaml
entity-config:
  cases:
    CreateShipment:
      allowedFields:
        - sender_name
        - sender_address
        - ...
      requiredFields:
        - sender_name
        - sender_address
        - ...
    TrackShipment:
      allowedFields:
        - tracking_number
        - ...
      requiredFields:
        - tracking_number
  confidenceThreshold: 0.8
  azureOpenAiEndpoint: https://YOUR_AZURE_OPENAI_ENDPOINT
  azureOpenAiKey: ${AZURE_OPENAI_KEY}
  model: gpt-4
```

- Set Azure endpoint and API key via environment variables or secrets (do **not** commit actual keys).

---

## 🔌 Usage

### **Build**
```bash
mvn clean package
```

### **Run**
```bash
java -jar target/entity-extraction-service-1.0.0.jar
```

### **API Endpoint**

**POST** `/extract-entities`

- **Request Example:**
    ```json
    {
      "text": "Send a parcel from John Smith, 123 Main St to Alice Lee, 456 Oak St. 2kg, express.",
      "useCase": "CreateShipment"
    }
    ```
- **Response Example:**
    ```json
    {
      "extractedEntities": {
        "sender_name": "John Smith",
        "receiver_name": "Alice Lee",
        "package_weight": "2kg",
        "delivery_type": "express"
      },
      "missingFields": [
        "receiver_address"
      ],
      "reason": "Azure OpenAI extraction",
      "language": "en"
    }
    ```

- **Headers:**
    - `X-Correlation-ID` (optional): For end-to-end tracing (recommended for distributed systems)

---

## 🔄 Integration Pattern

- Send raw user utterance and business use case (`useCase`) from your bot/app
- Receive extracted fields + missing fields in response
- Your bot orchestrator determines next prompt or business flow

---

## 📝 Best Practices

- Always forward `X-Correlation-ID` for traceable logs and distributed tracing
- Validate `useCase` against configured cases before invoking
- Never commit API keys or secrets—use environment variables or secure secret managers
- Use OpenAPI/Swagger UI (`/swagger-ui.html`) for live API tests and contract documentation

---

## 🛡️ Logging & Observability

- Every request and response is logged, tagged by correlation ID
- Errors routed through a global exception handler
- Ready for Azure Monitor, Application Insights, or cloud logging aggregation

---

## 🚦 Health & Quality

- Code formatting and style checks with Spotless and Checkstyle plugins
- CI/CD friendly (can be built/tested/deployed in any modern pipeline)
- Ready for Dockerization, Kubernetes, or Azure App Service deployments

---

## 🧑‍💻 Authors & Credits

Created by [Your Team/Company], with architecture best practices for modern AI logistics systems.

---

## 📚 References

- [Azure OpenAI Java SDK Docs](https://learn.microsoft.com/en-us/java/api/overview/azure/ai-openai-readme?view=azure-java-stable)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [OpenAPI Specification](https://swagger.io/specification/)