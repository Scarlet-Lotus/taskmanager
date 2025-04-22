**Backend for TaskManager** is a RESTful API developed using Spring Boot, which provides functionality for managing tasks, users, and comments.  
The project includes authentication, authorization, CRUD operations for tasks and comments, as well as additional features such as task filtering and sorting.

---

### 1. Key Technologies
- **Spring Boot**: A framework for rapid backend application development.
- **Spring Data JPA**: For working with the database through repositories.
- **Spring Security**: For user authentication and authorization.
- **JWT (JSON Web Token)**: For managing authentication tokens.
- **H2 Database**: An embedded database for testing and development.
- **Hibernate**: ORM for working with the database.
- **JUnit 5 + Mockito**: For writing unit tests.

---

### 2. API Endpoints

#### 2.1. Authentication
**POST /api/auth/register**  
Register a new user.  
**Request Body**:
```json
{
  "login": "user",
  "email": "user@example.com",
  "password": "password"
}
```
**Response**:
```json
{
  "id": 1,
  "login": "user",
  "email": "user@example.com"
}
```

**POST /api/auth/login**  
Authenticate a user.  
**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "password"
}
```
**Response**:
```json
{
  "token": "jwt-token"
}
```

---

#### 2.2. Users
**GET /api/users/profile**  
Get the user's profile.  
**Headers**: `Authorization: Bearer <token>`  
**Response**:
```json
{
  "id": 1,
  "login": "user",
  "email": "user@example.com",
  "tasks": []
}
```

**PUT /api/users/update**  
Update user data.  
**Headers**: `Authorization: Bearer <token>`  
**Request Body**:
```json
{
  "login": "new_login",
  "password": "new_password"
}
```
**Response**:
```json
{
  "id": 1,
  "login": "new_login",
  "email": "user@example.com"
}
```

---

#### 2.3. Tasks
**GET /api/tasks**  
Get a list of all tasks for the user.  
**Headers**: `Authorization: Bearer <token>`  
**Response**:
```json
[
  {
    "id": 1,
    "title": "Task 1",
    "status": "TO_DO",
    "priority": "HIGH"
  }
]
```

**POST /api/tasks**  
Create a new task.  
**Headers**: `Authorization: Bearer <token>`  
**Request Body**:
```json
{
  "title": "New Task",
  "description": "Description of the task",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM"
}
```
**Response**:
```json
{
  "id": 2,
  "title": "New Task",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM"
}
```

**PUT /api/tasks/{id}**  
Update a task.  
**Headers**: `Authorization: Bearer <token>`  
**Request Body**:
```json
{
  "title": "Updated Task",
  "status": "DONE"
}
```
**Response**:
```json
{
  "id": 1,
  "title": "Updated Task",
  "status": "DONE",
  "priority": "HIGH"
}
```

**DELETE /api/tasks/{id}**  
Delete a task.  
**Headers**: `Authorization: Bearer <token>`

---

#### 2.4. Comments
**POST /api/tasks/{taskId}/comments**  
Add a comment to a task.  
**Headers**: `Authorization: Bearer <token>`  
**Request Body**:
```json
{
  "text": "This is a comment"
}
```
**Response**:
```json
{
  "id": 1,
  "text": "This is a comment",
  "taskId": 1
}
```

---

### 3. Additional Features

#### 3.1. Task Filtering
**GET /api/tasks/filter/status?status=TO_DO**  
Filter tasks by status.  

**GET /api/tasks/filter/priority?priority=HIGH**  
Filter tasks by priority.  

---

#### 3.2. Pagination
**GET /api/tasks?page=0&size=10**  
Retrieve tasks with pagination.

---

### Authors
- **Almaz**  
GitHub: [https://github.com/Scarlet-Lotus]  
Email: mamlukbeibarys@gmail.com
