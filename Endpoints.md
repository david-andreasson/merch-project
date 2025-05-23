
# API Endpoints Dokumentation

**Base URL:** `https://aireviews.drillbi.se`

**Swagger UI:** https://aireviews.drillbi.se/swagger-ui/index.html`



Denna API erbjuder funktionalitet för hantering av produkter, recensioner och autentisering. Här är en lista över tillgängliga endpoints.

---

## 1. Autentisering

### 1.1 Registera en ny användare
**Endpoint:** `POST /auth/register`

Skapar en ny användare och returnerar en JWT-token eller API-nyckel beroende på `authType`.

**Request Body:**
```json
{
  "username": "exampleUser",
  "password": "examplePassword",
  "authType": "password" // eller "API_KEY"
}
````

**Response:**

```json
{
  "token": "jwt_token_here"
}
```

eller, om `authType` är `"API_KEY"`:

```json
{
  "apiKey": "generated_api_key_here"
}
```

### 1.2 Logga in

**Endpoint:** `POST /auth/login`

Loggar in en användare och returnerar en JWT-token.

**Request Body (lösenord):**

```json
{
  "username": "exampleUser",
  "password": "examplePassword",
  "authType": "password"
}
```

**Request Body (API-nyckel):**

```json
{
  "apiKey": "valid_api_key_here",
  "authType": "API_KEY"
}
```

**Response:**

```json
{
  "token": "jwt_token_here"
}
```

### 1.3 Testa skyddad endpoint

**Endpoint:** `GET /test-auth`

Kontrollerar giltig JWT-token eller API-nyckel.

**Header:**

```http
Authorization: Bearer <token>
```

**Response:**

* `200 OK` – OK om token är giltig.


### 1.4 Registrera API-KEY till ert API

**Endpoint:** `POST /user/api-key`

Kopplar API-nyckel till användaren för användning av /product with mode: "withUrl"

**Header:**

```http
Authorization: Bearer <token>
```

**Request Body:**

```json
api-nyckel
```

**Response:**

* `200 OK` – OK om API-nyckeln lyckades läggas till.

---

## 2. Produkter

Alla produkt-endpoints kräver header:

```http
Authorization: Bearer <token>
Content-Type: application/json
```

### 2.1 Skapa produkt (Mode: productOnly)

**Endpoint:** `POST /product`

**Request Body:**

```json
{
  "mode": "productOnly",
  "productId": "T12345"
}
```

**Response:**

```json
{
  "productId": "T12345",
  "productName": "Whitesnake T-shirt",
  "category": "T-shirt",
  "tags": "hårdrock, 80-tal, svart, bomull"
}
```

---

### 2.2 Skapa produkt via URL (Mode: withUrl)

**Endpoint:** `POST /product`

**Request Body:**

```json
{
  "mode": "withUrl",
  "productId": "T12345",
  "productInfoUrl": "https://kundens-api.se/products/T12345"
}
```

**Response:**

```json
{
  "productId": "T12345",
  "productName": "Whitesnake T-shirt",
  "category": "T-shirts",
  "tags": "hårdrock, 80-tal, svart, bomull"
}
```

---

### 2.3 Skapa produkt med detaljer (Mode: withDetails)

**Endpoint:** `POST /product`

**Request Body:**

```json
{
  "mode": "withDetails",
  "productId": "T12345",
  "productName": "Whitesnake T-shirt",
  "category": "T-shirts",
  "tags": ["hårdrock", "80-tal", "svart", "bomull"]
}
```

**Response:**

```json
{
  "productId": "T12345",
  "productName": "Whitesnake T-shirt",
  "category": "T-shirts",
  "tags": "hårdrock, 80-tal, svart, bomull"
}
```

---

### 2.4 Lägg till egen recension (Mode: customReview)

**Endpoint:** `POST /product`

**Request Body:**

```json
{
  "mode": "customReview",
  "productId": "T12345",
  "review": {
    "name": "RockPelle",
    "text": "Kvaliteten var inte som förväntat",
    "rating": 2
  }
}
```

**Response:**

```json
{
  "message": "Review added successfully"
}
```

---

### 2.5 Hämta produkt och recensioner

**Endpoint:** `GET /product`

Tar emot `productId` som query-parameter och returnerar produkt samt alla recensioner.

**URL:**

```
GET /product/T12345
```

**Response:**

```json
{
  "productId": "T12345",
  "stats": {
    "productId": "1T12345",
    "productName": "Whitesnake T-shirt",
    "currentAverage": 4.5,
    "totalReviews": 2,
    "lastReviewDate": "2025-04-26"
  },
  "reviews": [
    {
      "date": "2025-04-01",
      "name": "John Doe",
      "rating": 5,
      "text": "Amazing product, works perfectly!"
    },
    {
      "date": "2025-04-05",
      "name": "Jane Smith",
      "rating": 4,
      "text": "Good product, but could be cheaper."
    }
  ]
}
```

---

### 2.6 Ta bort en produkt (Mode: delete)

**Endpoint:** `DELETE /product`

Tar bort en produkt baserat på `productId`.

**URL:**

```
DELETE /product/T12345
```

**Response:**

* `200 OK` – Om produkten raderades framgångsrikt.

```json
{
  "message": "Product deleted successfully"
}
```

* `400 Bad Request` – Om `productId` saknas eller ogiltigt.

```json
{
  "error": "Invalid or missing productId"
}
```

* `404 Not Found` – Om produkten inte finns.

```json
{
  "error": "Product not found"
}
```

---

## 3. Exempel med curl

```bash
# 1. Registrera användare
curl -X POST http://161.97.151.105:8081/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"test_user","password":"secret123","authType":"password"}'

# 2. Logga in och spara token
TOKEN=$(curl -s -X POST http://161.97.151.105:8081/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"test_user","password":"secret123","authType":"password"}' \
  | jq -r .token)

# 3. Testa skyddad endpoint
curl -X GET http://161.97.151.105:8081/test-auth \
  -H "Authorization: Bearer $TOKEN"

# 4. Skapa produkt (productOnly)
curl -X POST http://161.97.151.105:8081/product \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"mode":"productOnly","productId":"T12345"}'

# 5. Hämta produkt med recensioner
curl -X GET "http://161.97.151.105:8081/product/T12345" \
  -H "Authorization: Bearer $TOKEN"

# 6. Ta bort produkt
curl -X DELETE "http://161.97.151.105:8081/product/T12345" \
  -H "Authorization: Bearer $TOKEN"
  
# 7. Hämta en lista på alla produkter ni lagt till i databasen,
curl -X GET http://161.97.151.105:8081/product/all \
  -H "Authorization: Bearer $TOKEN"
```

```

