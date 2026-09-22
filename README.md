# LeadProject

Spring Boot application for the AI Lead Agent MVP described in the product requirements document.

## Features

- Lead creation and assignment
- Excel import for leads
- Phone-based lead lookup
- Twilio outbound calling integration
- Plivo outbound calling integration
- Real-estate qualification call script
- Call transcript capture and sales brief generation
- Dashboard summary
- Campaign management
- Playbook configuration
- Compliance and suppression handling
- AI qualification endpoint
- Basic admin/security setup

## Swagger UI

```text
http://localhost:8080/swagger-ui.html
```

## API flow for the real-estate business use case

1. Upload leads: `POST /api/v1/leads/import`
2. Generate call plan: `POST /api/v1/leads/call-plan`
3. Start Twilio call: `POST /api/v1/leads/call` with `type` set to `iv` for the fixed questions or `aiagent` for the OpenAI conversation
4. Save transcript: `POST /api/v1/calls/{callId}/transcript`
5. Qualify lead: `POST /api/v1/leads/{leadId}/qualify`
6. Create sales brief: `POST /api/v1/leads/{leadId}/sales-brief`
7. Assign to salesperson: `POST /api/v1/leads/{leadId}/assign`
8. Check dashboard: `GET /api/v1/dashboard`

## Excel template

Use the file `ui/excel-template.csv` or create an Excel file with these headers:

```csv
Name,Phone,Email,Source
Aisha Rahman,+971555123456,aisha@gmail.com,website_form
```

## Voice provider configuration

Set the following environment variables for the provider you want to use.

### Twilio

```bash
TWILIO_ACCOUNT_SID=ACxxxxxxxx
TWILIO_AUTH_TOKEN=xxxxxxxx
TWILIO_PHONE_NUMBER=+971500000000
TWILIO_APP_BASE_URL=https://leadproject-59dl.onrender.com
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=llama3.2:3b
```

The AI voice mode uses Twilio speech gathering and a local Ollama model one turn at a time. Example request:

```json
{
	"phone": "+971500000000",
	"leadName": "Aisha Rahman",
	"leadId": 42,
	"type": "aiagent"
}
```

Use `type: "iv"` or omit the property to keep the existing predefined-question flow.

For local Ollama chat testing, call `POST /api/v1/ai/chat` with Basic Auth:

```json
{
	"message": "What information should I collect from a Dubai property buyer?",
	"conversation": ""
}
```

The endpoint returns `{ "reply": "..." }` and does not start a Twilio call.

### Docker Compose AI setup

```bash
docker compose up -d --build
docker compose exec ollama ollama pull llama3.2:3b
```

The model is stored in the `ollama_data` volume. The application reaches Ollama at `http://ollama:11434` inside the Compose network.

### Plivo

```bash
PLIVO_AUTH_ID=xxxxxxxx
PLIVO_AUTH_TOKEN=xxxxxxxx
PLIVO_PHONE_NUMBER=+971500000000
PLIVO_APP_BASE_URL=http://localhost:8080
```

## Run locally

```bash
mvn spring-boot:run
```

## Default users

- admin / admin123
- sales / sales123
- compliance / compliance123

## Docker

```bash
mvn clean package -DskipTests
docker-compose up --build
```
