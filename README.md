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
3. Start Twilio call: `POST /api/v1/leads/call`
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
TWILIO_APP_BASE_URL=http://localhost:8080
```

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
