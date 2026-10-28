# AI Knowledge & Decision Assistant

An AI-powered knowledge assistant that allows organizations to upload PDF documents, retrieve relevant information, and answer visitor questions using document-grounded responses and structured decision models.

## 1. Features

- Organization registration and login.
- Password hashing using BCrypt.
- JWT-based authentication for protected administrative APIs.
- Organization-specific assistant tokens.
- Public assistant chat without requiring visitor login.
- PDF upload, listing, and deletion.
- PDF text extraction and page-aware document chunking.
- Keyword-based document retrieval.
- AI-generated answers using retrieved document evidence.
- Source citations containing document names and page numbers.
- Structured decision types: Boolean, Choice, Score, Multi-condition, and General.
- Organization-level document isolation.
- PDF validation for file size, page count, and document count.
- Dashboard for document management.
- Embeddable assistant using an iframe.
- Meaningful responses when relevant information is unavailable.

## 2. Technology Stack

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL
- JWT authentication
- BCrypt password hashing
- Apache PDFBox
- Google Gemini API
- HTML, CSS, and JavaScript
- Maven
- Postman
- Eclipse IDE

## 3. Architecture

The application follows a layered backend architecture.

- **Controller layer:** Handles HTTP requests and responses.
- **Service layer:** Implements business logic, document retrieval, PDF processing, and AI integration.
- **Repository layer:** Communicates with MySQL through Spring Data JPA.
- **Document processing:** Extracts PDF text and stores searchable chunks with page information.
- **Retrieval layer:** Searches the authenticated organization's or assistant's knowledge base for relevant chunks.
- **Decision layer:** Classifies questions and evaluates supported structured decisions.
- **AI layer:** Generates answers grounded in retrieved document evidence.
- **Frontend:** Provides the organization dashboard and public visitor chat.

### Question Processing Flow

Organization Assistant Token + Visitor Question  
→ Token Validation  
→ Organization Identification  
→ Organization-Scoped Document Retrieval  
→ Question Classification  
→ Structured Decision Evaluation  
→ Gemini Answer Generation  
→ Answer + Source References

When no relevant evidence is retrieved, the application returns an insufficient-evidence response instead of calling the AI service.

## 4. Prerequisites

Install the following before running the application:

- Java 17 or a compatible JDK.
- Maven, or use the Maven Wrapper if included in the project.
- MySQL.
- Eclipse IDE or another Java IDE.
- A Google Gemini API key.
- Postman for API testing.

## 5. Database Setup

Create the application database in MySQL:

```sql
CREATE DATABASE ai_knowledge_assistant;
```

Configure the database URL, username, and password using your local application configuration.

Do not commit database passwords or other credentials to GitHub.

## 6. Environment Configuration

Configure the following values in your local environment or Eclipse Run Configurations:

- `JWT_SECRET`: A strong, randomly generated secret used to sign JWTs.
- `GEMINI_API_KEY`: Your Google Gemini API key.

Configure the MySQL connection properties in `application.properties` using environment variables or another secure local configuration method.

Never commit real API keys, passwords, JWT secrets, or public assistant tokens to the repository.

## 7. Run the Application

1. Clone or download the project.
2. Open the project in Eclipse.
3. Configure the database and required environment variables.
4. Ensure MySQL is running.
5. Run the main Spring Boot application class.
6. Open the application at:

   `http://localhost:8090`

If your local configuration uses a different port, use that port instead.

## 8. PDF Knowledge Base

Organizations can upload PDFs to their knowledge bases.

The application enforces these assignment limits:

- Maximum 10 PDF documents per organization.
- Maximum 20 pages per PDF.
- Maximum 10 MB per PDF.

The document-processing pipeline extracts text, divides it into searchable chunks, and retains document and page information for citations.

The current retrieval implementation uses keyword-based search. Embeddings and vector search are listed in the assignment as possible approaches, but they are not claimed as implemented features.

## 9. Assistant APIs

