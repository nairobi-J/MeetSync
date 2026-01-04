# MeetSync API Documentation

## 📋 Table of Contents
- [Availability APIs](#availability-apis)
- [Booking APIs](#booking-apis)
- [Common Data Models](#common-data-models)
- [Error Responses](#error-responses)

---

## 🎯 Availability APIs

### 1. Setup Availability (Host)

Configure host's weekly schedule, meeting preferences, and date exceptions.

**Endpoint:** `POST /api/availability/setup`

**Authentication:** Required (User-Id header)

**Headers:**
```json
{
  "User-Id": "123e4567-e89b-12d3-a456-426614174000",
  "Content-Type": "application/json"
}
```

**Request Body:**
```json
{
  "meetingDurationMinutes": 30,
  "minNoticeHours": 4,
  "futureDaysAllowed": 60,
  "bufferTimeMinutes": 15,
  "timezone": "Asia/Dhaka",
  "weeklyAvailability": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "09:00:00",
      "endTime": "17:00:00"
    },
    {
      "dayOfWeek": "TUESDAY",
      "startTime": "09:00:00",
      "endTime": "17:00:00"
    },
    {
      "dayOfWeek": "WEDNESDAY",
      "startTime": "09:00:00",
      "endTime": "17:00:00"
    },
    {
      "dayOfWeek": "THURSDAY",
      "startTime": "10:00:00",
      "endTime": "16:00:00"
    },
    {
      "dayOfWeek": "FRIDAY",
      "startTime": "09:00:00",
      "endTime": "14:00:00"
    }
  ],
  "dateOverrides": [
    {
      "date": "2026-01-15",
      "startTime": null,
      "endTime": null,
      "unavailable": true
    },
    {
      "date": "2026-01-20",
      "startTime": "10:00:00",
      "endTime": "14:00:00",
      "unavailable": false
    }
  ]
}
```

**Request Field Descriptions:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `meetingDurationMinutes` | Integer | Yes | Duration of each meeting slot in minutes (e.g., 30) |
| `minNoticeHours` | Integer | Yes | Minimum hours required before a meeting can be booked (e.g., 4) |
| `futureDaysAllowed` | Integer | Yes | How many days in advance bookings are allowed (e.g., 60) |
| `bufferTimeMinutes` | Integer | No | Buffer time added before each meeting slot (default: 0) |
| `timezone` | String | Yes | Host's timezone (e.g., "Asia/Dhaka", "America/New_York") |
| `weeklyAvailability` | Array | Yes | Regular weekly schedule |
| `weeklyAvailability[].dayOfWeek` | Enum | Yes | Day of week: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY |
| `weeklyAvailability[].startTime` | Time | Yes | Start time in HH:mm:ss format (e.g., "09:00:00") |
| `weeklyAvailability[].endTime` | Time | Yes | End time in HH:mm:ss format (e.g., "17:00:00") |
| `dateOverrides` | Array | No | Specific date exceptions to weekly schedule |
| `dateOverrides[].date` | Date | Yes | Date in YYYY-MM-DD format (e.g., "2026-01-15") |
| `dateOverrides[].startTime` | Time | No | Override start time (null if unavailable is true) |
| `dateOverrides[].endTime` | Time | No | Override end time (null if unavailable is true) |
| `dateOverrides[].unavailable` | Boolean | Yes | true = day off, false = custom hours |

**Success Response:**

**Status Code:** `200 OK`

```json
"Availability setup successfully"
```

**Error Responses:**

**Status Code:** `404 Not Found`
```json
{
  "error": "User not found"
}
```

---

### 2. Get Booking Link (Host)

Retrieve the unique booking link for the host to share with invitees.

**Endpoint:** `GET /api/availability/link`

**Authentication:** Required (User-Id header)

**Headers:**
```json
{
  "User-Id": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Success Response:**

**Status Code:** `200 OK`

```json
"https://meetsync.app/u/john.doe"
```

**Note:** The link uses the email prefix (part before @) from the host's email address.

**Example:** If host email is `john.doe@company.com`, the link will be `/u/john.doe`

**Error Responses:**

**Status Code:** `404 Not Found`
```json
{
  "error": "User not found"
}
```

**Status Code:** `400 Bad Request`
```json
{
  "error": "User email is invalid"
}
```

---

### 3. View Available Slots (Public - Invitee)

Get all available time slots for booking with a host. This is a public endpoint accessible to anyone with the booking link.

**Endpoint:** `GET /api/availability/u/{emailPrefix}`

**Authentication:** Not Required (Public endpoint)

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `emailPrefix` | String | Part of host's email before @ symbol (e.g., "john.doe") |

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `timezone` | String | No | Invitee's timezone to convert slots (e.g., "America/New_York"). If not provided, uses host's timezone |

**Example Request:**
```
GET /api/availability/u/john.doe?timezone=America/New_York
```

**Success Response:**

**Status Code:** `200 OK`

```json
[
  {
    "startTime": "2026-01-05T09:00:00",
    "endTime": "2026-01-05T09:30:00",
    "timezone": "America/New_York"
  },
  {
    "startTime": "2026-01-05T09:45:00",
    "endTime": "2026-01-05T10:15:00",
    "timezone": "America/New_York"
  },
  {
    "startTime": "2026-01-05T10:30:00",
    "endTime": "2026-01-05T11:00:00",
    "timezone": "America/New_York"
  },
  {
    "startTime": "2026-01-05T14:00:00",
    "endTime": "2026-01-05T14:30:00",
    "timezone": "America/New_York"
  },
  {
    "startTime": "2026-01-06T09:00:00",
    "endTime": "2026-01-06T09:30:00",
    "timezone": "America/New_York"
  }
]
```

**Response Field Descriptions:**

| Field | Type | Description |
|-------|------|-------------|
| `startTime` | DateTime | Meeting start time in ISO format |
| `endTime` | DateTime | Meeting end time in ISO format |
| `timezone` | String | Timezone in which times are displayed |

**Notes:**
- Times are automatically converted to invitee's timezone if provided
- Slots exclude already booked times
- Only shows slots with minimum required notice (e.g., 4 hours ahead)
- Only shows slots within the allowed booking window (e.g., 60 days)
- Buffer time is automatically applied between slots

**Error Responses:**

**Status Code:** `404 Not Found`
```json
{
  "error": "User not found"
}
```

**Status Code:** `400 Bad Request`
```json
{
  "error": "User has not set up availability"
}
```

---

## 📅 Booking APIs

### 4. Create Booking Request (Public - Invitee)

Book a time slot with a host. This is a public endpoint accessible to anyone.

**Endpoint:** `POST /api/bookings/u/{emailPrefix}`

**Authentication:** Not Required (Public endpoint)

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `emailPrefix` | String | Part of host's email before @ symbol (e.g., "john.doe") |

**Request Body:**
```json
{
  "inviteeName": "Jane Smith",
  "inviteeEmail": "jane.smith@example.com",
  "bookingDate": "2026-01-10",
  "bookingTime": "10:00:00"
}
```

**Request Field Descriptions:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `inviteeName` | String | Yes | Name of the person booking the meeting |
| `inviteeEmail` | String | Yes | Email address of the person booking |
| `bookingDate` | Date | Yes | Date for the meeting in YYYY-MM-DD format |
| `bookingTime` | Time | Yes | Time for the meeting in HH:mm:ss format |

**Success Response:**

**Status Code:** `200 OK`

```json
{
  "id": "987e6543-e21b-12d3-a456-426614174999",
  "hostName": "John Doe",
  "hostEmail": "john.doe@company.com",
  "inviteeName": "Jane Smith",
  "inviteeEmail": "jane.smith@example.com",
  "startTime": "2026-01-10T10:00:00",
  "endTime": "2026-01-10T10:30:00",
  "status": "PENDING",
  "createdAt": "2026-01-04T15:30:00"
}
```

**Response Field Descriptions:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Unique booking identifier |
| `hostName` | String | Name of the host |
| `hostEmail` | String | Email of the host |
| `inviteeName` | String | Name of the invitee |
| `inviteeEmail` | String | Email of the invitee |
| `startTime` | DateTime | Meeting start time |
| `endTime` | DateTime | Meeting end time |
| `status` | Enum | Booking status: PENDING, CONFIRMED, or CANCELLED |
| `createdAt` | DateTime | When the booking was created |

**Error Responses:**

**Status Code:** `404 Not Found`
```json
{
  "error": "User not found"
}
```

**Status Code:** `400 Bad Request`
```json
{
  "error": "User has not set up availability"
}
```

```json
{
  "error": "Booking date and time are required"
}
```

```json
{
  "error": "This time slot is no longer available"
}
```

```json
{
  "error": "Host is not available on this date"
}
```

```json
{
  "error": "Host is not available on this day of the week"
}
```

```json
{
  "error": "Requested time is outside host's available hours"
}
```

```json
{
  "error": "Booking requires at least 4 hours notice"
}
```

```json
{
  "error": "Booking date exceeds the allowed future booking limit"
}
```

---

### 5. Get Host Bookings (Host)

Retrieve all bookings for the authenticated host.

**Endpoint:** `GET /api/bookings/host`

**Authentication:** Required (User-Id header)

**Headers:**
```json
{
  "User-Id": "123e4567-e89b-12d3-a456-426614174000"
}
```

**Success Response:**

**Status Code:** `200 OK`

```json
[
  {
    "id": "987e6543-e21b-12d3-a456-426614174999",
    "hostName": "John Doe",
    "hostEmail": "john.doe@company.com",
    "inviteeName": "Jane Smith",
    "inviteeEmail": "jane.smith@example.com",
    "startTime": "2026-01-10T10:00:00",
    "endTime": "2026-01-10T10:30:00",
    "status": "PENDING",
    "createdAt": "2026-01-04T15:30:00"
  },
  {
    "id": "111e2222-e21b-12d3-a456-426614174888",
    "hostName": "John Doe",
    "hostEmail": "john.doe@company.com",
    "inviteeName": "Bob Johnson",
    "inviteeEmail": "bob@example.com",
    "startTime": "2026-01-12T14:00:00",
    "endTime": "2026-01-12T14:30:00",
    "status": "CONFIRMED",
    "createdAt": "2026-01-03T10:15:00"
  },
  {
    "id": "222e3333-e21b-12d3-a456-426614174777",
    "hostName": "John Doe",
    "hostEmail": "john.doe@company.com",
    "inviteeName": "Alice Brown",
    "inviteeEmail": "alice@example.com",
    "startTime": "2026-01-08T09:00:00",
    "endTime": "2026-01-08T09:30:00",
    "status": "CANCELLED",
    "createdAt": "2026-01-02T14:20:00"
  }
]
```

**Booking Statuses:**
- `PENDING`: New booking request, awaiting host confirmation
- `CONFIRMED`: Host has confirmed the meeting
- `CANCELLED`: Booking has been cancelled by host

---

### 6. Confirm Booking (Host)

Confirm a pending booking request.

**Endpoint:** `PUT /api/bookings/{bookingId}/confirm`

**Authentication:** Not Required (No headers needed)

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `bookingId` | UUID | The unique identifier of the booking to confirm |

**Example Request:**
```
PUT /api/bookings/987e6543-e21b-12d3-a456-426614174999/confirm
```

**Success Response:**

**Status Code:** `200 OK`

```json
{
  "id": "987e6543-e21b-12d3-a456-426614174999",
  "hostName": "John Doe",
  "hostEmail": "john.doe@company.com",
  "inviteeName": "Jane Smith",
  "inviteeEmail": "jane.smith@example.com",
  "startTime": "2026-01-10T10:00:00",
  "endTime": "2026-01-10T10:30:00",
  "status": "CONFIRMED",
  "createdAt": "2026-01-04T15:30:00"
}
```

**Error Responses:**

**Status Code:** `404 Not Found`
```json
{
  "error": "Booking not found"
}
```

---

### 7. Cancel Booking (Host)

Cancel an existing booking (pending or confirmed).

**Endpoint:** `PUT /api/bookings/{bookingId}/cancel`

**Authentication:** Not Required (No headers needed)

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `bookingId` | UUID | The unique identifier of the booking to cancel |

**Example Request:**
```
PUT /api/bookings/987e6543-e21b-12d3-a456-426614174999/cancel
```

**Success Response:**

**Status Code:** `200 OK`

```json
{
  "id": "987e6543-e21b-12d3-a456-426614174999",
  "hostName": "John Doe",
  "hostEmail": "john.doe@company.com",
  "inviteeName": "Jane Smith",
  "inviteeEmail": "jane.smith@example.com",
  "startTime": "2026-01-10T10:00:00",
  "endTime": "2026-01-10T10:30:00",
  "status": "CANCELLED",
  "createdAt": "2026-01-04T15:30:00"
}
```

**Error Responses:**

**Status Code:** `404 Not Found`
```json
{
  "error": "Booking not found"
}
```

---

## 📊 Common Data Models

### BookingStatus Enum
```
PENDING    - New booking awaiting host confirmation
CONFIRMED  - Host has confirmed the meeting
CANCELLED  - Booking has been cancelled
```

### DayOfWeek Enum
```
MONDAY
TUESDAY
WEDNESDAY
THURSDAY
FRIDAY
SATURDAY
SUNDAY
```

### Timezone Examples
```
Asia/Dhaka
America/New_York
America/Los_Angeles
Europe/London
Europe/Paris
Australia/Sydney
UTC
```

---

## ⚠️ Error Responses

All error responses follow this structure:

```json
{
  "error": "Error message description"
}
```

### Common HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 OK | Request successful |
| 400 Bad Request | Invalid request data or validation error |
| 404 Not Found | Resource not found (user, booking, etc.) |
| 500 Internal Server Error | Server error |

---

## 🎬 Complete User Flow Example

### Scenario: Sales Rep Booking

#### Step 1: Host (John) Sets Up Availability
```bash
POST /api/availability/setup
Headers: { "User-Id": "123e4567-e89b-12d3-a456-426614174000" }
Body: {
  "meetingDurationMinutes": 30,
  "minNoticeHours": 4,
  "futureDaysAllowed": 60,
  "bufferTimeMinutes": 15,
  "timezone": "Asia/Dhaka",
  "weeklyAvailability": [...],
  "dateOverrides": [...]
}
```

#### Step 2: John Gets His Booking Link
```bash
GET /api/availability/link
Headers: { "User-Id": "123e4567-e89b-12d3-a456-426614174000" }
Response: "https://meetsync.app/u/john.doe"
```

#### Step 3: Invitee (Jane) Views Available Slots
```bash
GET /api/availability/u/john.doe?timezone=America/New_York
Response: [
  {
    "startTime": "2026-01-05T09:00:00",
    "endTime": "2026-01-05T09:30:00",
    "timezone": "America/New_York"
  },
  ...
]
```

#### Step 4: Jane Books a Slot
```bash
POST /api/bookings/u/john.doe
Body: {
  "inviteeName": "Jane Smith",
  "inviteeEmail": "jane.smith@example.com",
  "bookingDate": "2026-01-10",
  "bookingTime": "10:00:00"
}
Response: {
  "id": "987e6543-e21b-12d3-a456-426614174999",
  "status": "PENDING",
  ...
}
```

#### Step 5: John Views His Bookings
```bash
GET /api/bookings/host
Headers: { "User-Id": "123e4567-e89b-12d3-a456-426614174000" }
Response: [
  {
    "id": "987e6543-e21b-12d3-a456-426614174999",
    "inviteeName": "Jane Smith",
    "status": "PENDING",
    ...
  }
]
```

#### Step 6: John Confirms the Booking
```bash
PUT /api/bookings/987e6543-e21b-12d3-a456-426614174999/confirm
Response: {
  "id": "987e6543-e21b-12d3-a456-426614174999",
  "status": "CONFIRMED",
  ...
}
```

---

## 🔑 Key Features

### Slot Generation Logic
1. ✅ Uses host's weekly availability schedule
2. ✅ Applies date overrides (days off or custom hours)
3. ✅ Excludes already booked time slots
4. ✅ Enforces minimum notice requirement
5. ✅ Limits bookings to allowed future days
6. ✅ Adds buffer time between consecutive meetings
7. ✅ Converts times to invitee's timezone

### Booking Validation
1. ✅ Verifies host availability on requested date
2. ✅ Checks if time is within host's working hours
3. ✅ Prevents double-booking conflicts
4. ✅ Validates minimum notice requirement
5. ✅ Ensures booking is within allowed future window
6. ✅ Respects date overrides and days off

### Timezone Support
- Host sets their timezone in preferences
- Invitees can view slots in their own timezone
- All time conversions handled automatically
- Maintains accuracy across different timezones

---

## 📝 Notes

- All datetime values are in ISO 8601 format
- Timezone conversions are handled server-side
- Buffer time prevents back-to-back meetings
- Date overrides take precedence over weekly schedule
- Public endpoints (view slots, create booking) don't require authentication
- Host endpoints require User-Id header for authentication

---

**Version:** 1.0.0  
**Last Updated:** January 4, 2026
