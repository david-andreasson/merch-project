# API Endpoints Dokumentation

Denna API erbjuder funktionalitet för hantering av produkter, recensioner och autentisering. Här är en lista över tillgängliga endpoints.

---

## Autentisering

### 1. Registera en ny användare
**Endpoint:** `POST /auth/register`

Skapar en ny användare och returnerar en JWT-token eller API-nyckel beroende på `authType`.

**Request Body:**
```json
{
  "username": "exampleUser",
  "password": "examplePassword",
  "authType": "password" // eller "API_KEY"
}
```

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

### 2. Logga in
**Endpoint:** `POST /auth/login`

Loggar in en användare med antingen användarnamn/lösenord eller en API-nyckel.

**Request Body (med användarnamn och lösenord):**
```json
{
  "username": "exampleUser",
  "password": "examplePassword",
  "authType": "password"
}
```

**Request Body (med API-nyckel):**
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

---

# 3. Produkter

## POST /product (Mode: productOnly)

**Beskrivning:**  
Lägger till en produkt med endast productId utan att hämta information från någon extern URL.

**Request Body:**
```json
{
  "mode": "productOnly",
  "productId": "T12345"
}
```
Random information kommer att genereras för productName, category och tags.

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

## POST /product (Mode: withUrl)

**Beskrivning:**  
Lägger till en produkt genom att hämta produktinformation från en extern URL.

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

## POST /product (Mode: withDetails)

**Beskrivning:**  
Lägger till en produkt med detaljerad information som produktnamn, kategori och tags.

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

## POST /product (Mode: customReview)

**Beskrivning:**  
Lägger till en recension för en produkt.

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

# GET /products

**Beskrivning:**  
Detta endpoint tar emot ett `productId` och returnerar produkten samt alla dess recensioner.

## Request

**URL:**  
`GET /products`

**Query Parameter:**
- `productId` (string, obligatoriskt) – ID:t för produkten som du vill hämta.

**Exempel:**
```json
{
  "productId": "T12345"
}
```

## Response

**Statuskod:**
- `200 OK` – Om produkten och dess recensioner hämtas framgångsrikt.
- `400 Bad Request` – Om `productId` saknas eller om produkten inte kan hittas.

**Response Body:**
```json
{
  "productId": "T12345",
  "productName": "Whitesnake T-shirt",
  "category": "T-shirts",
  "tags": "hårdrock, 80-tal, svart, bomull",
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

### 6. Ta bort en produkt (Ej implementerad)
**Endpoint:** `DELETE /products`

Tar bort en produkt från systemet baserat på dess `productId`.

---