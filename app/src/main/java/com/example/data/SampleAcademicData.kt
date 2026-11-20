package com.example.data

import com.example.model.AcademicEmail
import com.example.model.CalendarAlarmEvent
import com.example.model.ChatMessageItem
import com.example.model.EmailPriority
import com.example.model.FacultyMaterialItem
import com.example.model.FreshmanQaItem
import com.example.model.PlatformDownloadOption
import com.example.model.RepeatedQuestionCluster
import com.example.model.UserRole

object SampleAcademicData {

    val saudiBranches = listOf(
        "Riyadh (الرياض) — Saudi Arabia",
        "Jeddah (جدة) — Saudi Arabia",
        "Al-Madinah Al-Munawwarah (المدينة المنورة) — Saudi Arabia",
        "Dammam (الدمام) — Saudi Arabia",
        "Al-Ahsa (الأحساء) — Saudi Arabia",
        "Hail (حائل) — Saudi Arabia"
    )

    val regionalBranches = listOf(
        "Kuwait Branch (الكويت)",
        "Bahrain Branch (البحرين)",
        "Oman Branch (مسقط)",
        "Jordan Branch (عمّان)",
        "Lebanon Branch (بيروت)",
        "Egypt Branch (القاهرة)",
        "Palestine Branch (فلسطين)",
        "Sudan Branch (الخرطوم)"
    )

    val aouBranches = saudiBranches + regionalBranches

    val ringtones = listOf(
        "chime" to "Crystal Chime",
        "beacon" to "Academic Bell",
        "pulse" to "Digital Pulse",
        "horizon" to "Morning Horizon"
    )

    val platformDownloads: List<PlatformDownloadOption> = listOf(
        PlatformDownloadOption(
            id = "google_play",
            name = "Google Play (Android)",
            subtitle = "Native Android App • Material 3 + Offline Sync",
            badge = "Installed",
            instructions = "You are currently running the native Android edition of UniSync. Enable notifications and alarm permissions in Settings for continuous background alerts."
        ),
        PlatformDownloadOption(
            id = "app_store",
            name = "App Store (iOS & iPadOS)",
            subtitle = "iPhone & iPad Companion • Safari Web App Install",
            badge = "iOS / iPadOS",
            instructions = "Open UniSync in Safari on iPhone or iPad, tap the Share icon, and choose 'Add to Home Screen' for full-screen standalone access, or visit your institution's iOS portal."
        ),
        PlatformDownloadOption(
            id = "windows",
            name = "Windows 10 / 11",
            subtitle = "Desktop App via Microsoft Edge / Chrome PWA",
            badge = "Windows",
            instructions = "Open UniSync in Microsoft Edge or Chrome on Windows, click the 'Install App' icon in the address bar, and pin UniSync to your Start Menu or Taskbar."
        ),
        PlatformDownloadOption(
            id = "macos",
            name = "macOS (Apple Silicon & Intel)",
            subtitle = "Safari 'Add to Dock' / Chrome Desktop App",
            badge = "macOS",
            instructions = "Open UniSync in Safari on macOS Ventura or newer, choose File -> Add to Dock, or install via Chrome to launch UniSync directly from Spotlight and your Dock."
        )
    )

