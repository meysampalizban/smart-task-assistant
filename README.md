# 🧠 Smart Task Assistant

> یک REST API مدرن که وظایف روزمره را با کمک هوش مصنوعی دسته‌بندی، اولویت‌بندی و به زیروظایف عملی می‌شکند — کاربر فقط عنوان وظیفه را می‌نویسد، بقیه کارها با LLM انجام می‌شود.

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-2.0-6DB33F?logo=spring)](https://spring.io/projects/spring-ai)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql)](https://www.postgresql.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 💡 ایده پروژه

در یک Todo List معمولی، کاربر باید برای هر وظیفه دستی مشخص کند که مربوط به چه دسته‌ای است، چقدر فوری است و برای انجامش چه قدم‌هایی لازم است. این کار خسته‌کننده است و در عمل کاربران معمولاً از آن صرف‌نظر می‌کنند.

**Smart Task Assistant** این بار را از دوش کاربر برمی‌دارد. کاربر فقط عنوان خام وظیفه را می‌فرستد:

```json
{ "title": "فردا باید با بانک تماس بگیرم برای مسئله کارت اعتباری" }
```

و سرور با فراخوانی یک مدل زبانی، پاسخ غنی‌شده برمی‌گرداند:

```json
{
  "id": 42,
  "title": "فردا باید با بانک تماس بگیرم برای مسئله کارت اعتباری",
  "category": "FINANCE",
  "priority": "HIGH",
  "status": "PENDING",
  "subtasks": [
    "پیدا کردن شماره تماس بانک",
    "آماده کردن شماره کارت و مدارک",
    "تماس و توضیح مشکل"
  ],
  "enriched": true,
  "createdAt": "2026-11-19T13:54:08Z"
}
```

---

## ✨ ویژگی‌های کلیدی

- 🤖 **غنی‌سازی خودکار با AI**: دسته‌بندی، اولویت‌بندی و تولید زیروظایف با استفاده از LLM (OpenAI)
- 🛡️ **طراحی مقاوم**: اگر سرویس AI شکست بخورد، وظیفه با مقادیر پیش‌فرض ذخیره می‌شود و کاربر می‌تواند بعداً غنی‌سازی را دوباره اجرا کند
- ⚡ **Virtual Threads**: مقیاس‌پذیری بالا برای بارهای I/O-bound (تماس با LLM) بدون افزایش مصرف حافظه
- 📊 **مشاهده‌پذیری**: متریک‌های آماده از طریق Actuator (health, metrics)
- 🗄️ **نسخه‌بندی اسکیما با Flyway**: مهاجرت‌های دیتابیس قابل ردیابی و تکرارپذیر
- 🧪 **تست‌های چندلایه**: تست واحد برای سرویس AI، تست یکپارچه برای Controller
- 🐳 **اجرا با یک دستور**: راه‌اندازی کامل با Docker Compose

---

## 🏗️ معماری

```
┌──────────────────────────────────────────────────────────────┐
│                          Client                              │
│                   (Browser / Mobile / cURL)                  │
└──────────────────────────┬───────────────────────────────────┘
                           │ HTTP/JSON
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                     TaskController                           │
│   POST /tasks  ·  GET /tasks  ·  PATCH /tasks/{id}  ·  ...   │
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                      TaskService                             │
│        منطق کسب‌وکار · هماهنگی با AI · تبدیل DTO             │
└──────────┬───────────────────────────────┬───────────────────┘
           │                               │
           ▼                               ▼
┌──────────────────────┐      ┌────────────────────────────────┐
│   TaskRepository     │      │  TaskEnrichmentService         │
│   (Spring Data JPA)  │      │  (Spring AI ChatClient)        │
└──────────┬───────────┘      └──────────────┬─────────────────┘
           │                                 │
           ▼                                 ▼
┌──────────────────────┐      ┌────────────────────────────────┐
│     PostgreSQL       │      │         OpenAI API             │
│   (Flyway migrations)│      │      (gpt-4o-mini)             │
└──────────────────────┘      └────────────────────────────────┘
```

### جریان یک درخواست ایجاد وظیفه

1. کاربر `POST /api/v1/tasks` با عنوان خام می‌فرستد.
2. `TaskController` درخواست را با `@Valid` اعتبارسنجی می‌کند.
3. `TaskService` وظیفه را **قبل از** تماس با AI ذخیره می‌کند — تا در صورت شکست AI، داده از دست نرود.
4. `TaskEnrichmentService` با `ChatClient` به OpenAI وصل می‌شود و پاسخ را به یک رکورد `TaskSuggestion` تبدیل می‌کند.
5. نتیجه روی وظیفه اعمال و دوباره ذخیره می‌شود.
6. پاسخ نهایی (غنی‌شده یا با `enriched=false`) به کاربر برمی‌گردد.

---

## 🛠️ تکنولوژی‌ها

| لایه | تکنولوژی | نسخه | چرا؟ |
|---|---|---|---|
| زبان | Java | 25 (LTS) | Virtual Threads، Records، Pattern Matching |
| فریم‌ورک | Spring Boot | 4.1.2 | آخرین نسخه پایدار با پشتیبانی کامل از Java 25 |
| AI | Spring AI | 2.0 | انتزاع رسمی اسپرینگ برای LLM، خروجی تایپ‌شده |
| مدل | OpenAI | gpt-4o-mini | تعادل خوب بین هزینه و کیفیت |
| دیتابیس | PostgreSQL | 17 | استاندارد صنعت، پشتیبانی عالی از JSON و enum |
| ORM | Spring Data JPA | (از Boot) | کاهش کد boilerplate |
| Migration | Flyway | (از Boot) | نسخه‌بندی اسکیما |
| Build | Maven | 3.9+ | استاندارد و ساده |
| Container | Docker Compose | - | اجرای یکپارچه |

---

## 🚀 شروع سریع

### پیش‌نیازها

- **JDK 27** یا بالاتر ([دانلود](https://adoptium.net/))
- **Maven 3.9+**
- **Docker** و **Docker Compose**
- یک **کلید API از OpenAI** ([دریافت](https://platform.openai.com/api-keys))

### ۱. کلون پروژه

```bash
git clone https://github.com/meysampalizban/smart-task-assistant.git
cd smart-task-assistant
```

### ۲. تنظیم کلید API

یک فایل `.env` در ریشه پروژه بسازید:

```bash
echo "OPENAI_API_KEY=sk-your-key-here" > .env
```

> ⚠️ فایل `.env` در `.gitignore` قرار دارد و هرگز commit نمی‌شود.

### ۳. اجرا با Docker Compose

```bash
docker compose up -d postgres
mvn spring-boot:run
```

یا اجرای کامل (دیتابیس + اپلیکیشن):

```bash
docker compose up --build
```

اپلیکیشن روی `http://localhost:8080` در دسترس است.

### ۴. بررسی سلامت

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

---

## 📚 مرجع API

### `POST /api/v1/tasks` — ایجاد وظیفه

```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"آماده‌سازی ارائه برای جلسه هفته آینده"}'
```

**پاسخ (201 Created):**
```json
{
  "id": 1,
  "title": "آماده‌سازی ارائه برای جلسه هفته آینده",
  "category": "WORK",
  "priority": "HIGH",
  "status": "PENDING",
  "subtasks": [
    "جمع‌آوری داده‌ها و آمار هفته گذشته",
    "طراحی اسلایدها و تعیین ساختار ارائه",
    "تمرین ارائه و آماده‌سازی پاسخ به سوالات"
  ],
  "enriched": true,
  "createdAt": "2026-11-19T13:54:08Z"
}
```

### `GET /api/v1/tasks` — لیست با فیلتر و صفحه‌بندی

```bash
curl "http://localhost:8080/api/v1/tasks?status=PENDING&priority=HIGH&page=0&size=10"
```

پارامترهای اختیاری: `status`، `priority`، `category`، `page`، `size`، `sort`.

### `GET /api/v1/tasks/{id}` — مشاهده یک وظیفه

```bash
curl http://localhost:8080/api/v1/tasks/1
```

### `PATCH /api/v1/tasks/{id}` — به‌روزرسانی جزئی

```bash
curl -X PATCH http://localhost:8080/api/v1/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"status":"DONE"}'
```

### `DELETE /api/v1/tasks/{id}` — حذف

```bash
curl -X DELETE http://localhost:8080/api/v1/tasks/1
# 204 No Content
```

### `POST /api/v1/tasks/{id}/enrich` — اجرای مجدد غنی‌سازی AI

```bash
curl -X POST http://localhost:8080/api/v1/tasks/1/enrich
```

### کدهای خطای استاندارد

| کد | معنا | مثال |
|---|---|---|
| `400` | خطای اعتبارسنجی | عنوان خالی |
| `404` | وظیفه پیدا نشد | `TASK_NOT_FOUND` |
| `502` | خطای سرویس AI | `AI_ENRICHMENT_FAILED` |
| `500` | خطای غیرمنتظره | `INTERNAL_ERROR` |

همه خطاها ساختار یکسان دارند:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "درخواست معتبر نیست",
  "fieldErrors": [
    { "field": "title", "message": "نباید خالی باشد" }
  ],
  "timestamp": "2026-11-19T13:54:08Z"
}
```

---

## 🗂️ ساختار پروژه

```
smart-task-assistant/
├── src/main/java/ir/meysampalizban/sta/
│   ├── SmartTaskAssistantApplication.java
│   ├── ai/
│   │   ├── TaskEnrichmentService.java       # قرارداد
│   │   └── OpenAiTaskEnrichmentService.java # پیاده‌سازی
│   ├── common/
│   │   ├── EnrichmentException.java
│   │   ├── ErrorResponse.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── TaskNotFoundException.java
│   ├── config/
│   │   └── AiConfig.java                    # ChatClient Bean
│   └── task/
│       ├── Task.java                        # Entity
│       ├── Subtask.java                     # Entity
│       ├── TaskRepository.java
│       ├── TaskService.java
│       ├── TaskController.java
│       ├── TaskCategory.java
│       ├── TaskPriority.java
│       ├── TaskStatus.java
│       └── dto/
│           ├── CreateTaskRequest.java
│           ├── UpdateTaskRequest.java
│           └── TaskResponse.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       └── V1__create_task_tables.sql
├── src/test/java/...
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 🧪 اجرای تست‌ها

```bash
mvn test
```

تست‌های موجود:

- **`TaskServiceTest`**: با mock کردن `TaskEnrichmentService` بررسی می‌کند که منطق ذخیره‌سازی و به‌روزرسانی درست کار می‌کند و خطای AI در `create` بلعیده می‌شود.
- **`TaskControllerIT`**: با `@SpringBootTest` و `MockMvc` سناریوی کامل ایجاد، لیست و به‌روزرسانی را بررسی می‌کند.

---

## 🎯 تصمیم‌های طراحی

### چرا `create` تراکنشال نیست؟

اگر کل متد `create` در یک تراکنش بود، اتصال دیتابیس در طول ۲ تا ۱۰ ثانیه‌ای که منتظر پاسخ OpenAI هستیم **قفل** می‌ماند. با ۱۰ درخواست همزمان، pool ده‌تایی Hikari اشباع می‌شد. با جدا کردن دو `save()`، اتصال بین آن‌ها آزاد می‌شود. این الگو با Virtual Threads ترکیب می‌شود تا مقیاس‌پذیری واقعی به دست آید.

### چرا خطای AI در `create` بلعیده می‌شود اما در `enrich` نه؟

- در `create`، کاربر فقط می‌خواست وظیفه را ثبت کند. AI یک «امکانات اضافه» است. شکست آن نباید مانع ثبت شود — وظیفه با `enriched=false` ذخیره می‌شود و بعداً می‌توان `POST /{id}/enrich` را صدا زد.
- در `enrich`، کاربر **آگاهانه** درخواست غنی‌سازی داده. اگر شکست بخورد، باید بداند — پاسخ `502 Bad Gateway` با پیام واضح.

### چرا `ddl-auto: validate` و نه `update`؟

`ddl-auto: update` در پروداکشن خطرناک است — ممکن است ستون‌ها را بدون اطلاع شما تغییر دهد یا در هنگام تغییر enum ها رفتار غیرقابل پیش‌بینی داشته باشد. Flyway اسکیما را می‌سازد، و Hibernate فقط **اعتبارسنجی** می‌کند که Entity ها با جدول‌ها هم‌خوان باشند.

### چرا `open-in-view: false`؟

`spring.jpa.open-in-view` به‌طور پیش‌فرض `true` است و باعث می‌شود اتصال دیتابیس تا زمان سریالایز شدن پاسخ باز بماند — که در REST API هیچ فایده‌ای ندارد و فقط pool را هدر می‌دهد.

---

## 🤝 مشارکت

Pull Request ها خوش‌آمدند. برای تغییرات بزرگ، لطفاً ابتدا یک Issue باز کنید تا در مورد رویکرد بحث کنیم.

---

## 📄 لایسنس

این پروژه تحت لایسنس [MIT](LICENSE) منتشر شده است.

---

## 🙏 قدردانی

- [Spring AI](https://spring.io/projects/spring-ai) برای انتزاع زیبای LLM
- [Testcontainers](https://testcontainers.com/) برای تست‌های یکپارچه واقعی
- [Flyway](https://flywaydb.org/) برای نسخه‌بندی بدون دردسر اسکیما

---

**ساخته شده با ☕ و کمی ✨ هوش مصنوعی**
