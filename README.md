# Peblo AI Note Workspace

A robust, secure, full-stack notes application featuring an asynchronous AI engine that automatically generates contextual summaries and intelligent titles using the Google Gemini API.

## Tech Stack & Architecture

### Backend Engine
* **Framework:** Spring Boot 4.0.6 (Java 17 Corretto)
* **Security:** Spring Security with Stateless JSON Web Tokens (JWT)
* **Data Layer:** Spring Data JPA & Hibernate ORM
* **Database:** PostgreSQL (with HikariCP connection pooling)
* **AI Integration:** Google Gemini 2.5 Flash API via synchronous REST Client mapping structured JSON payloads

### Frontend Interface
* **Framework:** React with Vite
* **Styling:** Tailwind CSS & Lucide React Icons
* **API Client:** Axios (configured with secure interceptors for JWT authorization headers)

## Key Features
* **Stateless Authentication:** Secure user signup and login utilizing cryptographically signed JWT tokens.
* **Full CRUD Operations:** Seamless note creation, updates, storage, and retrieval with active Hibernate tracking.
* **Gemini AI Insights:** Dynamic right-hand panel that analyzes note content on-the-fly to deliver structural summaries and title recommendations.