    fun sampleEmails(role: UserRole): List<AcademicEmail> {
        val now = System.currentTimeMillis()
        return if (role == UserRole.STUDENT) {
            listOf(
                AcademicEmail(
                    id = "stu_mail_1",
                    senderName = "Dr. Tariq Al-Mansoor",
                    senderEmail = "t.almansoor@aou.edu.kw",
                    recipientEmail = "student@aou.edu.kw",
                    subject = "TM351 TMA Part 2 Submission Deadline Extended + Rubric Update",
                    preview = "Dear TM351 students, please review the updated normalization rubric.",
                    fullBody = """Dear TM351 Students,

Please note that the submission deadline for TM351 Tutor-Marked Assignment (TMA) Part 2 has been extended by 48 hours to allow thorough testing of your MongoDB and PostgreSQL schemas.

Key requirements to verify before uploading to the AOU LMS:
- Include your ERD diagram in PDF format alongside SQL DDL scripts.
- Document every indexing decision for the analytical query benchmark (Question 3).
- Ensure your student ID and AOU branch appear on the cover page.

Important Dates:
- Updated TMA Deadline: November 24, 2026 at 11:55 PM
- Online Revision Clinic: November 22, 2026 at 6:00 PM

Submission Portal:
https://lms.aou.edu.kw/mod/assign/view.php?id=35102

Best regards,
Dr. Tariq Al-Mansoor
Faculty of Computer Studies — AOU""",
                    receivedAtLabel = "Today, 10:15 AM",
                    timestampMillis = now - 3_600_000L,
                    priority = EmailPriority.CRITICAL,
                    courseTag = "TM351",
                    unread = true
                ),
                AcademicEmail(
                    id = "stu_mail_2",
                    senderName = "AOU Examination Unit",
                    senderEmail = "exams@aou.edu.kw",
                    recipientEmail = "student@aou.edu.kw",
                    subject = "Final Midterm Seating & Hall Allocation for M269",
                    preview = "Your hall assignment for the M269 Midterm Exam is ready.",
                    fullBody = """Dear Student,

Your examination hall and seat number for M269 (Algorithms, Data Structures & Computability) have been finalized for the Fall 2026 term.

Exam Details:
- Date & Time: November 26, 2026 at 4:00 PM
- Hall: Building B, Hall 204 (Seat 18)
- Duration: 120 Minutes

Reminder:
1. Arrive at least 20 minutes prior to the exam start time.
2. Bring your valid University Student ID card.
3. Non-programmable calculators are permitted only if listed on the exam paper.

Exam Guidelines:
https://sis.aou.edu.kw/exams/regulations

Wishing you success,
AOU Examination & Assessment Unit""",
                    receivedAtLabel = "Yesterday, 4:40 PM",
                    timestampMillis = now - 64_800_000L,
                    priority = EmailPriority.HIGH,
                    courseTag = "M269",
                    unread = true
                ),
                AcademicEmail(
                    id = "stu_mail_3",
                    senderName = "Prof. Nadia Haddad",
                    senderEmail = "n.haddad@aou.edu.kw",
                    recipientEmail = "student@aou.edu.kw",
                    subject = "TM354 Weekly Tutorial Slides & UML Case Study",
                    preview = "Lecture 6 slides and the hospital domain model are posted.",
                    fullBody = """Hello everyone,

Thank you for your active participation in this week's TM354 Software Engineering tutorial.

I have uploaded the updated Lecture 6 slide deck covering:
- Sequence diagrams and state machine transitions
- Component interfaces and layered architectural patterns
- Annotated UML Hospital Management case study

Please review Section 4 before next Tuesday's live session so we can walk through the class diagram refactoring together.

Course Resources:
https://lms.aou.edu.kw/course/view.php?id=354

Warm regards,
Prof. Nadia Haddad""",
                    receivedAtLabel = "Nov 18, 2:10 PM",
                    timestampMillis = now - 150_000_000L,
                    priority = EmailPriority.NORMAL,
                    courseTag = "TM354",
                    unread = false,
                    viewed = true
                ),
                AcademicEmail(
                    id = "stu_mail_4",
                    senderName = "AOU Student Affairs",
                    senderEmail = "affairs@aou.edu.kw",
                    recipientEmail = "student@aou.edu.kw",
                    subject = "Campus Tech Career Fair & Alumni Networking Day",
                    preview = "Join regional engineering employers at the AOU Auditorium.",
                    fullBody = """Greetings AOU Students,

You are invited to attend the annual AOU Tech Career & Internship Fair next month.

Highlights:
- CV clinic with industry engineers
- Summer internship booths for FCS and Business undergraduates
- Alumni panel on transitioning from TM471 capstone projects to full-time roles

Registration is optional and free for all enrolled students.

Best wishes,
AOU Student Affairs Office""",
                    receivedAtLabel = "Nov 16, 9:00 AM",
                    timestampMillis = now - 345_600_000L,
                    priority = EmailPriority.LOW,
                    courseTag = "CAMPUS",
                    unread = false,
                    viewed = true
                )
            )
        } else {
            listOf(
                AcademicEmail(
                    id = "ins_mail_1",
                    senderName = "AOU Academic Deanery",
                    senderEmail = "deanery.fcs@aou.edu.kw",
                    recipientEmail = "faculty@aou.edu.kw",
                    subject = "Urgent: TM351 & TM354 Midterm Grade Moderation Window",
                    preview = "Please submit moderated MTA sheets by November 25.",
                    fullBody = """Dear Faculty Member,

The Faculty Assessment Board requests all section coordinators for TM351 and TM354 to finalize their Midterm Assessment (MTA) grade moderation sheets before the portal closes.

Action Items:
- Upload the signed moderation checklist on the Faculty SIS portal.
- Flag any borderline script reviews for second-marker verification.
- Confirm attendance logs for makeup exam candidates.

Key Deadline:
- Moderation Cut-off: November 25, 2026 at 5:00 PM

Faculty Portal:
https://sis.aou.edu.kw/faculty/moderation

Kind regards,
Office of the Dean — Faculty of Computer Studies""",
                    receivedAtLabel = "Today, 9:30 AM",
                    timestampMillis = now - 3_000_000L,
                    priority = EmailPriority.CRITICAL,
                    courseTag = "TM351",
                    unread = true
                ),
                AcademicEmail(
                    id = "ins_mail_2",
                    senderName = "Youssef Al-Kandari (Student)",
                    senderEmail = "2341098@aou.edu.kw",
                    recipientEmail = "faculty@aou.edu.kw",
                    subject = "TM351 TMA Part 2: Question 3 PostgreSQL Index Format",
                    preview = "Should we submit EXPLAIN ANALYZE screenshots or SQL text?",
                    fullBody = """Dear Doctor,

I hope you are doing well. Several classmates and I in Section 04 had a quick question regarding Question 3 in the TM351 TMA Part 2 assignment.

Could you clarify:
1. Should we attach EXPLAIN ANALYZE execution screenshots inside the Word/PDF report, or also paste the raw query plan text?
2. Is it acceptable to use composite B-Tree indexes alongside the primary key?

Thank you very much for your guidance.

Respectfully,
Youssef Al-Kandari
ID: 2341098 — Section 04""",
                    receivedAtLabel = "Today, 8:45 AM",
                    timestampMillis = now - 6_500_000L,
                    priority = EmailPriority.HIGH,
                    courseTag = "TM351",
                    unread = true
                ),
                AcademicEmail(
                    id = "ins_mail_3",
                    senderName = "Lina Mansour (Student)",
                    senderEmail = "2341152@aou.edu.kw",
                    recipientEmail = "faculty@aou.edu.kw",
                    subject = "M269 Exam Scope: Graph Traversal & Dynamic Programming",
                    preview = "Will Dijkstra's algorithm proof be included in the exam?",
                    fullBody = """Hello Doctor,

Could you please confirm whether the upcoming M269 exam on November 26 covers the formal proof of Dijkstra's shortest-path algorithm, or only tracing and time-complexity analysis?

Also, will the formula sheet for recurrence relations be provided on the exam paper?

Thank you,
Lina Mansour
ID: 2341152""",
                    receivedAtLabel = "Yesterday, 6:20 PM",
                    timestampMillis = now - 54_000_000L,
                    priority = EmailPriority.NORMAL,
                    courseTag = "M269",
                    unread = true
                ),
                AcademicEmail(
                    id = "ins_mail_4",
                    senderName = "AOU Library Services",
                    senderEmail = "library@aou.edu.kw",
                    recipientEmail = "faculty@aou.edu.kw",
                    subject = "Spring 2027 Textbook & E-Resource Adoption Requests",
                    preview = "Submit updated reading lists for next semester.",
                    fullBody = """Dear Colleagues,

AOU E-Library is collecting reading list updates for Spring 2027 courses. If you plan to add new ACM/IEEE digital references or O'Reilly texts to your course syllabus, please submit your request by December 10.

Thank you,
AOU Central Library""",
                    receivedAtLabel = "Nov 17, 11:05 AM",
                    timestampMillis = now - 250_000_000L,
                    priority = EmailPriority.LOW,
                    courseTag = "ADMIN",
                    unread = false,
                    viewed = true
                )
            )
        }
    }

