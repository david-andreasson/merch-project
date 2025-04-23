## Registrera användare med användarnamn och lösenord

1. **Dra igång API.**
2. **Öppna Postman.**
3. **POST** till `http://localhost:8080/auth/register`
    - **Auth**: Ingen
    - **Header**: Content-Type: application/json
    - **Body**:
   ```json
   {
     "username": "test_user123",
     "password": "secret123",
     "authType": "PASSWORD"
   }
   ```
4. Klicka på **SEND** – du ska få **200 OK** som svar.

---

## Logga in med användarnamn och lösenord

1. **POST** till `http://localhost:8080/auth/login`
    - **Auth**: Ingen
    - **Header**: Content-Type: application/json
    - **Body**:
   ```json
   {
     "username": "test_user123",
     "password": "secret123",
     "authType": "PASSWORD"
   }
   ```
2. Klicka på **SEND** – du ska få **200 OK** + ett svar som t.ex.:
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9…Z0S4IXi4",
     "apiKey": null
   }
   ```
3. **Spara** `token` och använd den för att komma åt skyddade endpoints.


---

## Registrera användare med API-nyckel

1. **POST** till `http://localhost:8080/auth/register`
    - **Auth**: Ingen
    - **Header**: Content-Type: application/json
    - **Body**:
   ```json
   {
     "username": "test_user_api",
     "password": "secret123",
     "authType": "API_KEY"
   }
   ```
2. Klicka på **SEND** – du ska få **200 OK** + ett svar som t.ex.:
   ```json
   {
     "token": null,
     "apiKey": "CC3Izc1wXyhfRFV5vHWMFMXQ4YDR1xwtvk07s7YzZqQ"
   }
   ```
3. **Spara** `apiKey` för nästa steg.

---

## Logga in med användarnamn och API-nyckel

1. **POST** till `http://localhost:8080/auth/login`
    - **Auth**: Ingen
    - **Header**: Content-Type: application/json
    - **Body**:
   ```json
   {
     "username": "test_user_api",
     "apiKey": "DinRiktigaApiNyckel",
     "authType": "API_KEY"
   }
   ```
2. Klicka på **SEND** – du ska få **200 OK** + ett svar som t.ex.:
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9…Z0S4IXi4",
     "apiKey": null
   }
   ```

---

## Komma åt skyddad endpoint

1. **GET** till `http://localhost:8080/test-auth` (eller vår produkt-endpoint)
    - **Auth**: Ingen
    - **Header**:
      ```
      Authorization: Bearer <DinToken>
      ```
    - **Body**: Ingen
2. Klicka på **SEND** – du ska få **200 OK** +:
   ```
   ✅ Authenticated as: test_user123
   ```