The following endpoints are implemented in the current application.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/organizations/register` | Register an organization |
| POST | `/api/auth/login` | Authenticate an organization |
| GET | `/api/documents` | List the organization's documents |
| POST | `/api/documents/upload` | Upload a PDF |
| DELETE | `/api/documents/{documentId}` | Delete an organization's document |
| POST | `/api/assistant/ask` | Ask a question through a public assistant |

Administrative endpoints require the appropriate authentication. The public assistant endpoint uses an organization-specific assistant token.

### Example Assistant Request

`POST /api/assistant/ask`

```json
{
  "assistantToken": "YOUR_ORGANIZATION_PUBLIC_TOKEN",
  "question": "Can an organization upload more than 10 PDF documents?"
}
```

Replace the placeholder with a valid assistant token from your local test organization.

The response includes:

- `answer`: The generated answer.
- `sources`: Relevant document and page references.
- `status`: Whether the question was answered or relevant evidence was unavailable.
- `decision`: The detected decision type, result, and explanation.

## 10. Decision Models

The decision layer supports the following classifications:

- **BOOLEAN:** Evaluates supported yes/no questions.
- **CHOICE:** Identifies available options and selects an option when the implemented rules support a reliable selection.
- **MULTI_CONDITION:** Checks whether the identified conditions are supported by the retrieved evidence.
- **SCORE:** Requires explicit scoring criteria before producing a reliable score.
- **GENERAL:** Handles ordinary information questions.

When the evidence is insufficient or multiple options cannot be distinguished reliably, the system can return `REVIEW_REQUIRED` or `INSUFFICIENT_EVIDENCE`, depending on the situation.

These structured decision models use application logic. They should not be interpreted as a fully implemented semantic reasoning engine.

## 11. Security

- Passwords are hashed using BCrypt.
- JWT authentication protects administrative operations.
- Public assistant tokens identify organizations without exposing passwords.
- Document retrieval is scoped to the identified organization.
- Source documents are checked against organization ownership before their contents are returned.
- Visitors can use the public assistant without logging in.
- Unsupported questions receive an insufficient-evidence response when no relevant knowledge is retrieved.
- API keys and passwords must be configured outside version control.

For production deployment, use HTTPS, restrictive Content Security Policy settings, secure secret management, rate limiting, and additional monitoring.

## 12. Testing

The application has been manually tested using Postman.

Verified scenarios include:

- Organization registration and login.
- PDF upload and document management.
- File-size, page-count, and document-count validation.
- Boolean decisions for PDF upload limits.
- Choice-question classification.
- Multi-condition decisions.
- Unsupported questions returning `INSUFFICIENT_EVIDENCE`.
- Document and page citations.
- Duplicate citation removal.
- Multi-tenant isolation between organizations.

Before submission, also maintain a repeatable set of at least 10 questions, including two multi-condition questions, one question requiring multiple structured decisions, and one unsupported question.

## 13. Known Limitations

- Retrieval currently uses keyword-based search rather than a vector database.
- Multi-condition evaluation relies on application rules and keyword matching; it is not a general-purpose semantic verification engine.
- Score decisions require explicit scoring criteria.
- AI responses and decision outcomes should be validated against the supplied sources.
- Production deployment would require further security, observability, scalability, and automated testing work.

## 14. Future Improvements

- Add semantic embeddings and vector search.
- Improve question decomposition and multi-decision orchestration.
- Add automated unit, integration, and tenant-isolation tests.
- Add usage statistics and audit logging.
- Improve production deployment, rate limiting, and monitoring.
- Add stronger browser security policies and secret-management integration.

## 15. Submission Checklist

- [ ] Complete source code committed to GitHub.
- [ ] Example configuration provided without secrets.
- [ ] Database setup instructions included.
- [ ] At least two organizations configured for testing.
- [ ] Two to three PDFs available for each organization.
- [ ] At least 10 test questions prepared.
- [ ] At least two multi-condition questions prepared.
- [ ] Multi-decision scenario tested.
- [ ] Unsupported question tested.
- [ ] Tenant isolation demonstrated.
- [ ] Application setup and API instructions verified.
- [ ] Demo video recorded, approximately 8–15 minutes.
- [ ] No passwords, API keys, JWT secrets, or other credentials committed.