    val arabicEmailTranslations: Map<String, Pair<String, String>> = mapOf(
        "stu_mail_1" to (
            "تمديد موعد تسليم الواجب TM351 (الجزء الثاني) وتحديث معايير التقييم" to
            """طلاب مقرر TM351 الأعزاء،

نحيطكم علماً بأنه تم تمديد موعد تسليم الجزء الثاني من الواجب الدراسي (TMA Part 2) لمدة 48 ساعة لإتاحة الوقت الكافي لاختبار قواعد بيانات MongoDB وPostgreSQL.

المتطلبات الأساسية قبل الرفع على نظام LMS:
- إرفاق مخطط ERD بصيغة PDF مع نصوص أوامر SQL DDL.
- توثيق جميع قرارات الفهرسة لتحليل أداء الاستعلام (السؤال الثالث).
- التأكد من كتابة الرقم الجامعي وفرع الجامعة على صفحة الغلاف.

المواعيد المهمة:
- الموعد النهائي المحدّث للتسليم: 24 نوفمبر 2026 الساعة 11:55 مساءً
- جلسة المراجعة المباشرة: 22 نوفمبر 2026 الساعة 6:00 مساءً

رابط بوابة التسليم:
https://lms.aou.edu.kw/mod/assign/view.php?id=35102

مع أطيب التحيات،
د. طارق المنصور
كلية دراسات الحاسوب — الجامعة العربية المفتوحة"""
        ),
        "stu_mail_2" to (
            "توزيع قاعات ومقاعد الاختبار النصفي لمقرر M269" to
            """عزيزي الطالب،

تم اعتماد توزيع قاعة الاختبار ورقم المقعد لمقرر M269 (الخوارزميات وهياكل البيانات والحوسبة) للفصل الدراسي خريف 2026.

تفاصيل الاختبار:
- التاريخ والوقت: 26 نوفمبر 2026 الساعة 4:00 عصراً
- القاعة: مبنى ب، قاعة 204 (مقعد 18)
- المدة: 120 دقيقة

تنبيهات مهمة:
1. الحضور قبل موعد الاختبار بـ 20 دقيقة على الأقل.
2. إحضار البطاقة الجامعية سارية المفعول.
3. يُسمح بالآلة الحاسبة غير القابلة للبرمجة فقط إذا ذُكر ذلك في ورقة الاختبار.

لوائح الاختبارات:
https://sis.aou.edu.kw/exams/regulations

مع تمنياتنا لكم بالتوفيق،
وحدة الاختبارات والتقييم — الجامعة العربية المفتوحة"""
        ),
        "stu_mail_3" to (
            "شرائح المحاضرة السادسة ودراسة حالة UML لمقرر TM354" to
            """السلام عليكم جميعاً،

شكراً لمشاركتكم الفعّالة في درس مقرر TM354 هندسة البرمجيات لهذا الأسبوع.

تم رفع شرائح المحاضرة السادسة المحدّثة وتشمل:
- مخططات التسلسل وانتقالات الآلات ذات الحالات.
- واجهات المكونات والأنماط المعمارية الطبقية.
- نموذج مجال إدارة المستشفيات بلغة UML.

يرجى مراجعة القسم الرابع قبل الجلسة المباشرة يوم الثلاثاء القادم لمناقشة تحسين مخطط الفئات معاً.

مصادر المقرر:
https://lms.aou.edu.kw/course/view.php?id=354

مع خالص التقدير،
أ.د. نادية حداد"""
        ),
        "stu_mail_4" to (
            "معرض التوظيف التقني ويوم الخريجين بالجامعة" to
            """طلاب الجامعة العربية المفتوحة الأعزاء،

يسرّنا دعوتكم لحضور معرض التوظيف والتدريب التقني السنوي في الشهر القادم.

أبرز الفعاليات:
- ورشة مراجعة السيرة الذاتية مع مهندسين من القطاع التقني.
- أجنحة التدريب الصيفي لطلبة كلية الحاسوب وإدارة الأعمال.
- لقاء الخريجين حول الانتقال من مشاريع التخرج TM471 إلى سوق العمل.

التسجيل مجاني واختياري لجميع الطلبة المسجلين.

مع أطيب التمنيات،
شؤون الطلبة — الجامعة العربية المفتوحة"""
        ),
        "ins_mail_1" to (
            "عاجل: فترة اعتماد ومراجعة درجات الاختبار النصفي لمقرري TM351 وTM354" to
            """سعادة عضو هيئة التدريس المحترم،

تطلب لجنة التقييم بالكلية من جميع منسقي الشعب لمقرري TM351 وTM354 اعتماد نماذج مراجعة درجات الاختبار النصفي (MTA) قبل إغلاق البوابة الإلكترونية.

الإجراءات المطلوبة:
- رفع قائمة التحقق الموقّعة للمراجعة عبر بوابة أعضاء هيئة التدريس SIS.
- تمييز الأوراق الحدية لإتمام التدقيق من المصحح الثاني.
- تأكيد سجلات الحضور للمتقدمين للاختبار البديل.

الموعد النهائي:
- إغلاق بوابة الاعتماد: 25 نوفمبر 2026 الساعة 5:00 مساءً

بوابة أعضاء هيئة التدريس:
https://sis.aou.edu.kw/faculty/moderation

مع وافر التقدير،
مكتب عميد كلية دراسات الحاسوب"""
        ),
        "ins_mail_2" to (
            "استفسار حول الواجب TM351 الجزء الثاني: صيغة فهارس PostgreSQL في السؤال الثالث" to
            """سعادة الدكتور الفاضل،
السلام عليكم ورحمة الله وبركاته،

نأمل أن تكونوا بخير. لديّ أنا وعدد من زملائي في الشعبة 04 استفسار حول السؤال الثالث في الواجب TM351 الجزء الثاني.

نرجو توضيح الآتي:
1. هل نرفق لقطات شاشة لنتائج EXPLAIN ANALYZE داخل ملف Word/PDF أم نلصق نص خطة التنفيذ أيضاً؟
2. هل يُسمح باستخدام الفهارس المركبة B-Tree إلى جانب المفتاح الأساسي؟

شاكرين ومقدّرين توجيهكم الكريم.

وتفضلوا بقبول فائق الاحترام،
يوسف الكندري
الرقم الجامعي: 2341098 — الشعبة 04"""
        ),
        "ins_mail_3" to (
            "نطاق اختبار مقرر M269: اجتياز الرسوم البيانية والبرمجة الديناميكية" to
            """مرحباً دكتور،

هل يشمل اختبار M269 القادم بتاريخ 26 نوفمبر الإثبات النظري لخوارزمية ديكسترا لأقصر مسار، أم يقتصر على التتبع وتحليل التعقيد الزمني؟

وهل ستُرفق ورقة القوانين الخاصة بعلاقات التكرار في ورقة الاختبار؟

شكراً لكم،
لينا منصور
الرقم الجامعي: 2341152"""
        ),
        "ins_mail_4" to (
            "طلبات اعتماد الكتب والمصادر الإلكترونية لفصل ربيع 2027" to
            """الزملاء الأفاضل،

تجمع المكتبة الإلكترونية بالجامعة العربية المفتوحة قوائم القراءة المحدّثة لمقررات فصل ربيع 2027. في حال رغبتكم بإضافة مراجع رقمية جديدة من ACM أو IEEE أو O'Reilly ضمن خطة المقرر، يرجى إرسال الطلب قبل 10 ديسمبر.

مع الشكر والتقدير،
المكتبة المركزية — الجامعة العربية المفتوحة"""
        )
    )

