# Documenting a REST API

All error messages follows this format:
{ status : statusCode, "msg": "Explains the problem" }



# Auth
| Method | URL                | Request Body (JSON) | Response (JSON)  | Status          |
|--------|--------------------|----------------------|------------------|-----------------|
| post   | /api/auth/register |         UserRegistrationDTO          | LoginResponseDTO | 200             |
| POST   | /api/auth/login    |            UserLoginDTO          | LoginResponseDTO        | 200 / 404       |


**UserRegistrationDTO**

```json
{
  "name": "String", "email": "String", "password": "String", "passwordCheck": "String"
}
```
**LoginRequestDTO**
```json
{
  "email": "String", "password": "String"
}
```

**LoginResponseDTO**
```json 
{ "token": "String (JWT)" }
```






# User
| Method  | URL             | Request Body (JSON) | Response (JSON) | Status          |
|---------|-----------------|----------------------|------------------|-----------------|
| GET     | /api/users      |                      | [user, user, …] (1) | 200             |
| GET     | /api/users/{id} |                      | user (1)         | 200 / 404       |
| POST    | /api/users      | user(1) without id   |                  | 201 / 400 / 409 |
| PUT     | /api/users/{id} | user(1) without id   | user (1)         | 200 / 400 / 403 / 404 |
| DELETEE | /api/users/{id} |   |       | 204 / 403 / 404 |


## Request Body and Response Formats


# GeminiPrompt

| Method | URL             | Request Body (JSON) | Response (JSON) | Error (e) |
|--------|-----------------|----------------------|------------------|-----------|
| GET    | /api/users      |                      | [user, user, …] (1) |         |
| GET    | /api/users/{id} |                      | user (1)         | (e1)      |
| POST   | /api/users      | user(1) without id   |                  | (e2)      |
| UPDATE | /api/users/{id} | user(1) without id   | user (1)         |           |












## Errors
(e1)

(e2)
(e3)