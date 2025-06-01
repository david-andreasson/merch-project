# Reviews API

A Spring Boot application for managing products and reviews, supporting both JWT-based authentication and API key-based authentication. The application includes features such as AI-generated reviews when a product has fewer than a minimum number of reviews, rate-limited external API calls, and Dockerized deployment.

## Table of Contents

* [Features](#features)
* [Prerequisites](#prerequisites)
* [Installation](#installation)
* [Configuration](#configuration)
* [Running the Application](#running-the-application)
* [API Endpoints](#api-endpoints)

    * [Authentication](#authentication)
    * [User](#user)
    * [Products and Reviews](#products-and-reviews)
* [Error Handling](#error-handling)
* [Testing](#testing)
* [Docker and Deployment](#docker-and-deployment)
* [Contributing](#contributing)
* [License](#license)

## Features

* **Authentication Flows**

    * JWT-based (username/password) authentication
    * API key-based authentication
* **Product Management**

    * Create, list, and delete products scoped per user (multi-tenant via `userId+productId`)
* **Review Management**

    * Create, list, delete reviews for products
    * Fetch recent reviews for the last two months
    * Generate AI reviews when fewer than a configurable minimum exist
    * Compute review statistics (average rating, total reviews, last review date)
* **Security**

    * Spring Security configuration with stateless sessions (JWT filter)
    * API key hashing and expiration (6 months)
    * CORS configuration for local front-end testing
* **API Documentation**

    * Swagger/OpenAPI integration
* **Configuration and Deployment**

    * Configuration via `application.properties` or environment variables
    * Ready for Docker build and CI/CD pipelines

## Prerequisites

* Java 17 or higher
* Maven 3.6+
* Docker (optional, for containerization)
* Git
* AI API key (if using AI review generation)
* OpenWeatherMap API key (for weather-based AI prompts)

## Installation

1. **Clone the repository**

   ```bash
   git clone https://github.com/your-org/reviews-api.git
   cd reviews-api
   ```

2. **Build with Maven**

   ```bash
   mvn clean install
   ```

3. **Run the application**

   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080` by default.

## Configuration

Configuration properties can be set in `src/main/resources/application.properties` or via environment variables:

```properties
# JWT
JWT_SECRET=your_jwt_secret_base64
JWT_EXPIRATION=3600000

# API Key
MASTER_KEY=your_master_key_for_encryption

# AI Review Service
ONEMINAI_API_KEY=<your_openai_api_key>
ONEMINAI_API_URL=https://api.one-min.ai/v1/generate

# Weather Service
OPENWEATHER_API_KEY=<your_openweathermap_key>
OPENWEATHER_API_URL=https://api.openweathermap.org/data/2.5/weather?q={city}&appid={key}&units=metric

# CORS (only for local testing)
front-end.url=http://localhost:8080
```

### Environment Variables (example)

```bash
export JWT_SECRET=...  
export JWT_EXPIRATION=3600000  
export MASTER_KEY=...  
export ONEMINAI_API_KEY=...  
export ONEMINAI_API_URL=https://api.one-min.ai/v1/generate  
export OPENWEATHER_API_KEY=...  
export OPENWEATHER_API_URL=https://api.openweathermap.org/data/2.5/weather?q={city}&appid={key}&units=metric  
```

## Running the Application

1. **Database Setup**
   By default, the application uses an in-memory H2 database. You can switch to a persistent database by updating `spring.datasource` properties.

2. **Start the server**

   ```bash
   mvn spring-boot:run
   ```

3. **Access Swagger UI**
   Navigate to `http://localhost:8080/swagger-ui/index.html` to explore the API.

## API Endpoints

### Authentication

* **POST /auth/register**
  Register a new user either via API key or username/password.
  **Request Body (RegisterRequest)**:

  ```json
  {
    "username": "user1",
    "password": "pass123",
    "authType": "PASSWORD"  // or "API_KEY"
  }
  ```

    * If `authType = "API_KEY"`: returns `{"apiKey": "<your_new_key>"}`
    * If `authType = "PASSWORD"`: returns `{"token": "<jwt_token>"}`

* **POST /auth/login**
  Authenticate an existing user.
  **Request Body (AuthenticationRequest)** for password flow:

  ```json
  {
    "username": "user1",
    "password": "pass123",
    "authType": "PASSWORD"
  }
  ```

  For API key flow:

  ```json
  {
    "apiKey": "<existing_api_key>",
    "authType": "API_KEY"
  }
  ```

    * Returns: `{"token": "<jwt_token>"}`

### User

* **POST /user/api-key**
  Update the authenticated user’s raw API key.

    * **Request Body**: Raw plaintext API key (string)
    * **Authentication**: Must include valid JWT in `Authorization: Bearer <token>`
    * Returns: `200 OK` with message "API key updated"

### Products and Reviews

> **Note**: All `/product/**` endpoints require a valid JWT in the `Authorization` header.

1. **GET /product/all**
   List all products belonging to the authenticated user.
   **Response**: Array of `ProductRespons`:

   ```json
   [
     {
       "productId": "abc123",
       "productName": "Example T-Shirt",
       "category": "Apparel",
       "tags": "tag1, tag2"
     }
     // ... more
   ]
   ```

2. **GET /product/{productId}**
   Retrieve all reviews for a given product (last 2 months + AI fallback).

    * `productId` is the client-visible ID (server will prefix with `userId`).
      **Response (`ReviewsRespons`)**:

   ```json
   {
     "productId": "abc123",
     "stats": {
       "productId": "1abc123",
       "productName": "Example T-Shirt",
       "currentAverage": 4.2,
       "totalReviews": 7,
       "lastReviewDate": "2023-08-15"
     },
     "reviews": [
       { "name": "Alice", "rating": 5, "text": "Great!", "date": "2023-08-14" },
       { "name": "AI_Bot", "rating": 4, "text": "Automatically generated review.", "date": "2023-08-15" }
       // ... up to MAX_REVIEWS
     ]
   }
   ```

3. **POST /product**
   Create a product or add a review based on the `mode` in `ProductRequest`.
   **Request Body (ProductRequest)**:

   ```json
   {
     "mode": "withDetails",           // "productOnly", "withUrl", "withDetails", or "customReview"
     "productId": "abc123",
     // For "productOnly": no productName/category/tags required
     // For "withDetails": include productName, category, tags
     "productName": "Example T-Shirt",
     "category": "Apparel",
     "tags": ["tag1", "tag2"],
     // For "withUrl": include productInfoUrl (external service endpoint)
     "productInfoUrl": "https://external.service/api/info/abc123",
     // For "customReview": include nested ReviewRequest
     "review": {
       "name": "Bob",
       "rating": 5,
       "text": "Excellent quality!"
     }
   }
   ```

    * **Modes**:

        * `productOnly`: Creates a product with default hardcoded values.
        * `withUrl`: Fetches product details from `productInfoUrl` using user’s API key, then saves.
        * `withDetails`: Saves product with provided details.
        * `customReview`: Adds a custom review to an existing product; throws `ProductNotFoundException` if not exists.
    * **Responses**:

        * `201 Created` with `ProductRespons` or a success message for review.
        * `400 Bad Request` / `404 Not Found` / `409 Conflict` depending on errors.

4. **DELETE /product/{productId}**
   Delete a product and all associated reviews.

    * Authentication: JWT required.
    * Response: `200 OK` with message "Product and related reviews deleted successfully" or `404 Not Found` if product does not exist.

## Error Handling

This application uses a global exception handler (`GlobalExceptionHandler`) to format errors as JSON. Common error responses include:

* **400 Bad Request**: Invalid parameters or request payload (e.g., missing fields, invalid mode, invalid API key).
* **401 Unauthorized**: Missing or invalid JWT in protected endpoints.
* **403 Forbidden**: Invalid or expired API key flow.
* **404 Not Found**: Resource (product or review) not found.
* **409 Conflict**: Attempt to create a product with an existing `productId`.
* **500 Internal Server Error**: Unhandled exceptions and AI service errors.

Error response format (`ErrorResponse`):

```json
{
  "timestamp": "2023-08-15T12:34:56.789",
  "status": 404,
  "error": "Not Found",
  "message": "Product does not exist",
  "path": "/product/abc123"
}
```

## Testing

* **Unit Tests**: Located under `src/test/java`. Uses JUnit 5 and Mockito for service and repository tests.

* **Integration Tests**: Also under `src/test/java` for controller-level and filter-level tests.

* **Coverage**: Aim for 70%+ code coverage. Run tests with:

  ```bash
  mvn test
  ```

* **AI Review Mocking**: Set `USE_MOCK=true` in `AiReviewService` for deterministic tests without external API calls.

## Docker and Deployment

### Dockerfile

A Dockerfile is provided to build a container image for this service. Example:

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/reviews-api.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run with Docker:

```bash
# Build the JAR
mvn clean package -DskipTests

# Build Docker image
docker build -t reviews-api .

# Run Docker container
docker run -d \
  -e JWT_SECRET=... \
  -e MASTER_KEY=... \
  -e ONEMINAI_API_KEY=... \
  -e OPENWEATHER_API_KEY=... \
  -p 8080:8080 \
  reviews-api
```

### CI/CD (GitHub Actions)

This project includes a GitHub Actions workflow. It runs automatically on every push to the `main` branch and performs the following steps:

1. **Check out source code**: Uses the `actions/checkout@v3` action to fetch the latest code.
2. **Set up Java 21**: Uses `actions/setup-java@v4` to install Temurin JDK 21 for building.
3. **Cache Maven dependencies**: Uses `actions/cache@v3` to cache the local Maven repository (`~/.m2/repository`) based on the project’s `pom.xml` hash, speeding up repeated builds.
4. **Run tests with Maven**: Executes `mvn verify`, which runs unit and integration tests, including JaCoCo coverage reporting.
5. **Upload JaCoCo report**: Uses `actions/upload-artifact@v4` to store the code coverage report (`target/site/jacoco/`) as a build artifact.
6. **Set up Docker Buildx**: Uses `docker/setup-buildx-action@v3` to enable advanced Docker build features.
7. **Log in to Docker Hub**: Uses `docker/login-action@v3`, with `DOCKER_USERNAME` and `DOCKER_PASSWORD` provided as repository secrets, to authenticate.
8. **Build and push Docker image**: Uses `docker/build-push-action@v5`:

    * Builds the Docker image from the project root.
    * Targets the `linux/amd64` platform.
    * Pushes to Docker Hub under the tag `davidandreasson/merch-project:latest`.
    * Uses GitHub Actions cache (`type=gha`) to speed up rebuilds.


## License

This project is licensed under the MIT License.