    val repeatedStudentQuestions: List<RepeatedQuestionCluster> = listOf(
        RepeatedQuestionCluster(
            id = "rq_1",
            topic = "TM351 TMA Part 2: EXPLAIN ANALYZE & File Format",
            courseCode = "TM351",
            askCount = 14,
            summary = "14 students asked whether to include SQL text + EXPLAIN ANALYZE screenshots in PDF.",
            studentEmails = listOf("2341098@aou.edu.kw", "2341104@aou.edu.kw", "2341189@aou.edu.kw")
        ),
        RepeatedQuestionCluster(
            id = "rq_2",
            topic = "M269 Exam Coverage: Dijkstra & Recurrence Sheet",
            courseCode = "M269",
            askCount = 9,
            summary = "9 students asked if Dijkstra formal proofs & Master Theorem sheet are included.",
            studentEmails = listOf("2341152@aou.edu.kw", "2341211@aou.edu.kw")
        )
    )

    fun defaultCalendarEvents(role: UserRole): List<CalendarAlarmEvent> {
        val now = System.currentTimeMillis()
        return if (role == UserRole.STUDENT) {
            listOf(
                CalendarAlarmEvent(
                    id = "cal_stu_1",
                    userId = "demo",
                    title = "TM351 Live Revision Clinic",
                    courseCode = "TM351",
                    category = "lecture",
                    eventTimeMillis = now + 2 * 3_600_000L + 15 * 60_000L,
                    alarmEnabled = true,
                    ringtoneId = "chime",
                    source = "aou_sync"
                ),
                CalendarAlarmEvent(
                    id = "cal_stu_2",
                    userId = "demo",
                    title = "TM351 TMA Part 2 Submission",
                    courseCode = "TM351",
                    category = "assignment",
                    eventTimeMillis = now + 4 * 86_400_000L,
                    alarmEnabled = true,
                    ringtoneId = "beacon",
                    source = "aou_sync"
                ),
                CalendarAlarmEvent(
                    id = "cal_stu_3",
                    userId = "demo",
                    title = "M269 Midterm Exam (Hall 204)",
                    courseCode = "M269",
                    category = "exam",
                    eventTimeMillis = now + 6 * 86_400_000L,
                    alarmEnabled = true,
                    ringtoneId = "pulse",
                    source = "aou_sync"
                )
            )
        } else {
            listOf(
                CalendarAlarmEvent(
                    id = "cal_ins_1",
                    userId = "demo",
                    title = "TM351 Section 04 Lecture",
                    courseCode = "TM351",
                    category = "lecture",
                    eventTimeMillis = now + 1 * 3_600_000L + 40 * 60_000L,
                    alarmEnabled = true,
                    ringtoneId = "chime",
                    source = "aou_sync"
                ),
                CalendarAlarmEvent(
                    id = "cal_ins_2",
                    userId = "demo",
                    title = "Office Hours — M269 & TM354",
                    courseCode = "M269",
                    category = "office_hour",
                    eventTimeMillis = now + 26 * 3_600_000L,
                    alarmEnabled = true,
                    ringtoneId = "horizon",
                    source = "aou_sync"
                ),
                CalendarAlarmEvent(
                    id = "cal_ins_3",
                    userId = "demo",
                    title = "MTA Moderation Submission Deadline",
                    courseCode = "TM351",
                    category = "assignment",
                    eventTimeMillis = now + 5 * 86_400_000L,
                    alarmEnabled = true,
                    ringtoneId = "beacon",
                    source = "aou_sync"
                )
            )
        }
    }

