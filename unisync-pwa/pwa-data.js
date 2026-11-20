window.UNISYNC_DATA = (function () {
  const MS_CLIENT_ID = "8326f723-eb8a-4288-b346-9614eb8608c3";
  const MS_SCOPES = "openid profile email User.Read Mail.Read Mail.Send Mail.ReadWrite";

  const SAUDI_BRANCHES = [
    "Riyadh (الرياض) — Saudi Arabia",
    "Jeddah (جدة) — Saudi Arabia",
    "Al-Madinah Al-Munawwarah (المدينة المنورة) — Saudi Arabia",
    "Dammam (الدمام) — Saudi Arabia",
    "Al-Ahsa (الأحساء) — Saudi Arabia",
    "Hail (حائل) — Saudi Arabia"
  ];

  const REGIONAL_BRANCHES = [
    "Kuwait Branch (الكويت)",
    "Bahrain Branch (البحرين)",
    "Oman Branch (مسقط)",
    "Jordan Branch (عمّان)",
    "Lebanon Branch (بيروت)",
    "Egypt Branch (القاهرة)",
    "Palestine Branch (فلسطين)",
    "Sudan Branch (الخرطوم)"
  ];

  const LANGUAGES = [
    { code: "en", nativeName: "English", englishName: "English", rtl: false },
    { code: "ar", nativeName: "العربية", englishName: "Arabic", rtl: true },
    { code: "fr", nativeName: "Français", englishName: "French", rtl: false },
    { code: "es", nativeName: "Español", englishName: "Spanish", rtl: false },
    { code: "de", nativeName: "Deutsch", englishName: "German", rtl: false },
    { code: "tr", nativeName: "Türkçe", englishName: "Turkish", rtl: false },
    { code: "ur", nativeName: "اردو", englishName: "Urdu", rtl: true },
    { code: "fa", nativeName: "فارسی", englishName: "Persian", rtl: true },
    { code: "ms", nativeName: "Bahasa Melayu", englishName: "Malay", rtl: false },
    { code: "id", nativeName: "Bahasa Indonesia", englishName: "Indonesian", rtl: false },
    { code: "zh", nativeName: "中文", englishName: "Chinese", rtl: false },
    { code: "ja", nativeName: "日本語", englishName: "Japanese", rtl: false },
    { code: "ko", nativeName: "한국어", englishName: "Korean", rtl: false },
    { code: "hi", nativeName: "हिन्दी", englishName: "Hindi", rtl: false },
    { code: "bn", nativeName: "বাংলা", englishName: "Bengali", rtl: false },
    { code: "ru", nativeName: "Русский", englishName: "Russian", rtl: false },
    { code: "pt", nativeName: "Português", englishName: "Portuguese", rtl: false },
    { code: "it", nativeName: "Italiano", englishName: "Italian", rtl: false },
    { code: "nl", nativeName: "Nederlands", englishName: "Dutch", rtl: false },
    { code: "sw", nativeName: "Kiswahili", englishName: "Swahili", rtl: false }
  ];

  const UI_STRINGS = {
    en: {
      studentRole: "Student / طالب",
      instructorRole: "Instructor / Faculty",
      signInMicrosoft: "Sign in with Microsoft / Outlook",
      signInAcademic: "Academic Workspace Login",
      demoAccount: "Demo Account",
      installApp: "Install & Platforms",
      darkMode: "Dark Mode",
      language: "Language",
      home: "Home",
      inbox: "Inbox",
      pomodoro: "Pomodoro",
      chat: "Chat",
      roadmaps: "Roadmaps",
      materials: "Materials",
      calendarAlarm: "Calendar & Alarm",
      tools: "Tools",
      settings: "Settings",
      nextUp: "Next Up",
      repeatedQuestions: "Most Repeated Student Questions",
      unifiedReplyAll: "Unified Reply to All",
      summarizeEmail: "Summarize Email",
      smartReply: "Generate Smart Reply",
      extractDates: "Extract Dates to Calendar",
      sendReply: "Send Reply",
      backToInbox: "Back to Inbox",
      all: "All",
      signOut: "Sign Out",
      urgentEmailAlert: "Important Unreplied Email",
      tapToOpenReply: "Tap to open & reply",
      repliedReviewed: "Replied & Reviewed",
      menu: "Menu",
      exitPomodoro: "Exit Pomodoro",
      studyFocus: "Study Focus",
      breakTime: "Break Time",
      allCaughtUp: "All caught up — no pending repeated questions",
      compose: "Compose"
    },
    ar: {
      studentRole: "طالب",
      instructorRole: "عضو هيئة تدريس",
      signInMicrosoft: "تسجيل الدخول عبر Microsoft / Outlook",
      signInAcademic: "دخول البريد الأكاديمي",
      demoAccount: "حساب تجريبي",
      installApp: "تثبيت التطبيق على الجهاز",
      darkMode: "الوضع الداكن",
      language: "اللغة",
      home: "الرئيسية",
      inbox: "البريد",
      pomodoro: "بومودورو",
      chat: "المحادثة",
      roadmaps: "الخطط الدراسية",
      materials: "المواد والروابط",
      calendarAlarm: "التقويم والمنبه",
      tools: "الأدوات",
      settings: "الإعدادات",
      nextUp: "القادم",
      repeatedQuestions: "أكثر أسئلة الطلاب تكراراً",
      unifiedReplyAll: "رد موحّد للجميع",
      summarizeEmail: "تلخيص الرسالة",
      smartReply: "توليد رد ذكي",
      extractDates: "استخراج المواعيد للتقويم",
      sendReply: "إرسال الرد",
      backToInbox: "العودة للبريد",
      all: "الكل",
      signOut: "تسجيل الخروج",
      urgentEmailAlert: "تنبيه: رسالة مهمة بانتظار الرد",
      tapToOpenReply: "اضغط للفتح والرد",
      repliedReviewed: "المردود والمقروء",
      menu: "القائمة",
      exitPomodoro: "إنهاء بومودورو",
      studyFocus: "تركيز الدراسة",
      breakTime: "وقت الاستراحة",
      allCaughtUp: "تم الرد على جميع الأسئلة المتكررة",
      compose: "رسالة جديدة"
    },
    fr: {
      studentRole: "Étudiant",
      instructorRole: "Enseignant",
      signInMicrosoft: "Continuer avec Microsoft / Outlook",
      signInAcademic: "Connexion Académique",
      demoAccount: "Compte Démo",
      installApp: "Installer l'application",
      darkMode: "Mode sombre",
      language: "Langue",
      home: "Accueil",
      inbox: "Boîte",
      pomodoro: "Pomodoro",
      chat: "Discussion",
      roadmaps: "Parcours",
      materials: "Supports",
      calendarAlarm: "Calendrier & Alarme",
      tools: "Outils",
      settings: "Paramètres",
      nextUp: "À venir",
      repeatedQuestions: "Questions fréquentes des étudiants",
      unifiedReplyAll: "Réponse unifiée à tous",
      summarizeEmail: "Résumer l'e-mail",
      smartReply: "Réponse intelligente",
      extractDates: "Ajouter les dates au calendrier",
      sendReply: "Envoyer",
      backToInbox: "Retour",
      all: "Tous",
      signOut: "Déconnexion",
      urgentEmailAlert: "E-mail important sans réponse",
      tapToOpenReply: "Appuyez pour répondre",
      repliedReviewed: "Répondus & Lus",
      menu: "Menu",
      exitPomodoro: "Quitter Pomodoro",
      studyFocus: "Concentration",
      breakTime: "Pause",
      allCaughtUp: "Tout est à jour",
      compose: "Nouveau"
    },
    es: {
      studentRole: "Estudiante",
      instructorRole: "Docente",
      signInMicrosoft: "Continuar con Microsoft / Outlook",
      signInAcademic: "Acceso Académico",
      demoAccount: "Cuenta Demo",
      installApp: "Instalar aplicación",
      darkMode: "Modo oscuro",
      language: "Idioma",
      home: "Inicio",
      inbox: "Bandeja",
      pomodoro: "Pomodoro",
      chat: "Chat",
      roadmaps: "Planes",
      materials: "Materiales",
      calendarAlarm: "Calendario y Alarma",
      tools: "Herramientas",
      settings: "Ajustes",
      nextUp: "Próximo",
      repeatedQuestions: "Preguntas frecuentes",
      unifiedReplyAll: "Respuesta unificada",
      summarizeEmail: "Resumir correo",
      smartReply: "Respuesta inteligente",
      extractDates: "Extraer fechas al calendario",
      sendReply: "Enviar",
      backToInbox: "Volver",
      all: "Todos",
      signOut: "Cerrar sesión",
      urgentEmailAlert: "Correo importante sin responder",
      tapToOpenReply: "Toca para responder",
      repliedReviewed: "Respondidos y Revisados",
      menu: "Menú",
      exitPomodoro: "Salir de Pomodoro",
      studyFocus: "Enfoque de estudio",
      breakTime: "Descanso",
      allCaughtUp: "Todo al día",
      compose: "Redactar"
    },
    tr: {
      studentRole: "Öğrenci",
      instructorRole: "Öğretim Üyesi",
      signInMicrosoft: "Microsoft / Outlook ile Giriş",
      signInAcademic: "Akademik Giriş",
      demoAccount: "Demo Hesap",
      installApp: "Uygulamayı Yükle",
      darkMode: "Karanlık Mod",
      language: "Dil",
      home: "Ana Sayfa",
      inbox: "Gelen Kutusu",
      pomodoro: "Pomodoro",
      chat: "Sohbet",
      roadmaps: "Müfredat",
      materials: "Materyaller",
      calendarAlarm: "Takvim ve Alarm",
      tools: "Araçlar",
      settings: "Ayarlar",
      nextUp: "Sıradaki",
      repeatedQuestions: "En Sık Sorulan Öğrenci Soruları",
      unifiedReplyAll: "Tümüne Ortak Yanıt",
      summarizeEmail: "E-postayı Özetle",
      smartReply: "Akıllı Yanıt",
      extractDates: "Tarihleri Takvime Aktar",
      sendReply: "Gönder",
      backToInbox: "Geri",
      all: "Tümü",
      signOut: "Çıkış Yap",
      urgentEmailAlert: "Yanıtlanmamış Önemli E-posta",
      tapToOpenReply: "Yanıtlamak için dokunun",
      repliedReviewed: "Yanıtlananlar ve İncelenenler",
      menu: "Menü",
      exitPomodoro: "Pomodoro'dan Çık",
      studyFocus: "Çalışma Odağı",
      breakTime: "Mola Zamanı",
      allCaughtUp: "Bekleyen soru yok",
      compose: "Yeni"
    }
  };

  function getStrings(langCode) {
    return Object.assign({}, UI_STRINGS.en, UI_STRINGS[langCode] || {});
  }

  function isRtl(langCode) {
    const found = LANGUAGES.find((l) => l.code === langCode);
    return !!(found && found.rtl);
  }

  const COURSE_STATUSES = [
    { key: "completed", labelEn: "Completed", labelAr: "مكتملة", shortEn: "Done", shortAr: "مكتملة", color: "#1F6E5A" },
    { key: "in_progress", labelEn: "In Progress", labelAr: "قيد الدراسة", shortEn: "Now", shortAr: "جارية", color: "#1E4E8C" },
    { key: "next_term", labelEn: "Next Term", labelAr: "الترم القادم", shortEn: "Next", shortAr: "القادم", color: "#C86432" },
    { key: "not_started", labelEn: "Not Started", labelAr: "لم تبدأ بعد", shortEn: "New", shortAr: "جديدة", color: "#64748B" }
  ];

  const PRIORITY_META = {
    CRITICAL: { labelEn: "Critical", labelAr: "حرج", badgeClass: "badge-critical", rank: 0 },
    HIGH: { labelEn: "High", labelAr: "مرتفع", badgeClass: "badge-high", rank: 1 },
    NORMAL: { labelEn: "Normal", labelAr: "عادي", badgeClass: "badge-normal", rank: 2 },
    LOW: { labelEn: "Low", labelAr: "منخفض", badgeClass: "badge-low", rank: 3 }
  };

  function c(code, title, credits, stage, category, prerequisites) {
    return {
      code,
      title,
      credits,
      stage,
      category,
      prerequisites: prerequisites || [],
      description: `AOU required curriculum course (${code}) in stage ${stage}.`
    };
  }

  const MAJORS = [
    {
      id: "cs",
      nameEn: "Computer Science",
      nameAr: "علوم الحاسوب",
      facultyEn: "Faculty of Computer Studies",
      courses: [
        c("GR101", "Self-Learning Skills", 3, 1, "UNIVERSITY_REQ"),
        c("AR111", "Arabic Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("EL111", "English Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("TU170", "Learning Online", 3, 1, "UNIVERSITY_REQ"),
        c("MT129", "Calculus and Probability", 4, 1, "FACULTY_REQ"),
        c("AR112", "Arabic Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["AR111"]),
        c("EL112", "English Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["EL111"]),
        c("MT131", "Discrete Mathematics", 4, 2, "FACULTY_REQ", ["MT129"]),
        c("MT132", "Linear Algebra", 4, 2, "FACULTY_REQ", ["MT129"]),
        c("TM103", "Computer Organization & Architecture", 4, 2, "FACULTY_REQ", ["TU170"]),
        c("TM105", "Introduction to Programming", 4, 3, "FACULTY_REQ", ["EL111", "TU170"]),
        c("TM111", "Introduction to Computing & IT I", 8, 3, "MAJOR_CORE", ["EL111"]),
        c("TM112", "Introduction to Computing & IT II", 8, 3, "MAJOR_CORE", ["TM111"]),
        c("M251", "Object-Oriented Programming Using Java", 8, 4, "MAJOR_CORE", ["TM105"]),
        c("M269", "Algorithms, Data Structures & Computability", 8, 4, "MAJOR_CORE", ["TM105", "MT131"]),
        c("T227", "Change, Strategy & Projects at Work", 8, 4, "MAJOR_CORE", ["TM112"]),
        c("TM240", "Computer Graphics & Multimedia", 4, 4, "MAJOR_CORE", ["MT132", "TM105"]),
        c("TM298", "Operating Systems", 4, 4, "MAJOR_CORE", ["TM103", "M269"]),
        c("TM351", "Data Management & Analysis", 8, 5, "MAJOR_CORE", ["M251"]),
        c("TM354", "Software Engineering", 8, 5, "MAJOR_CORE", ["M251"]),
        c("TM366", "Artificial Intelligence", 8, 5, "MAJOR_CORE", ["M269"]),
        c("TM471-A", "Graduation Project Part I", 4, 6, "ELECTIVE_PROJECT", ["TM351", "TM354"]),
        c("TM471-B", "Graduation Project Part II", 4, 6, "ELECTIVE_PROJECT", ["TM471-A"]),
        c("CAS400", "Computing Capstone Assessment", 0, 6, "ELECTIVE_PROJECT", ["TM471-A"])
      ]
    },
    {
      id: "itc",
      nameEn: "Information Technology & Computing (ITC)",
      nameAr: "تقنية المعلومات والحوسبة",
      facultyEn: "Faculty of Computer Studies",
      courses: [
        c("GR101", "Self-Learning Skills", 3, 1, "UNIVERSITY_REQ"),
        c("AR111", "Arabic Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("EL111", "English Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("TU170", "Learning Online", 3, 1, "UNIVERSITY_REQ"),
        c("MT129", "Calculus and Probability", 4, 1, "FACULTY_REQ"),
        c("AR112", "Arabic Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["AR111"]),
        c("EL112", "English Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["EL111"]),
        c("MT131", "Discrete Mathematics", 4, 2, "FACULTY_REQ", ["MT129"]),
        c("MT132", "Linear Algebra", 4, 2, "FACULTY_REQ", ["MT129"]),
        c("TM103", "Computer Organization & Architecture", 4, 2, "FACULTY_REQ", ["TU170"]),
        c("TM105", "Introduction to Programming", 4, 3, "FACULTY_REQ", ["EL111"]),
        c("TM111", "Introduction to Computing & IT I", 8, 3, "MAJOR_CORE", ["EL111"]),
        c("TM112", "Introduction to Computing & IT II", 8, 3, "MAJOR_CORE", ["TM111"]),
        c("M251", "Object-Oriented Programming Using Java", 8, 4, "MAJOR_CORE", ["TM105"]),
        c("M269", "Algorithms & Data Structures", 8, 4, "MAJOR_CORE", ["TM105", "MT131"]),
        c("T227", "Change, Strategy & Projects at Work", 8, 4, "MAJOR_CORE", ["TM112"]),
        c("TM240", "Computer Graphics & Multimedia", 4, 4, "MAJOR_CORE", ["TM105"]),
        c("TM298", "Operating Systems", 4, 4, "MAJOR_CORE", ["TM103"]),
        c("TM351", "Data Management & Analysis", 8, 5, "MAJOR_CORE", ["M251"]),
        c("TM352", "Web, Mobile & Cloud Technologies", 8, 5, "MAJOR_CORE", ["M251"]),
        c("TM354", "Software Engineering", 8, 5, "MAJOR_CORE", ["M251"]),
        c("TM366", "Artificial Intelligence", 8, 5, "MAJOR_CORE", ["M269"]),
        c("TM471-A", "Graduation Project Part I", 4, 6, "ELECTIVE_PROJECT", ["TM351", "TM354"]),
        c("TM471-B", "Graduation Project Part II", 4, 6, "ELECTIVE_PROJECT", ["TM471-A"]),
        c("CAS400", "ITC Capstone Assessment", 0, 6, "ELECTIVE_PROJECT", ["TM471-A"])
      ]
    },
    {
      id: "net_sec",
      nameEn: "Networking & Security",
      nameAr: "الشبكات والأمن السيبراني",
      facultyEn: "Faculty of Computer Studies",
      courses: [
        c("GR101", "Self-Learning Skills", 3, 1, "UNIVERSITY_REQ"),
        c("AR111", "Arabic Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("EL111", "English Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("TU170", "Learning Online", 3, 1, "UNIVERSITY_REQ"),
        c("AR112", "Arabic Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["AR111"]),
        c("EL112", "English Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["EL111"]),
        c("MT129", "Calculus and Probability", 4, 2, "FACULTY_REQ"),
        c("MT131", "Discrete Mathematics", 4, 2, "FACULTY_REQ", ["MT129"]),
        c("MT132", "Linear Algebra", 4, 2, "FACULTY_REQ", ["MT129"]),
        c("TM111", "Introduction to Computing & IT I", 8, 3, "MAJOR_CORE", ["EL111"]),
        c("TM105", "Introduction to Programming", 4, 3, "FACULTY_REQ", ["EL111"]),
        c("TM112", "Introduction to Computing & IT II", 8, 3, "MAJOR_CORE", ["TM111"]),
        c("T216A", "Cisco Networking (CCNA) Part I", 8, 4, "MAJOR_CORE", ["TM112"]),
        c("M251", "Object-Oriented Programming", 8, 4, "MAJOR_CORE", ["TM105"]),
        c("T227", "Change, Strategy & Projects", 8, 4, "MAJOR_CORE", ["TM112"]),
        c("T216B", "Cisco Networking (CCNA) Part II", 8, 4, "MAJOR_CORE", ["T216A"]),
        c("TM103", "Computer Organization & Architecture", 4, 4, "FACULTY_REQ", ["TU170"]),
        c("T316", "Advanced Networking & Routing", 8, 5, "MAJOR_CORE", ["T216B"]),
        c("TM352", "Web, Mobile & Cloud Technologies", 8, 5, "MAJOR_CORE", ["M251"]),
        c("T318", "Network Security & Ethical Hacking", 8, 5, "MAJOR_CORE", ["T216B"]),
        c("TM298", "Operating Systems", 4, 5, "MAJOR_CORE", ["TM103"]),
        c("TM471-A", "Graduation Project Part I", 4, 6, "ELECTIVE_PROJECT", ["T316", "T318"]),
        c("TM471-B", "Graduation Project Part II", 4, 6, "ELECTIVE_PROJECT", ["TM471-A"]),
        c("CAS400", "Networking Capstone Assessment", 0, 6, "ELECTIVE_PROJECT", ["TM471-A"])
      ]
    },
    {
      id: "web_dev",
      nameEn: "Web Development",
      nameAr: "تطوير الويب",
      facultyEn: "Faculty of Computer Studies",
      courses: [
        c("AR111", "Arabic Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("EL111", "English Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("TU170", "Learning Online", 3, 1, "UNIVERSITY_REQ"),
        c("GR101", "Self-Learning Skills", 3, 1, "UNIVERSITY_REQ"),
        c("MT129", "Calculus and Probability", 4, 1, "FACULTY_REQ"),
        c("AR112", "Arabic Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["AR111"]),
        c("EL112", "English Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["EL111"]),
        c("MT131", "Discrete Mathematics", 4, 2, "FACULTY_REQ", ["MT129"]),
        c("MT132", "Linear Algebra", 4, 2, "FACULTY_REQ", ["MT129"]),
        c("TM105", "Introduction to Programming", 4, 3, "FACULTY_REQ", ["EL111"]),
        c("TM103", "Computer Organization", 4, 3, "FACULTY_REQ", ["TU170"]),
        c("TM111", "Introduction to Computing & IT I", 8, 3, "MAJOR_CORE", ["EL111"]),
        c("TM112", "Introduction to Computing & IT II", 8, 3, "MAJOR_CORE", ["TM111"]),
        c("M251", "Object-Oriented Programming", 8, 4, "MAJOR_CORE", ["TM105"]),
        c("M269", "Algorithms & Data Structures", 8, 4, "MAJOR_CORE", ["TM105", "MT131"]),
        c("T227", "Change, Strategy & Projects", 8, 4, "MAJOR_CORE", ["TM112"]),
        c("TM354", "Software Engineering", 8, 5, "MAJOR_CORE", ["M251"]),
        c("TT284", "Web Technologies", 8, 5, "MAJOR_CORE", ["TM112"]),
        c("TM352", "Web, Mobile & Cloud Technologies", 8, 5, "MAJOR_CORE", ["TT284", "M251"]),
        c("TM356", "Interaction Design & UX", 8, 5, "MAJOR_CORE", ["TT284"]),
        c("TM298", "Operating Systems", 4, 5, "MAJOR_CORE", ["TM103"]),
        c("TM471-A", "Graduation Project Part I", 4, 6, "ELECTIVE_PROJECT", ["TM352", "TM354"]),
        c("TM471-B", "Graduation Project Part II", 4, 6, "ELECTIVE_PROJECT", ["TM471-A"]),
        c("CAS400", "Web Capstone Assessment", 0, 6, "ELECTIVE_PROJECT", ["TM471-A"])
      ]
    },
    {
      id: "comp_bus",
      nameEn: "Computing with Business",
      nameAr: "الحوسبة مع إدارة الأعمال",
      facultyEn: "Faculty of Computer Studies",
      courses: [
        c("AR111", "Arabic Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("EL111", "English Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("TU170", "Learning Online", 3, 1, "UNIVERSITY_REQ"),
        c("GR101", "Self-Learning Skills", 3, 1, "UNIVERSITY_REQ"),
        c("MT129", "Calculus and Probability", 4, 1, "FACULTY_REQ"),
        c("AR112", "Arabic Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["AR111"]),
        c("EL112", "English Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["EL111"]),
        c("MT131", "Discrete Mathematics", 4, 2, "FACULTY_REQ", ["MT129"]),
        c("MT132", "Linear Algebra", 4, 2, "FACULTY_REQ", ["MT129"]),
        c("TM105", "Introduction to Programming", 4, 3, "FACULTY_REQ", ["EL111"]),
        c("BUS110", "Introduction to Business", 8, 3, "FACULTY_REQ", ["EL111"]),
        c("TM111", "Introduction to Computing & IT I", 8, 3, "MAJOR_CORE", ["EL111"]),
        c("B207A", "Shaping Business Opportunities I", 8, 4, "MAJOR_CORE", ["BUS110"]),
        c("TM103", "Computer Organization", 4, 4, "FACULTY_REQ", ["TU170"]),
        c("M251", "Object-Oriented Programming", 8, 4, "MAJOR_CORE", ["TM105"]),
        c("B207B", "Shaping Business Opportunities II", 8, 4, "MAJOR_CORE", ["B207A"]),
        c("M269", "Algorithms & Data Structures", 8, 5, "MAJOR_CORE", ["TM105", "MT131"]),
        c("TM352", "Web, Mobile & Cloud Technologies", 8, 5, "MAJOR_CORE", ["M251"]),
        c("BUS310", "Strategic Management", 8, 5, "MAJOR_CORE", ["B207B"]),
        c("TM351", "Data Management & Analysis", 8, 5, "MAJOR_CORE", ["M251"]),
        c("TM298", "Operating Systems", 4, 5, "MAJOR_CORE", ["TM103"]),
        c("TM471-A", "Graduation Project Part I", 4, 6, "ELECTIVE_PROJECT", ["TM351", "BUS310"]),
        c("TM471-B", "Graduation Project Part II", 4, 6, "ELECTIVE_PROJECT", ["TM471-A"]),
        c("CAS400", "Computing with Business Capstone", 0, 6, "ELECTIVE_PROJECT", ["TM471-A"])
      ]
    },
    {
      id: "bus_acct_en",
      nameEn: "Business Admin - Accounting (English)",
      nameAr: "إدارة الأعمال - المحاسبة (إنجليزي)",
      facultyEn: "Faculty of Business Studies",
      courses: [
        c("EL111", "English Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("AR111", "Arabic Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("GR101", "Self-Learning Skills", 3, 1, "UNIVERSITY_REQ"),
        c("TU170", "Learning Online", 3, 1, "UNIVERSITY_REQ"),
        c("EL112", "English Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["EL111"]),
        c("AR112", "Arabic Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["AR111"]),
        c("LB170", "Communication Skills for Business", 4, 2, "FACULTY_REQ", ["EL111"]),
        c("BUS110", "Introduction to Business", 8, 2, "FACULTY_REQ", ["EL111"]),
        c("STAT201", "Business Statistics", 3, 3, "FACULTY_REQ", ["TU170"]),
        c("B124", "Fundamentals of Accounting", 4, 3, "FACULTY_REQ", ["EL112"]),
        c("ACCT201", "Financial Accounting I", 3, 3, "MAJOR_CORE", ["B124"]),
        c("ACCT202", "Financial Accounting II", 3, 3, "MAJOR_CORE", ["ACCT201"]),
        c("MGT201", "Principles of Management", 3, 3, "FACULTY_REQ", ["BUS110"]),
        c("ECON201", "Microeconomics", 3, 4, "FACULTY_REQ", ["BUS110"]),
        c("ECON202", "Macroeconomics", 3, 4, "FACULTY_REQ", ["ECON201"]),
        c("ACCT250", "Cost Accounting", 3, 4, "MAJOR_CORE", ["ACCT202"]),
        c("ACCT301", "Intermediate Accounting I", 3, 4, "MAJOR_CORE", ["ACCT202"]),
        c("ACCT305", "Intermediate Accounting II", 3, 5, "MAJOR_CORE", ["ACCT301"]),
        c("ACCT306", "Managerial Accounting", 3, 5, "MAJOR_CORE", ["ACCT250"]),
        c("FIN301", "Corporate Finance", 3, 5, "FACULTY_REQ", ["ACCT202", "STAT201"]),
        c("ACCT320", "Accounting Information Systems", 3, 5, "MAJOR_CORE", ["ACCT202"]),
        c("ACCT322", "Auditing & Assurance", 3, 5, "MAJOR_CORE", ["ACCT301"]),
        c("ACCT340", "Taxation Accounting", 3, 6, "MAJOR_CORE", ["ACCT301"]),
        c("ACCT401", "Advanced Financial Accounting", 3, 6, "MAJOR_CORE", ["ACCT305"]),
        c("ACCT420", "International Accounting Standards", 3, 6, "MAJOR_CORE", ["ACCT305"]),
        c("BUS490", "Graduation Project in Accounting", 6, 6, "ELECTIVE_PROJECT", ["ACCT401"])
      ]
    },
    {
      id: "bus_acct_ar",
      nameEn: "Business Admin - Accounting (Arabic)",
      nameAr: "إدارة الأعمال - المحاسبة (عربي)",
      facultyEn: "Faculty of Business Studies",
      courses: [
        c("EL111", "English Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("AR111", "Arabic Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("GR101", "Self-Learning Skills", 3, 1, "UNIVERSITY_REQ"),
        c("TU170", "Learning Online", 3, 1, "UNIVERSITY_REQ"),
        c("EL112", "English Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["EL111"]),
        c("AR112", "Arabic Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["AR111"]),
        c("LB170", "Business Communication Skills", 4, 2, "FACULTY_REQ", ["EL111"]),
        c("BUS110", "Introduction to Business", 8, 2, "FACULTY_REQ", ["AR111"]),
        c("MATH201", "Mathematics for Business", 3, 3, "FACULTY_REQ"),
        c("STAT201", "Business Statistics", 3, 3, "FACULTY_REQ", ["MATH201"]),
        c("ACCT201", "Financial Accounting I", 3, 3, "MAJOR_CORE", ["BUS110"]),
        c("ACCT202", "Financial Accounting II", 3, 3, "MAJOR_CORE", ["ACCT201"]),
        c("MGT201", "Principles of Management", 3, 3, "FACULTY_REQ", ["BUS110"]),
        c("ECON201", "Microeconomics", 3, 4, "FACULTY_REQ", ["BUS110"]),
        c("ECON202", "Macroeconomics", 3, 4, "FACULTY_REQ", ["ECON201"]),
        c("ACCT250", "Cost Accounting", 3, 4, "MAJOR_CORE", ["ACCT202"]),
        c("ACCT301", "Intermediate Accounting I", 3, 4, "MAJOR_CORE", ["ACCT202"]),
        c("ACCT305", "Intermediate Accounting II", 3, 5, "MAJOR_CORE", ["ACCT301"]),
        c("ACCT306", "Managerial Accounting", 3, 5, "MAJOR_CORE", ["ACCT250"]),
        c("ACCT307", "Governmental & Non-Profit Accounting", 3, 5, "MAJOR_CORE", ["ACCT202"]),
        c("FIN301", "Financial Management", 3, 5, "FACULTY_REQ", ["ACCT202"]),
        c("ACCT322", "Auditing Principles", 3, 5, "MAJOR_CORE", ["ACCT301"]),
        c("ACCT340", "Zakat & Tax Accounting", 3, 6, "MAJOR_CORE", ["ACCT301"]),
        c("ACCT350", "Islamic Financial Accounting", 3, 6, "MAJOR_CORE", ["ACCT301"]),
        c("ACCT401", "Advanced Accounting", 3, 6, "MAJOR_CORE", ["ACCT305"]),
        c("ACCT420", "International Financial Reporting", 3, 6, "MAJOR_CORE", ["ACCT305"]),
        c("BUS490", "Graduation Project in Accounting", 6, 6, "ELECTIVE_PROJECT", ["ACCT401"])
      ]
    },
    {
      id: "bus_mkt",
      nameEn: "Business Admin - Marketing",
      nameAr: "إدارة الأعمال - التسويق",
      facultyEn: "Faculty of Business Studies",
      courses: [
        c("EL111", "English Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("AR111", "Arabic Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("GR101", "Self-Learning Skills", 3, 1, "UNIVERSITY_REQ"),
        c("TU170", "Learning Online", 3, 1, "UNIVERSITY_REQ"),
        c("EL112", "English Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["EL111"]),
        c("AR112", "Arabic Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["AR111"]),
        c("LB170", "Communication Skills for Business", 4, 2, "FACULTY_REQ", ["EL111"]),
        c("BUS110", "Introduction to Business", 8, 2, "FACULTY_REQ", ["EL111"]),
        c("STAT201", "Business Statistics", 3, 3, "FACULTY_REQ"),
        c("ECON201", "Microeconomics", 3, 3, "FACULTY_REQ", ["BUS110"]),
        c("ECON202", "Macroeconomics", 3, 3, "FACULTY_REQ", ["ECON201"]),
        c("MGT201", "Principles of Management", 3, 3, "FACULTY_REQ", ["BUS110"]),
        c("MKT201", "Principles of Marketing", 3, 3, "MAJOR_CORE", ["BUS110"]),
        c("ACCT201", "Financial Accounting I", 3, 4, "FACULTY_REQ", ["BUS110"]),
        c("MGT202", "Organizational Behavior", 3, 4, "FACULTY_REQ", ["MGT201"]),
        c("MKT205", "Consumer Behavior", 3, 4, "MAJOR_CORE", ["MKT201"]),
        c("FIN301", "Corporate Finance", 3, 4, "FACULTY_REQ", ["ACCT201"]),
        c("MGT301", "Operations Management", 3, 5, "FACULTY_REQ", ["MGT201", "STAT201"]),
        c("MKT302", "Marketing Research", 3, 5, "MAJOR_CORE", ["MKT201", "STAT201"]),
        c("MKT305", "Integrated Marketing Communications", 3, 5, "MAJOR_CORE", ["MKT205"]),
        c("MKT310", "Digital & Social Media Marketing", 3, 5, "MAJOR_CORE", ["MKT201"]),
        c("MKT401", "Services Marketing", 3, 6, "MAJOR_CORE", ["MKT302"]),
        c("MKT405", "Global Marketing Strategy", 3, 6, "MAJOR_CORE", ["MKT305"]),
        c("BUS310", "Strategic Management", 8, 6, "FACULTY_REQ", ["MGT301"]),
        c("BUS490", "Marketing Graduation Project", 6, 6, "ELECTIVE_PROJECT", ["MKT401", "BUS310"])
      ]
    },
    {
      id: "bus_sys",
      nameEn: "Business Admin - Systems / Management",
      nameAr: "إدارة الأعمال - النظم والإدارة",
      facultyEn: "Faculty of Business Studies",
      courses: [
        c("EL111", "English Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("AR111", "Arabic Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("GR101", "Self-Learning Skills", 3, 1, "UNIVERSITY_REQ"),
        c("TU170", "Learning Online", 3, 1, "UNIVERSITY_REQ"),
        c("EL112", "English Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["EL111"]),
        c("AR112", "Arabic Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["AR111"]),
        c("LB170", "Communication Skills for Business", 4, 2, "FACULTY_REQ", ["EL111"]),
        c("BUS110", "Introduction to Business", 8, 2, "FACULTY_REQ", ["EL111"]),
        c("STAT201", "Business Statistics", 3, 3, "FACULTY_REQ"),
        c("ECON201", "Microeconomics", 3, 3, "FACULTY_REQ", ["BUS110"]),
        c("ECON202", "Macroeconomics", 3, 3, "FACULTY_REQ", ["ECON201"]),
        c("MGT201", "Principles of Management", 3, 3, "MAJOR_CORE", ["BUS110"]),
        c("MIS201", "Management Information Systems", 3, 4, "MAJOR_CORE", ["TU170", "MGT201"]),
        c("ACCT201", "Financial Accounting I", 3, 4, "FACULTY_REQ", ["BUS110"]),
        c("MGT202", "Organizational Behavior", 3, 4, "MAJOR_CORE", ["MGT201"]),
        c("FIN301", "Corporate Finance", 3, 4, "FACULTY_REQ", ["ACCT201"]),
        c("MGT301", "Operations Management", 3, 5, "MAJOR_CORE", ["MGT201", "STAT201"]),
        c("MIS301", "Enterprise Systems & Analytics", 3, 5, "MAJOR_CORE", ["MIS201"]),
        c("MGT305", "Human Resource Management", 3, 5, "MAJOR_CORE", ["MGT202"]),
        c("BUS310", "Strategic Management", 8, 5, "MAJOR_CORE", ["MGT301"]),
        c("MGT401", "Leadership & Change Management", 3, 6, "MAJOR_CORE", ["MGT305"]),
        c("MGT405", "Project & Quality Management", 3, 6, "MAJOR_CORE", ["MGT301"]),
        c("BUS490", "Management Graduation Project", 6, 6, "ELECTIVE_PROJECT", ["BUS310"])
      ]
    },
    {
      id: "ell",
      nameEn: "English Language & Literature",
      nameAr: "اللغة الإنجليزية وآدابها",
      facultyEn: "Faculty of Language Studies",
      courses: [
        c("AR111", "Arabic Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("EL111", "English Communication Skills I", 3, 1, "UNIVERSITY_REQ"),
        c("TU170", "Learning Online", 3, 1, "UNIVERSITY_REQ"),
        c("GR101", "Self-Learning Skills", 3, 1, "UNIVERSITY_REQ"),
        c("EL120", "English Phonetics & Phonology", 3, 2, "MAJOR_CORE", ["EL111"]),
        c("EL121", "Short Story & Essay Writing", 3, 2, "MAJOR_CORE", ["EL111"]),
        c("AR112", "Arabic Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["AR111"]),
        c("EL112", "English Communication Skills II", 3, 2, "UNIVERSITY_REQ", ["EL111"]),
        c("EL210", "Syntax & Morphology", 3, 3, "MAJOR_CORE", ["EL120"]),
        c("EL211", "Semantics & Pragmatics", 3, 3, "MAJOR_CORE", ["EL120"]),
        c("EL119", "Academic Oral Communication", 3, 3, "FACULTY_REQ", ["EL112"]),
        c("EL118", "Academic Reading & Research", 3, 3, "FACULTY_REQ", ["EL112"]),
        c("EL222", "History of English Literature", 3, 4, "MAJOR_CORE", ["EL121"]),
        c("AA100A", "The Arts Past and Present I", 8, 4, "FACULTY_REQ", ["EL112"]),
        c("AA100B", "The Arts Past and Present II", 8, 4, "FACULTY_REQ", ["AA100A"]),
        c("U214A", "Worlds of English I", 8, 5, "MAJOR_CORE", ["EL210"]),
        c("U214B", "Worlds of English II", 8, 5, "MAJOR_CORE", ["U214A"]),
        c("A230A", "Reading and Studying Literature I", 8, 5, "MAJOR_CORE", ["EL222"]),
        c("A230B", "Reading and Studying Literature II", 8, 5, "MAJOR_CORE", ["A230A"]),
        c("E304A", "Exploring English Grammar I", 8, 6, "MAJOR_CORE", ["U214B"]),
        c("E304B", "Exploring English Grammar II", 8, 6, "MAJOR_CORE", ["E304A"]),
        c("EL400", "Graduation Research Project in ELL", 6, 6, "ELECTIVE_PROJECT", ["A230B", "E304A"])
      ]
    }
  ];

  function getSampleEmails(role) {
    const now = Date.now();
    if (role === "student") {
      return [
        {
          id: "stu_mail_1",
          senderName: "Dr. Tariq Al-Mansoor",
          senderEmail: "t.almansoor@aou.edu.sa",
          recipientEmail: "student@aou.edu.sa",
          subject: "TM351 TMA Part 2 Submission Deadline Extended + Rubric Update",
          fullBody: `Dear TM351 Students,\n\nPlease note that the submission deadline for TM351 Tutor-Marked Assignment (TMA) Part 2 has been extended by 48 hours to allow thorough testing of your MongoDB and PostgreSQL schemas.\n\nKey requirements to verify before uploading to the AOU LMS:\n- Include your ERD diagram in PDF format alongside SQL DDL scripts.\n- Document every indexing decision for the analytical query benchmark (Question 3).\n- Ensure your student ID and AOU branch appear on the cover page.\n\nImportant Dates:\n- Updated TMA Deadline: November 24, 2026 at 11:55 PM\n- Online Revision Clinic: November 22, 2026 at 6:00 PM\n\nSubmission Portal:\nhttps://lms.aou.edu.sa/mod/assign/view.php?id=35102\n\nBest regards,\nDr. Tariq Al-Mansoor\nFaculty of Computer Studies — AOU`,
          receivedAtLabel: "Today, 10:15 AM",
          timestampMillis: now - 3600000,
          priority: "CRITICAL",
          courseTag: "TM351",
          unread: true,
          viewed: false,
          replied: false
        },
        {
          id: "stu_mail_2",
          senderName: "AOU Examination Unit",
          senderEmail: "exams@aou.edu.sa",
          recipientEmail: "student@aou.edu.sa",
          subject: "Final Midterm Seating & Hall Allocation for M269",
          fullBody: `Dear Student,\n\nYour examination hall and seat number for M269 (Algorithms, Data Structures & Computability) have been finalized for the Fall 2026 term.\n\nExam Details:\n- Date & Time: November 26, 2026 at 4:00 PM\n- Hall: Building B, Hall 204 (Seat 18)\n- Duration: 120 Minutes\n\nReminder:\n1. Arrive at least 20 minutes prior to the exam start time.\n2. Bring your valid University Student ID card.\n3. Non-programmable calculators are permitted only if listed on the exam paper.\n\nWishing you success,\nAOU Examination & Assessment Unit`,
          receivedAtLabel: "Yesterday, 4:40 PM",
          timestampMillis: now - 64800000,
          priority: "HIGH",
          courseTag: "M269",
          unread: true,
          viewed: false,
          replied: false
        },
        {
          id: "stu_mail_3",
          senderName: "Prof. Nadia Haddad",
          senderEmail: "n.haddad@aou.edu.sa",
          recipientEmail: "student@aou.edu.sa",
          subject: "TM354 Weekly Tutorial Slides & UML Case Study",
          fullBody: `Hello everyone,\n\nThank you for your active participation in this week's TM354 Software Engineering tutorial.\n\nI have uploaded the updated Lecture 6 slide deck covering:\n- Sequence diagrams and state machine transitions\n- Component interfaces and layered architectural patterns\n- Annotated UML Hospital Management case study\n\nPlease review Section 4 before next Tuesday's live session so we can walk through the class diagram refactoring together.\n\nWarm regards,\nProf. Nadia Haddad`,
          receivedAtLabel: "Nov 18, 2:10 PM",
          timestampMillis: now - 150000000,
          priority: "NORMAL",
          courseTag: "TM354",
          unread: false,
          viewed: true,
          replied: false
        },
        {
          id: "stu_mail_4",
          senderName: "AOU Student Affairs",
          senderEmail: "affairs@aou.edu.sa",
          recipientEmail: "student@aou.edu.sa",
          subject: "Campus Tech Career Fair & Alumni Networking Day",
          fullBody: `Greetings AOU Students,\n\nYou are invited to attend the annual AOU Tech Career & Internship Fair next month.\n\nHighlights:\n- CV clinic with industry engineers\n- Summer internship booths for FCS and Business undergraduates\n- Alumni panel on transitioning from TM471 capstone projects to full-time roles\n\nRegistration is optional and free for all enrolled students.\n\nBest wishes,\nAOU Student Affairs Office`,
          receivedAtLabel: "Nov 16, 9:00 AM",
          timestampMillis: now - 345600000,
          priority: "LOW",
          courseTag: "CAMPUS",
          unread: false,
          viewed: true,
          replied: false
        }
      ];
    }
    return [
      {
        id: "ins_mail_1",
        senderName: "AOU Academic Deanery",
        senderEmail: "deanery.fcs@aou.edu.sa",
        recipientEmail: "faculty@aou.edu.sa",
        subject: "Urgent: TM351 & TM354 Midterm Grade Moderation Window",
        fullBody: `Dear Faculty Member,\n\nThe Faculty Assessment Board requests all section coordinators for TM351 and TM354 to finalize their Midterm Assessment (MTA) grade moderation sheets before the portal closes.\n\nAction Items:\n- Upload the signed moderation checklist on the Faculty SIS portal.\n- Flag any borderline script reviews for second-marker verification.\n- Confirm attendance logs for makeup exam candidates.\n\nKey Deadline:\n- Moderation Cut-off: November 25, 2026 at 5:00 PM\n\nKind regards,\nOffice of the Dean — Faculty of Computer Studies`,
        receivedAtLabel: "Today, 9:30 AM",
        timestampMillis: now - 3000000,
        priority: "CRITICAL",
        courseTag: "TM351",
        unread: true,
        viewed: false,
        replied: false
      },
      {
        id: "ins_mail_2",
        senderName: "Youssef Al-Kandari (Student)",
        senderEmail: "2341098@aou.edu.sa",
        recipientEmail: "faculty@aou.edu.sa",
        subject: "TM351 TMA Part 2: Question 3 PostgreSQL Index Format",
        fullBody: `Dear Doctor,\n\nI hope you are doing well. Several classmates and I in Section 04 had a quick question regarding Question 3 in the TM351 TMA Part 2 assignment.\n\nCould you clarify:\n1. Should we attach EXPLAIN ANALYZE execution screenshots inside the Word/PDF report, or also paste the raw query plan text?\n2. Is it acceptable to use composite B-Tree indexes alongside the primary key?\n\nThank you very much for your guidance.\n\nRespectfully,\nYoussef Al-Kandari\nID: 2341098 — Section 04`,
        receivedAtLabel: "Today, 8:45 AM",
        timestampMillis: now - 6500000,
        priority: "HIGH",
        courseTag: "TM351",
        unread: true,
        viewed: false,
        replied: false
      },
      {
        id: "ins_mail_3",
        senderName: "Lina Mansour (Student)",
        senderEmail: "2341152@aou.edu.sa",
        recipientEmail: "faculty@aou.edu.sa",
        subject: "M269 Exam Scope: Graph Traversal & Dynamic Programming",
        fullBody: `Hello Doctor,\n\nCould you please confirm whether the upcoming M269 exam on November 26 covers the formal proof of Dijkstra's shortest-path algorithm, or only tracing and time-complexity analysis?\n\nAlso, will the formula sheet for recurrence relations be provided on the exam paper?\n\nThank you,\nLina Mansour\nID: 2341152`,
        receivedAtLabel: "Yesterday, 6:20 PM",
        timestampMillis: now - 54000000,
        priority: "NORMAL",
        courseTag: "M269",
        unread: true,
        viewed: false,
        replied: false
      },
      {
        id: "ins_mail_4",
        senderName: "AOU Library Services",
        senderEmail: "library@aou.edu.sa",
        recipientEmail: "faculty@aou.edu.sa",
        subject: "Spring 2027 Textbook & E-Resource Adoption Requests",
        fullBody: `Dear Colleagues,\n\nAOU E-Library is collecting reading list updates for Spring 2027 courses. If you plan to add new ACM/IEEE digital references or O'Reilly texts to your course syllabus, please submit your request by December 10.\n\nThank you,\nAOU Central Library`,
        receivedAtLabel: "Nov 17, 11:05 AM",
        timestampMillis: now - 250000000,
        priority: "LOW",
        courseTag: "ADMIN",
        unread: false,
        viewed: true,
        replied: false
      }
    ];
  }

  const ARABIC_EMAIL_TRANSLATIONS = {
    stu_mail_1: {
      subject: "تمديد موعد تسليم الواجب TM351 (الجزء الثاني) وتحديث معايير التقييم",
      body: `طلاب مقرر TM351 الأعزاء،\n\nنحيطكم علماً بأنه تم تمديد موعد تسليم الجزء الثاني من الواجب الدراسي (TMA Part 2) لمدة 48 ساعة لإتاحة الوقت الكافي لاختبار قواعد بيانات MongoDB وPostgreSQL.\n\nالمتطلبات الأساسية قبل الرفع على نظام LMS:\n- إرفاق مخطط ERD بصيغة PDF مع نصوص أوامر SQL DDL.\n- توثيق جميع قرارات الفهرسة لتحليل أداء الاستعلام (السؤال الثالث).\n- التأكد من كتابة الرقم الجامعي وفرع الجامعة على صفحة الغلاف.\n\nالمواعيد المهمة:\n- الموعد النهائي المحدّث للتسليم: 24 نوفمبر 2026 الساعة 11:55 مساءً\n- جلسة المراجعة المباشرة: 22 نوفمبر 2026 الساعة 6:00 مساءً\n\nمع أطيب التحيات،\nد. طارق المنصور\nكلية دراسات الحاسوب — الجامعة العربية المفتوحة`
    },
    stu_mail_2: {
      subject: "توزيع قاعات ومقاعد الاختبار النصفي لمقرر M269",
      body: `عزيزي الطالب،\n\nتم اعتماد توزيع قاعة الاختبار ورقم المقعد لمقرر M269 (الخوارزميات وهياكل البيانات والحوسبة) للفصل الدراسي خريف 2026.\n\nتفاصيل الاختبار:\n- التاريخ والوقت: 26 نوفمبر 2026 الساعة 4:00 عصراً\n- القاعة: مبنى ب، قاعة 204 (مقعد 18)\n- المدة: 120 دقيقة\n\nتنبيهات مهمة:\n1. الحضور قبل موعد الاختبار بـ 20 دقيقة على الأقل.\n2. إحضار البطاقة الجامعية سارية المفعول.\n3. يُسمح بالآلة الحاسبة غير القابلة للبرمجة فقط إذا ذُكر ذلك في ورقة الاختبار.\n\nمع تمنياتنا لكم بالتوفيق،\nوحدة الاختبارات والتقييم — الجامعة العربية المفتوحة`
    },
    stu_mail_3: {
      subject: "شرائح المحاضرة السادسة ودراسة حالة UML لمقرر TM354",
      body: `السلام عليكم جميعاً،\n\nشكراً لمشاركتكم الفعّالة في درس مقرر TM354 هندسة البرمجيات لهذا الأسبوع.\n\nتم رفع شرائح المحاضرة السادسة المحدّثة وتشمل:\n- مخططات التسلسل وانتقالات الآلات ذات الحالات.\n- واجهات المكونات والأنماط المعمارية الطبقية.\n- نموذج مجال إدارة المستشفيات بلغة UML.\n\nيرجى مراجعة القسم الرابع قبل الجلسة المباشرة يوم الثلاثاء القادم لمناقشة تحسين مخطط الفئات معاً.\n\nمع خالص التقدير،\nأ.د. نادية حداد`
    },
    stu_mail_4: {
      subject: "معرض التوظيف التقني ويوم الخريجين بالجامعة",
      body: `طلاب الجامعة العربية المفتوحة الأعزاء،\n\nيسرّنا دعوتكم لحضور معرض التوظيف والتدريب التقني السنوي في الشهر القادم.\n\nأبرز الفعاليات:\n- ورشة مراجعة السيرة الذاتية مع مهندسين من القطاع التقني.\n- أجنحة التدريب الصيفي لطلبة كلية الحاسوب وإدارة الأعمال.\n- لقاء الخريجين حول الانتقال من مشاريع التخرج TM471 إلى سوق العمل.\n\nالتسجيل مجاني واختياري لجميع الطلبة المسجلين.\n\nمع أطيب التمنيات،\nشؤون الطلبة — الجامعة العربية المفتوحة`
    },
    ins_mail_1: {
      subject: "عاجل: فترة اعتماد ومراجعة درجات الاختبار النصفي لمقرري TM351 وTM354",
      body: `سعادة عضو هيئة التدريس المحترم،\n\nتطلب لجنة التقييم بالكلية من جميع منسقي الشعب لمقرري TM351 وTM354 اعتماد نماذج مراجعة درجات الاختبار النصفي (MTA) قبل إغلاق البوابة الإلكترونية.\n\nالإجراءات المطلوبة:\n- رفع قائمة التحقق الموقّعة للمراجعة عبر بوابة أعضاء هيئة التدريس SIS.\n- تمييز الأوراق الحدية لإتمام التدقيق من المصحح الثاني.\n- تأكيد سجلات الحضور للمتقدمين للاختبار البديل.\n\nالموعد النهائي:\n- إغلاق بوابة الاعتماد: 25 نوفمبر 2026 الساعة 5:00 مساءً\n\nمع وافر التقدير،\nمكتب عميد كلية دراسات الحاسوب`
    },
    ins_mail_2: {
      subject: "استفسار حول الواجب TM351 الجزء الثاني: صيغة فهارس PostgreSQL في السؤال الثالث",
      body: `سعادة الدكتور الفاضل،\nالسلام عليكم ورحمة الله وبركاته،\n\nلديّ أنا وعدد من زملائي في الشعبة 04 استفسار حول السؤال الثالث في الواجب TM351 الجزء الثاني.\n\nنرجو توضيح الآتي:\n1. هل نرفق لقطات شاشة لنتائج EXPLAIN ANALYZE داخل ملف Word/PDF أم نلصق نص خطة التنفيذ أيضاً؟\n2. هل يُسمح باستخدام الفهارس المركبة B-Tree إلى جانب المفتاح الأساسي؟\n\nشاكرين ومقدّرين توجيهكم الكريم.\n\nيوسف الكندري — الشعبة 04`
    },
    ins_mail_3: {
      subject: "نطاق اختبار مقرر M269: اجتياز الرسوم البيانية والبرمجة الديناميكية",
      body: `مرحباً دكتور،\n\nهل يشمل اختبار M269 القادم بتاريخ 26 نوفمبر الإثبات النظري لخوارزمية ديكسترا لأقصر مسار، أم يقتصر على التتبع وتحليل التعقيد الزمني؟\n\nوهل ستُرفق ورقة القوانين الخاصة بعلاقات التكرار في ورقة الاختبار؟\n\nشكراً لكم،\nلينا منصور`
    },
    ins_mail_4: {
      subject: "طلبات اعتماد الكتب والمصادر الإلكترونية لفصل ربيع 2027",
      body: `الزملاء الأفاضل،\n\nتجمع المكتبة الإلكترونية بالجامعة العربية المفتوحة قوائم القراءة المحدّثة لمقررات فصل ربيع 2027. في حال رغبتكم بإضافة مراجع رقمية جديدة من ACM أو IEEE أو O'Reilly ضمن خطة المقرر، يرجى إرسال الطلب قبل 10 ديسمبر.\n\nمع الشكر والتقدير،\nالمكتبة المركزية — الجامعة العربية المفتوحة`
    }
  };

  function getAdaptedEmail(email, langCode) {
    if (!email || langCode === "en") {
      return { subject: email.subject, body: email.fullBody, adapted: false };
    }
    if (langCode === "ar" && ARABIC_EMAIL_TRANSLATIONS[email.id]) {
      const tr = ARABIC_EMAIL_TRANSLATIONS[email.id];
      return { subject: tr.subject, body: tr.body, adapted: true };
    }
    if (langCode === "ar") {
      return {
        subject: `[${email.courseTag}] ${email.subject}`,
        body: `المُرسل: ${email.senderName}\nالمقرر: ${email.courseTag}\n\nملخص ومحتوى الرسالة:\n${email.fullBody}`,
        adapted: true
      };
    }
    const langObj = LANGUAGES.find((l) => l.code === langCode) || LANGUAGES[0];
    return {
      subject: `[${langObj.nativeName} • ${email.courseTag}] ${email.subject}`,
      body: `[${langObj.nativeName}]\n${email.fullBody}`,
      adapted: true
    };
  }

  function summarizeEmail(email, langCode) {
    const firstSentence = email.fullBody.split("\n").map((s) => s.trim()).find((s) => s.length > 25) || email.subject;
    if (langCode === "ar") {
      return `• المقرر: ${email.courseTag} (${PRIORITY_META[email.priority].labelAr})\n• الفكرة الرئيسة: ${getAdaptedEmail(email, "ar").subject}\n• الإجراء المقترح: مراجعة التفاصيل والمواعيد وإرسال الرد عند الحاجة.`;
    }
    return `• Course: ${email.courseTag} (${email.priority})\n• Key Point: ${firstSentence}\n• Action: Review deadlines and send confirmation reply if needed.`;
  }

  function generateSmartReply(email, tone, role, senderName, langCode) {
    if (langCode === "ar") {
      const greeting = role === "instructor" ? `عزيزي/عزيزتي ${email.senderName}،` : `سعادة ${email.senderName} المحترم/ة،`;
      const body = tone === "CONCISE"
        ? `شكراً لرسالتكم بخصوص مقرر ${email.courseTag}. تم الاطلاع على التفاصيل وسأعمل وفق المطلوب.`
        : tone === "SUPPORTIVE"
          ? `شكراً جزيلاً على تواصلكم وتوضيحكم بخصوص مقرر ${email.courseTag}. سأقوم بمراجعة كافة النقاط والمواعيد المحددة.`
          : `أشكركم على رسالتكم المتعلقة بمقرر ${email.courseTag}. أؤكد استلام التعليمات وسألتزم بالمواعيد والمتطلبات المذكورة.`;
      return `${greeting}\n\n${body}\n\nمع خالص التحية،\n${senderName}`;
    }
    const body = tone === "CONCISE"
      ? `Thank you for the update on ${email.courseTag}. Received and noted.`
      : tone === "SUPPORTIVE"
        ? `Thank you for reaching out about ${email.courseTag}. I appreciate the clarification and will follow up promptly.`
        : `Thank you for your message regarding ${email.courseTag}. I acknowledge the details and deadlines mentioned and will ensure timely follow-up.`;
    return `Dear ${email.senderName},\n\n${body}\n\nBest regards,\n${senderName}`;
  }

  function extractDates(email) {
    const matches = [];
    const regex = /\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\s+\d{1,2}(?:,\s*\d{4})?/gi;
    let m;
    while ((m = regex.exec(email.fullBody)) !== null && matches.length < 3) {
      matches.push({ title: `${email.courseTag} Milestone`, courseCode: email.courseTag, dateLabel: m[0], category: "assignment" });
    }
    if (!matches.length) {
      matches.push({ title: `${email.courseTag} Follow-up`, courseCode: email.courseTag, dateLabel: "Upcoming", category: "assignment" });
    }
    return matches;
  }

  const REPEATED_QUESTIONS = [
    {
      id: "rq_1",
      topic: "TM351 TMA Part 2: EXPLAIN ANALYZE & File Format",
      topicAr: "الواجب TM351 الجزء الثاني: صيغة EXPLAIN ANALYZE والملف",
      courseCode: "TM351",
      askCount: 14,
      summary: "14 students asked whether to include SQL text + EXPLAIN ANALYZE screenshots in PDF.",
      summaryAr: "سأل 14 طالباً عمّا إذا كان يجب إرفاق نصوص SQL مع لقطات EXPLAIN ANALYZE في ملف PDF.",
      studentEmails: ["2341098@aou.edu.sa", "2341104@aou.edu.sa", "2341189@aou.edu.sa"]
    },
    {
      id: "rq_2",
      topic: "M269 Exam Coverage: Dijkstra & Recurrence Sheet",
      topicAr: "نطاق اختبار M269: خوارزمية ديكسترا وورقة القوانين",
      courseCode: "M269",
      askCount: 9,
      summary: "9 students asked if Dijkstra formal proofs & Master Theorem sheet are included.",
      summaryAr: "استفسر 9 طلاب عمّا إذا كانت البراهين النظرية لخوارزمية ديكسترا وورقة القوانين مشمولة في الاختبار.",
      studentEmails: ["2341152@aou.edu.sa", "2341211@aou.edu.sa"]
    }
  ];

  function getDefaultCalendarEvents(role) {
    const now = Date.now();
    if (role === "student") {
      return [
        { id: "cal_stu_1", title: "TM351 Live Revision Clinic", courseCode: "TM351", category: "lecture", eventTimeMillis: now + 2 * 3600000 + 15 * 60000, alarmEnabled: true },
        { id: "cal_stu_2", title: "TM351 TMA Part 2 Submission", courseCode: "TM351", category: "assignment", eventTimeMillis: now + 4 * 86400000, alarmEnabled: true },
        { id: "cal_stu_3", title: "M269 Midterm Exam (Hall 204)", courseCode: "M269", category: "exam", eventTimeMillis: now + 6 * 86400000, alarmEnabled: true }
      ];
    }
    return [
      { id: "cal_ins_1", title: "TM351 Section 04 Lecture", courseCode: "TM351", category: "lecture", eventTimeMillis: now + 3600000 + 40 * 60000, alarmEnabled: true },
      { id: "cal_ins_2", title: "Office Hours — M269 & TM354", courseCode: "M269", category: "office_hour", eventTimeMillis: now + 26 * 3600000, alarmEnabled: true },
      { id: "cal_ins_3", title: "MTA Moderation Submission Deadline", courseCode: "TM351", category: "assignment", eventTimeMillis: now + 5 * 86400000, alarmEnabled: true }
    ];
  }

  const DEFAULT_FACULTY_MATERIALS = [
    {
      id: "mat_1",
      authorName: "Dr. Tariq Al-Mansoor",
      courseCode: "TM351",
      title: "Week 6: Query Optimization & Indexing Guide",
      description: "Slides + PostgreSQL EXPLAIN ANALYZE examples for TMA Part 2.",
      resourceUrl: "https://lms.aou.edu.sa/mod/resource/view.php?id=35106",
      materialType: "slides"
    },
    {
      id: "mat_2",
      authorName: "Dr. Tariq Al-Mansoor",
      courseCode: "M269",
      title: "Graph Algorithms Walkthrough Session",
      description: "Recorded revision tutorial covering BFS, DFS, and Dijkstra complexity.",
      resourceUrl: "https://teams.microsoft.com/l/meetup-join/aou-m269-rev",
      materialType: "recording"
    }
  ];

  const DEFAULT_CHAT_MESSAGES = [
    {
      id: "chat_1",
      senderName: "Dr. Tariq Al-Mansoor",
      channel: "direct",
      courseTag: "TM351",
      text: "Welcome to Direct Course Chat. Feel free to ask any TM351 or M269 question or share a schema image here."
    },
    {
      id: "chat_2",
      senderName: "Sara (CS Year 3)",
      channel: "general",
      courseTag: "M269",
      text: "Anyone revising M269 trees and graphs tonight in the Pomodoro focus room?"
    }
  ];

  const FRESHMEN_QA = [
    { id: "fq_1", category: "LMS & Portal", q: "How do I access my AOU LMS and SIS accounts during my first week?", a: "Use your official AOU student email and initial password from Admissions. SIS handles registration & grades while LMS hosts slides and TMAs." },
    { id: "fq_2", category: "Grading & Assessment", q: "How are AOU courses graded (TMA, MTA, Final Exam)?", a: "Most AOU courses divide 100 marks into Continuous Assessment (TMA 20% + MTA 30%) and Final Exam (50%)." },
    { id: "fq_3", category: "Registration", q: "What do TU170, EL111, AR111, and GR101 cover?", a: "Mandatory University Requirements in year 1 covering online learning, academic communication, and self-learning skills." },
    { id: "fq_4", category: "Prerequisites", q: "Why can't I register M251 or M269 yet?", a: "M251 requires TM105, and M269 requires both TM105 and MT131. Check the visual Roadmap flowchart to verify prerequisite arrows." }
  ];

  const RINGTONES = [
    { id: "chime", label: "Crystal Chime", freqs: [587.33, 880] },
    { id: "beacon", label: "Academic Bell", freqs: [523.25, 659.25] },
    { id: "pulse", label: "Digital Pulse", freqs: [783.99, 987.77] },
    { id: "horizon", label: "Morning Horizon", freqs: [440, 554.37] }
  ];

  return {
    MS_CLIENT_ID,
    MS_SCOPES,
    SAUDI_BRANCHES,
    REGIONAL_BRANCHES,
    LANGUAGES,
    getStrings,
    isRtl,
    COURSE_STATUSES,
    PRIORITY_META,
    MAJORS,
    getSampleEmails,
    getAdaptedEmail,
    summarizeEmail,
    generateSmartReply,
    extractDates,
    REPEATED_QUESTIONS,
    getDefaultCalendarEvents,
    DEFAULT_FACULTY_MATERIALS,
    DEFAULT_CHAT_MESSAGES,
    FRESHMEN_QA,
    RINGTONES
  };
})();