    val defaultFacultyMaterials: List<FacultyMaterialItem> = listOf(
        FacultyMaterialItem(
            id = "mat_demo_1",
            authorId = "demo_instructor",
            authorName = "Dr. Tariq Al-Mansoor",
            courseCode = "TM351",
            title = "Week 6: Query Optimization & Indexing Guide",
            description = "Slides + PostgreSQL EXPLAIN ANALYZE examples for TMA Part 2.",
            resourceUrl = "https://lms.aou.edu.kw/mod/resource/view.php?id=35106",
            materialType = "slides",
            published = true
        ),
        FacultyMaterialItem(
            id = "mat_demo_2",
            authorId = "demo_instructor",
            authorName = "Dr. Tariq Al-Mansoor",
            courseCode = "M269",
            title = "Graph Algorithms Walkthrough Session",
            description = "Recorded revision tutorial covering BFS, DFS, and Dijkstra complexity.",
            resourceUrl = "https://teams.microsoft.com/l/meetup-join/aou-m269-rev",
            materialType = "recording",
            published = true
        )
    )

    val defaultChatMessages: List<ChatMessageItem> = listOf(
        ChatMessageItem(
            id = "chat_dir_1",
            senderId = "demo_instructor",
            senderName = "Dr. Tariq Al-Mansoor",
            senderRole = "instructor",
            channel = "direct",
            participants = listOf("demo_student", "demo_instructor"),
            courseTag = "TM351",
            text = "Welcome to Direct Course Chat. Feel free to ask any TM351 or M269 question or share a screenshot of your schema/code here."
        ),
        ChatMessageItem(
            id = "chat_gen_1",
            senderId = "peer_sara",
            senderName = "Sara (CS Year 3)",
            senderRole = "student",
            channel = "general",
            participants = emptyList(),
            courseTag = "M269",
            text = "Anyone revising M269 trees and graphs tonight in the Pomodoro focus room?"
        ),
        ChatMessageItem(
            id = "chat_gen_2",
            senderId = "peer_omar",
            senderName = "Omar (ITC)",
            senderRole = "student",
            channel = "general",
            participants = emptyList(),
            courseTag = "TM351",
            text = "Yes! Just finished indexing Question 3 for TMA Part 2."
        )
    )

    val freshmenQaList: List<FreshmanQaItem> = listOf(
        FreshmanQaItem(
            id = "fq_1",
            category = "LMS & Portal",
            question = "How do I access my AOU LMS and SIS accounts during my first week?",
            answer = "Use your official AOU student email (studentID@aou.edu...) and the initial password issued by Admissions. SIS handles course registration and grades, while LMS hosts lecture slides, TMAs, and Turnitin links."
        ),
        FreshmanQaItem(
            id = "fq_2",
            category = "Grading & Assessment",
            question = "How are AOU courses graded (TMA, MTA, Final Exam)?",
            answer = "Most AOU courses divide 100 marks into Continuous Assessment (TMA 20% + Midterm/MTA 30%) and Final Exam (50%). To pass a course, you typically must score at least 50% overall AND meet the minimum threshold in both continuous assessment and the final exam."
        ),
        FreshmanQaItem(
            id = "fq_3",
            category = "Registration",
            question = "What do TU170, EL111, AR111, and GR101 cover?",
            answer = "These are mandatory University Requirements taken in your first year: TU170 builds digital & online learning skills, EL111/AR111 build academic communication, and GR101 teaches university self-learning methodology."
        ),
        FreshmanQaItem(
            id = "fq_4",
            category = "Prerequisites",
            question = "Why can't I register M251 or M269 yet?",
            answer = "M251 requires passing TM105 (Introduction to Programming), and M269 requires both TM105 and MT131 (Discrete Mathematics). Check your visual Roadmap in UniSync to see prerequisite arrows."
        ),
        FreshmanQaItem(
            id = "fq_5",
            category = "Academic Integrity",
            question = "What similarity guidelines apply to TMA submissions on Turnitin?",
            answer = "Always write answers in your own words, cite all references using Harvard/IEEE style as specified in the TMA guide, and review your Turnitin similarity report early before the deadline."
        )
    )
}
