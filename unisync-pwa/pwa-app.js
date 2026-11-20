(function () {
  const D = window.UNISYNC_DATA;
  const STORAGE_KEY = "unisync_pwa_state_v1";
  const LANG_STORAGE_KEY = "unisync_lang";

  const ICON_SVG_BADGE = `<img src="./icons/apple-touch-icon.png" alt="UniSync" class="app-badge-icon" />`;
  const ICON_SVG_BADGE_LG = `<img src="./icons/apple-touch-icon.png" alt="UniSync" class="app-badge-icon lg" />`;

  function escapeHtml(str) {
    return String(str || "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#39;");
  }

  function loadPersisted() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : {};
    } catch (_) {
      return {};
    }
  }

  function resolveInitialLanguage(saved) {
    try {
      const urlLang = new URLSearchParams(window.location.search).get("lang");
      if (urlLang && D.LANGUAGES.some((l) => l.code === urlLang)) return urlLang;
    } catch (_) {}
    try {
      const storedLang = localStorage.getItem(LANG_STORAGE_KEY);
      if (storedLang && D.LANGUAGES.some((l) => l.code === storedLang)) return storedLang;
    } catch (_) {}
    if (saved.languageCode && D.LANGUAGES.some((l) => l.code === saved.languageCode)) {
      return saved.languageCode;
    }
    return "ar";
  }

  function hydrateWithDefaults(savedList, defaultList) {
    if (!Array.isArray(savedList) || !savedList.length) return defaultList.slice();
    const byId = {};
    defaultList.forEach((d) => { byId[d.id] = d; });
    return savedList.map((item) => (byId[item.id] ? Object.assign({}, byId[item.id], item) : item));
  }

  const saved = loadPersisted();
  const initialLang = resolveInitialLanguage(saved);
  const initialRole = saved.role || "student";

  const state = {
    session: saved.session || null,
    role: initialRole,
    darkMode: Boolean(saved.darkMode),
    languageCode: initialLang,
    fontScale: Number(saved.fontScale || 1),
    aouBranch: saved.aouBranch || D.SAUDI_BRANCHES[0],
    selectedMajorId: saved.selectedMajorId || "cs",
    companionGender: saved.companionGender || "girl",
    totalPomodoroMinutes: Number(saved.totalPomodoroMinutes || 0),
    profilePhotoDataUrl: saved.profilePhotoDataUrl || "",
    alarmRingtoneId: saved.alarmRingtoneId || "chime",
    alarmVolume: typeof saved.alarmVolume === "number" ? saved.alarmVolume : 0.85,

    studentTab: "home",
    studentSubTool: "hub",
    instructorTab: "home",
    drawerOpen: false,
    showRepliedReviewed: false,

    emails: D.getSampleEmails(initialRole),
    priorityFilter: null,
    sortByPriority: true,
    selectedEmailId: null,
    showOriginalEmail: false,
    emailSummary: "",
    smartReplyDraft: "",
    replyTone: "FORMAL",
    sentEmailReplies: saved.sentEmailReplies || {},
    answeredQuestionIds: saved.answeredQuestionIds || [],
    answeredQuestionReplies: saved.answeredQuestionReplies || {},

    calendarEvents: hydrateWithDefaults(saved.calendarEvents, D.getDefaultCalendarEvents(initialRole)),
    triggeredEventIds: [],
    ringingEvent: null,

    courseStatuses: saved.courseStatuses || { GR101: "completed", AR111: "completed", EL111: "in_progress", TM105: "next_term" },
    roadmapZoom: 1,
    roadmapPanX: 0,
    roadmapPanY: 0,
    selectedCourseCode: null,

    manualNotes: saved.manualNotes || [],
    facultyMaterials: hydrateWithDefaults(saved.facultyMaterials, D.DEFAULT_FACULTY_MATERIALS),
    chatMessages: hydrateWithDefaults(saved.chatMessages, D.DEFAULT_CHAT_MESSAGES),
    chatTab: "direct",
    chatAttachmentDataUrl: "",

    studyMessages: [],
    freshmanSearch: "",

    voiceNotes: saved.voiceNotes || [],
    recordingVoice: false,
    playingVoiceId: null,

    pomodoroDuration: 25,
    pomodoroSeconds: 25 * 60,
    pomodoroRunning: false,
    pomodoroIsBreak: false,
    milestoneAward: null,

    unifiedReplyClusterId: null,
    unifiedReplyText: "",
    showComposeModal: false,
    showLangModal: false,
    showInstallModal: false,
    snackbar: "",
    deferredInstallPrompt: null,
    msAccessToken: null
  };

  // Apply persisted replied/viewed flags to initial emails
  const persistedMeta = saved.emailMeta || {};
  state.emails = state.emails.map((e) => {
    const m = persistedMeta[e.id];
    return m ? Object.assign({}, e, m) : e;
  });

  function isArabic() {
    return state.languageCode === "ar";
  }

  function loc(obj, enKey, arKey) {
    if (!obj) return "";
    if (isArabic() && obj[arKey]) return obj[arKey];
    return obj[enKey] || "";
  }

  function persist() {
    try {
      localStorage.setItem(LANG_STORAGE_KEY, state.languageCode);
      const emailMeta = {};
      state.emails.forEach((e) => {
        emailMeta[e.id] = { unread: e.unread, viewed: e.viewed, replied: e.replied };
      });
      localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({
          session: state.session,
          role: state.role,
          darkMode: state.darkMode,
          languageCode: state.languageCode,
          fontScale: state.fontScale,
          aouBranch: state.aouBranch,
          selectedMajorId: state.selectedMajorId,
          companionGender: state.companionGender,
          totalPomodoroMinutes: state.totalPomodoroMinutes,
          profilePhotoDataUrl: state.profilePhotoDataUrl,
          alarmRingtoneId: state.alarmRingtoneId,
          alarmVolume: state.alarmVolume,
          sentEmailReplies: state.sentEmailReplies,
          answeredQuestionIds: state.answeredQuestionIds,
          answeredQuestionReplies: state.answeredQuestionReplies,
          calendarEvents: state.calendarEvents,
          courseStatuses: state.courseStatuses,
          manualNotes: state.manualNotes,
          facultyMaterials: state.facultyMaterials,
          chatMessages: state.chatMessages,
          voiceNotes: state.voiceNotes,
          emailMeta
        })
      );
    } catch (_) {}
  }

  function syncUrlLanguage(code) {
    try {
      const url = new URL(window.location.href);
      url.searchParams.set("lang", code);
      window.history.replaceState(null, "", url.toString());
    } catch (_) {}
  }

  function setLanguage(code) {
    const valid = D.LANGUAGES.some((l) => l.code === code) ? code : "ar";
    state.languageCode = valid;
    state.showLangModal = false;
    state.showOriginalEmail = false;
    if (state.selectedEmailId) {
      const activeEmail = state.emails.find((e) => e.id === state.selectedEmailId);
      if (activeEmail && state.emailSummary) {
        state.emailSummary = D.summarizeEmail(activeEmail, valid);
      }
      if (activeEmail && state.smartReplyDraft) {
        state.smartReplyDraft = D.generateSmartReply(
          activeEmail,
          state.replyTone,
          state.role,
          (state.session && state.session.name) || "UniSync",
          valid
        );
      }
    }
    syncUrlLanguage(valid);
    persist();
    render();
  }

  let snackbarTimer = null;
  function showToast(msg) {
    state.snackbar = msg;
    renderSnackbar();
    clearTimeout(snackbarTimer);
    snackbarTimer = setTimeout(() => {
      state.snackbar = "";
      renderSnackbar();
    }, 3200);
  }

  // --- Theme, RTL & Viewport Helpers ---
  function applyThemeAndDirection() {
    const rtl = D.isRtl(state.languageCode);
    document.documentElement.lang = state.languageCode;
    document.documentElement.dir = rtl ? "rtl" : "ltr";
    document.documentElement.style.setProperty("--font-scale", String(state.fontScale));
    document.body.className = `role-${state.role} theme-${state.darkMode ? "dark" : "light"}`;

    const themeMeta = document.getElementById("theme-color-meta");
    if (themeMeta) {
      if (state.darkMode) themeMeta.content = "#0B0F17";
      else themeMeta.content = state.role === "instructor" ? "#1E4E8C" : "#1F6E5A";
    }
  }

  // --- Web Audio Synthesizer for Smart Alarm & Ringtones ---
  let audioCtx = null;
  let alarmLoopTimer = null;
  function playToneOnce(ringtoneId, volume) {
    try {
      const AudioCtor = window.AudioContext || window.webkitAudioContext;
      if (!AudioCtor) return;
      if (!audioCtx) audioCtx = new AudioCtor();
      if (audioCtx.state === "suspended") audioCtx.resume();
      const preset = D.RINGTONES.find((r) => r.id === ringtoneId) || D.RINGTONES[0];
      const now = audioCtx.currentTime;
      preset.freqs.forEach((f, idx) => {
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.type = "sine";
        osc.frequency.setValueAtTime(f, now + idx * 0.22);
        gain.gain.setValueAtTime(0.001, now + idx * 0.22);
        gain.gain.exponentialRampToValueAtTime(Math.max(0.05, volume * 0.35), now + idx * 0.22 + 0.04);
        gain.gain.exponentialRampToValueAtTime(0.001, now + idx * 0.22 + 0.36);
        osc.connect(gain);
        gain.connect(audioCtx.destination);
        osc.start(now + idx * 0.22);
        osc.stop(now + idx * 0.22 + 0.38);
      });
    } catch (_) {}
  }

  function startAlarmLoop(evt) {
    stopAlarmLoop();
    state.ringingEvent = evt;
    playToneOnce(state.alarmRingtoneId, state.alarmVolume);
    alarmLoopTimer = setInterval(() => {
      playToneOnce(state.alarmRingtoneId, state.alarmVolume);
    }, 1800);
    render();
  }

  function stopAlarmLoop() {
    clearInterval(alarmLoopTimer);
    alarmLoopTimer = null;
  }

  // --- Pomodoro Focus Lock (WakeLock, Fullscreen, Back/Unload Trap) ---
  let wakeLockSentinel = null;
  function isFocusLocked() {
    return (
      state.session &&
      state.role === "student" &&
      state.studentTab === "pomodoro" &&
      state.pomodoroRunning &&
      !state.pomodoroIsBreak
    );
  }

  async function engageFocusLock() {
    try {
      if ("wakeLock" in navigator && !wakeLockSentinel) {
        wakeLockSentinel = await navigator.wakeLock.request("screen");
      }
    } catch (_) {}
    try {
      if (document.documentElement.requestFullscreen && !document.fullscreenElement) {
        await document.documentElement.requestFullscreen();
      }
    } catch (_) {}
    try {
      history.pushState({ focusLock: true }, "", window.location.href);
    } catch (_) {}
  }

  async function releaseFocusLock() {
    try {
      if (wakeLockSentinel) {
        await wakeLockSentinel.release();
        wakeLockSentinel = null;
      }
    } catch (_) {}
    try {
      if (document.fullscreenElement && document.exitFullscreen) {
        await document.exitFullscreen();
      }
    } catch (_) {}
  }

  window.addEventListener("popstate", () => {
    if (isFocusLocked()) {
      try {
        history.pushState({ focusLock: true }, "", window.location.href);
      } catch (_) {}
    }
  });

  window.addEventListener("beforeunload", (e) => {
    if (isFocusLocked()) {
      e.preventDefault();
      e.returnValue = "";
    }
  });

  // --- Microsoft Entra ID PKCE OAuth Flow (Client ID preserved) ---
  function base64UrlEncode(buf) {
    const bytes = new Uint8Array(buf);
    let bin = "";
    for (let i = 0; i < bytes.length; i++) bin += String.fromCharCode(bytes[i]);
    return btoa(bin).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
  }

  async function startMicrosoftLogin() {
    const str = D.getStrings(state.languageCode);
    try {
      const randomBytes = new Uint8Array(32);
      crypto.getRandomValues(randomBytes);
      const verifier = base64UrlEncode(randomBytes);
      const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(verifier));
      const challenge = base64UrlEncode(digest);
      const redirectUri = window.location.origin + window.location.pathname;
      sessionStorage.setItem("unisync_ms_verifier", verifier);
      sessionStorage.setItem("unisync_ms_redirect", redirectUri);

      const url = new URL("https://login.microsoftonline.com/common/oauth2/v2.0/authorize");
      url.searchParams.set("client_id", D.MS_CLIENT_ID);
      url.searchParams.set("response_type", "code");
      url.searchParams.set("redirect_uri", redirectUri);
      url.searchParams.set("response_mode", "query");
      url.searchParams.set("scope", D.MS_SCOPES);
      url.searchParams.set("code_challenge", challenge);
      url.searchParams.set("code_challenge_method", "S256");
      window.location.href = url.toString();
    } catch (_) {
      showToast(str.toastMsRequiresHttps);
    }
  }

  async function handleMicrosoftCallbackIfPresent() {
    const params = new URLSearchParams(window.location.search);
    const code = params.get("code");
    if (!code) return;
    const str = D.getStrings(state.languageCode);
    const verifier = sessionStorage.getItem("unisync_ms_verifier");
    const redirectUri = sessionStorage.getItem("unisync_ms_redirect") || (window.location.origin + window.location.pathname);
    syncUrlLanguage(state.languageCode);
    if (!verifier) return;

    try {
      const body = new URLSearchParams({
        client_id: D.MS_CLIENT_ID,
        scope: D.MS_SCOPES,
        code,
        redirect_uri: redirectUri,
        grant_type: "authorization_code",
        code_verifier: verifier
      });
      const res = await fetch("https://login.microsoftonline.com/common/oauth2/v2.0/token", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body
      });
      const json = await res.json();
      if (json.access_token) {
        state.msAccessToken = json.access_token;
        const meRes = await fetch("https://graph.microsoft.com/v1.0/me", {
          headers: { Authorization: `Bearer ${json.access_token}` }
        });
        const me = meRes.ok ? await meRes.json() : {};
        state.session = {
          mode: "microsoft",
          name: me.displayName || "Outlook User",
          email: me.mail || me.userPrincipalName || "user@aou.edu.sa"
        };
        const msgRes = await fetch(
          "https://graph.microsoft.com/v1.0/me/messages?$top=15&$select=id,subject,bodyPreview,body,from,receivedDateTime,isRead,importance",
          {
            headers: {
              Authorization: `Bearer ${json.access_token}`,
              Prefer: 'outlook.body-content-type="text"'
            }
          }
        );
        if (msgRes.ok) {
          const msgJson = await msgRes.json();
          if (Array.isArray(msgJson.value) && msgJson.value.length) {
            state.emails = msgJson.value.map((m, i) => {
              const subj = m.subject || "(No Subject)";
              const bodyTxt = (m.body && m.body.content) || m.bodyPreview || "";
              const tagMatch = (subj + " " + bodyTxt).match(/\b([A-Z]{1,4}\d{3}(?:-[A-B]|[A-B])?)\b/);
              const prio = m.importance === "high" ? "CRITICAL" : m.importance === "low" ? "LOW" : "NORMAL";
              return {
                id: m.id,
                senderName: (m.from && m.from.emailAddress && m.from.emailAddress.name) || "Outlook Sender",
                senderEmail: (m.from && m.from.emailAddress && m.from.emailAddress.address) || "",
                recipientEmail: state.session.email,
                subject: subj,
                fullBody: bodyTxt,
                receivedAtLabel: (m.receivedDateTime || "").slice(0, 16).replace("T", " "),
                timestampMillis: Date.now() - i * 600000,
                priority: prio,
                courseTag: tagMatch ? tagMatch[1] : "AOU",
                unread: !m.isRead,
                viewed: Boolean(m.isRead),
                replied: false,
                fromGraph: true
              };
            });
          }
        }
        persist();
        render();
        showToast(str.toastMsConnected);
      }
    } catch (_) {
      showToast(str.toastMsOffline);
    }
  }

  // --- Voice Note Recording (Web MediaRecorder) ---
  let mediaRecorder = null;
  let audioChunks = [];
  let recordStartMillis = 0;

  async function startVoiceRecording() {
    const str = D.getStrings(state.languageCode);
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      showToast(str.toastVoiceUnsupported);
      return;
    }
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      audioChunks = [];
      mediaRecorder = new MediaRecorder(stream);
      mediaRecorder.ondataavailable = (ev) => {
        if (ev.data.size > 0) audioChunks.push(ev.data);
      };
      mediaRecorder.onstop = () => {
        stream.getTracks().forEach((t) => t.stop());
        const blob = new Blob(audioChunks, { type: "audio/webm" });
        const reader = new FileReader();
        reader.onloadend = () => {
          const curStr = D.getStrings(state.languageCode);
          const titleEl = document.getElementById("voice-title-input");
          const fallbackTitle = `${curStr.voiceNotes} ${state.voiceNotes.length + 1}`;
          const title = (titleEl && titleEl.value.trim()) || fallbackTitle;
          const durationSec = Math.max(1, Math.round((Date.now() - recordStartMillis) / 1000));
          state.voiceNotes.unshift({
            id: "vn_" + Date.now(),
            title,
            durationSec,
            dataUrl: reader.result
          });
          state.recordingVoice = false;
          persist();
          render();
          showToast(curStr.toastVoiceSaved);
        };
        reader.readAsDataURL(blob);
      };
      recordStartMillis = Date.now();
      state.recordingVoice = true;
      mediaRecorder.start();
      render();
    } catch (_) {
      showToast(str.toastMicDenied);
    }
  }

  function stopVoiceRecording() {
    if (mediaRecorder && mediaRecorder.state !== "inactive") {
      mediaRecorder.stop();
    } else {
      state.recordingVoice = false;
      render();
    }
  }

  let activeAudioPlayer = null;
  function togglePlayVoice(note) {
    if (activeAudioPlayer) {
      activeAudioPlayer.pause();
      activeAudioPlayer = null;
      if (state.playingVoiceId === note.id) {
        state.playingVoiceId = null;
        render();
        return;
      }
    }
    state.playingVoiceId = note.id;
    activeAudioPlayer = new Audio(note.dataUrl);
    activeAudioPlayer.onended = () => {
      state.playingVoiceId = null;
      activeAudioPlayer = null;
      render();
    };
    activeAudioPlayer.play().catch(() => {
      state.playingVoiceId = null;
      render();
    });
    render();
  }

  // --- Time & Formatting Helpers ---
  function formatCountdown(targetMillis, strObj) {
    const str = strObj || D.getStrings(state.languageCode);
    const diff = targetMillis - Date.now();
    if (diff <= 0) return str.dueNow;
    const totalSec = Math.floor(diff / 1000);
    const days = Math.floor(totalSec / 86400);
    const hrs = Math.floor((totalSec % 86400) / 3600);
    const mins = Math.floor((totalSec % 3600) / 60);
    const secs = totalSec % 60;
    const pad = (n) => String(n).padStart(2, "0");
    return days > 0 ? `${days}${str.dayShort} ${pad(hrs)}:${pad(mins)}:${pad(secs)}` : `${pad(hrs)}:${pad(mins)}:${pad(secs)}`;
  }

  function formatClock(sec) {
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
  }

  function formatDateTime(ms) {
    try {
      const locale = isArabic() ? "ar-SA" : state.languageCode;
      return new Intl.DateTimeFormat(locale, {
        month: "short",
        day: "numeric",
        hour: "numeric",
        minute: "2-digit"
      }).format(new Date(ms));
    } catch (_) {
      return new Date(ms).toLocaleString();
    }
  }

  function formatBranchChip(branchStr) {
    const parts = branchStr.split(" —");
    const main = parts[0];
    if (isArabic()) {
      const m = main.match(/\(([^)]+)\)/);
      return m ? m[1] : main;
    }
    return main.replace(/\s*\([^)]+\)/, "").trim();
  }

  // Background 1-second tick
  setInterval(() => {
    if (!state.session) return;
    const now = Date.now();

    if (!state.ringingEvent) {
      const due = state.calendarEvents.find(
        (evt) =>
          evt.alarmEnabled &&
          !state.triggeredEventIds.includes(evt.id) &&
          evt.eventTimeMillis <= now &&
          evt.eventTimeMillis >= now - 15000
      );
      if (due) {
        state.triggeredEventIds.push(due.id);
        startAlarmLoop(due);
        return;
      }
    }

    const str = D.getStrings(state.languageCode);
    document.querySelectorAll("[data-countdown-ms]").forEach((el) => {
      el.textContent = formatCountdown(Number(el.getAttribute("data-countdown-ms")), str);
    });

    if (state.pomodoroRunning) {
      if (state.pomodoroSeconds > 0) {
        state.pomodoroSeconds -= 1;
        const clockEl = document.getElementById("pomodoro-clock-el");
        if (clockEl) clockEl.textContent = formatClock(state.pomodoroSeconds);
      } else {
        state.pomodoroRunning = false;
        releaseFocusLock();
        if (!state.pomodoroIsBreak) {
          const prevBucket = Math.floor(state.totalPomodoroMinutes / 30);
          state.totalPomodoroMinutes += state.pomodoroDuration;
          const nextBucket = Math.floor(state.totalPomodoroMinutes / 30);
          if (nextBucket > prevBucket) {
            state.milestoneAward = nextBucket * 30;
          }
        }
        state.pomodoroIsBreak = false;
        state.pomodoroSeconds = state.pomodoroDuration * 60;
        persist();
        playToneOnce(state.alarmRingtoneId, state.alarmVolume);
        render();
      }
    }
  }, 1000);

  // --- Shared SVG Icons ---
  const ICONS = {
    menu: `<svg width="22" height="22" viewBox="0 0 24 24" fill="currentColor"><path d="M3 6h18v2H3V6zm0 5h18v2H3v-2zm0 5h18v2H3v-2z"/></svg>`,
    dark: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M12 3a9 9 0 1 0 9 9c0-.46-.04-.92-.1-1.36a5.389 5.389 0 0 1-4.4 2.26 5.403 5.403 0 0 1-3.14-9.8c-.44-.06-.9-.1-1.36-.1z"/></svg>`,
    light: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M6.76 4.84l-1.8-1.79-1.41 1.41 1.79 1.79 1.42-1.41zM4 10.5H1v2h3v-2zm9-9.95h-2V3.5h2V.55zm7.45 3.91l-1.41-1.41-1.79 1.79 1.41 1.41 1.79-1.79zm-3.21 13.7l1.79 1.8 1.41-1.41-1.8-1.79-1.4 1.4zM20 10.5v2h3v-2h-3zm-8-5c-3.31 0-6 2.69-6 6s2.69 6 6 6 6-2.69 6-6-2.69-6-6-6zm-1 16.95h2V19.5h-2v2.95zm-7.45-3.91l1.41 1.41 1.79-1.8-1.41-1.41-1.79 1.8z"/></svg>`,
    settings: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58a.49.49 0 0 0 .12-.61l-1.92-3.32a.488.488 0 0 0-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54a.484.484 0 0 0-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58a.49.49 0 0 0-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z"/></svg>`,
    home: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"/></svg>`,
    inbox: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"/></svg>`,
    timer: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M15 1H9v2h6V1zm-4 13h2V8h-2v6zm8.03-6.61l1.42-1.42c-.43-.51-.9-.99-1.41-1.41l-1.42 1.42A8.962 8.962 0 0 0 12 4c-4.97 0-9 4.03-9 9s4.02 9 9 9a8.994 8.994 0 0 0 7.03-14.61zM12 20c-3.87 0-7-3.13-7-7s3.13-7 7-7 7 3.13 7 7-3.13 7-7 7z"/></svg>`,
    chat: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/></svg>`,
    map: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M20.5 3l-.16.03L15 5.1 9 3 3.36 4.9c-.21.07-.36.25-.36.48V20.5c0 .28.22.5.5.5l.16-.03L9 18.9l6 2.1 5.64-1.9c.21-.07.36-.25.36-.48V3.5c0-.28-.22-.5-.5-.5zM15 19l-6-2.11V5l6 2.11V19z"/></svg>`,
    tools: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z"/></svg>`,
    calendar: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M19 3h-1V1h-2v2H8V1H6v2H5c-1.11 0-1.99.9-1.99 2L3 19c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V8h14v11z"/></svg>`,
    materials: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/></svg>`,
    history: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M13 3a9 9 0 0 0-9 9H1l3.89 3.89.07.14L9 12H6c0-3.87 3.13-7 7-7s7 3.13 7 7-3.13 7-7 7c-1.93 0-3.68-.79-4.94-2.06l-1.42 1.42A8.954 8.954 0 0 0 13 21a9 9 0 0 0 0-18zm-1 5v5l4.28 2.54.72-1.21-3.5-2.08V8H12z"/></svg>`,
    camera: `<svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M12 15.2a3.2 3.2 0 1 0 0-6.4 3.2 3.2 0 0 0 0 6.4z"/><path d="M9 2L7.17 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2h-3.17L15 2H9zm3 15c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5z"/></svg>`,
    lock: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/></svg>`,
    signOut: `<svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor"><path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/></svg>`
  };

  function renderPriorityBadge(priority) {
    const meta = D.PRIORITY_META[priority] || D.PRIORITY_META.NORMAL;
    return `<span class="badge ${meta.badgeClass}">${escapeHtml(isArabic() ? meta.labelAr : meta.labelEn)}</span>`;
  }

  function renderUrgentEmailBanner(str) {
    const rtl = D.isRtl(state.languageCode);
    const target = state.emails
      .filter((e) => !e.replied && (e.priority === "CRITICAL" || e.priority === "HIGH"))
      .sort((a, b) => D.PRIORITY_META[a.priority].rank - D.PRIORITY_META[b.priority].rank)[0];
    if (!target) return "";
    const adapted = D.getAdaptedEmail(target, state.languageCode);
    return `
      <div class="urgent-banner" data-open-email="${escapeHtml(target.id)}" role="button" tabindex="0">
        <div style="min-width:0;flex:1;">
          <div style="font-size:0.78rem;font-weight:800;color:var(--highlight);">${escapeHtml(str.urgentEmailAlert)} • ${escapeHtml(target.courseTag)}</div>
          <div style="font-weight:700;font-size:0.9rem;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${escapeHtml(adapted.subject)}</div>
          <div class="muted" style="font-size:0.76rem;">${escapeHtml(loc(target, "senderName", "senderNameAr"))} • ${escapeHtml(str.tapToOpenReply)}</div>
        </div>
        <span style="font-weight:800;color:var(--highlight);">${rtl ? "←" : "→"}</span>
      </div>
    `;
  }

  function renderNextUpCard(str) {
    const now = Date.now();
    const upcoming = state.calendarEvents.filter((e) => e.eventTimeMillis >= now - 60000).sort((a, b) => a.eventTimeMillis - b.eventTimeMillis)[0] || state.calendarEvents[0];
    if (!upcoming) return "";
    return `
      <div class="card highlight-border">
        <div class="row-between">
          <span class="badge badge-tag">${escapeHtml(str.nextUp)} • ${escapeHtml(upcoming.courseCode)}</span>
          <button class="btn btn-outline btn-sm" id="btn-open-next-cal">${escapeHtml(str.calendarAlarm)}</button>
        </div>
        <div class="card-title">${escapeHtml(loc(upcoming, "title", "titleAr"))}</div>
        <div class="countdown-digits" data-countdown-ms="${upcoming.eventTimeMillis}">${formatCountdown(upcoming.eventTimeMillis, str)}</div>
        <div class="muted">${escapeHtml(formatDateTime(upcoming.eventTimeMillis))}</div>
      </div>
    `;
  }

  // --- Screens ---
  function renderLoginScreen(str) {
    const curLang = D.LANGUAGES.find((l) => l.code === state.languageCode) || D.LANGUAGES[0];
    return `
      <div class="login-wrap">
        <div class="login-card card">
          <div class="login-brand">
            ${ICON_SVG_BADGE_LG}
            <h1 style="margin:0;font-size:1.7rem;font-weight:900;letter-spacing:-0.03em;">UniSync</h1>
          </div>

          <div class="row-between">
            <button class="chip" id="login-lang-btn">🌐 ${escapeHtml(curLang.nativeName)}</button>
            <button class="chip" id="login-dark-btn">${state.darkMode ? "☀️" : "🌙"} ${escapeHtml(str.darkMode)}</button>
          </div>

          <div style="font-weight:700;font-size:0.88rem;color:var(--text-secondary);">
            ${escapeHtml(str.chooseRole)}
          </div>
          <div class="branch-grid">
            <button class="chip ${state.role === "student" ? "selected" : ""}" data-select-role="student" style="min-height:44px;">🎓 ${escapeHtml(str.studentRole)}</button>
            <button class="chip ${state.role === "instructor" ? "selected" : ""}" data-select-role="instructor" style="min-height:44px;">👨‍🏫 ${escapeHtml(str.instructorRole)}</button>
          </div>

          <button class="btn btn-primary btn-full" id="btn-ms-login">
            ${escapeHtml(str.signInMicrosoft)}
          </button>

          <div class="divider"></div>

          <input id="academic-name-input" class="field" placeholder="${escapeHtml(str.fullNameOptional)}" />
          <input id="academic-email-input" class="field" type="email" placeholder="${escapeHtml(str.universityEmailPlaceholder)}" />
          <button class="btn btn-tonal btn-full" id="btn-academic-login">
            ${escapeHtml(str.signInAcademic)}
          </button>

          <button class="btn btn-outline btn-full" id="btn-demo-login">
            ${escapeHtml(str.demoAccount)} (${state.role === "instructor" ? escapeHtml(str.instructorRole) : escapeHtml(str.studentRole)})
          </button>

          <button class="btn btn-outline btn-sm btn-full" id="btn-open-install">
            📲 ${escapeHtml(str.installApp)}
          </button>
        </div>
      </div>
    `;
  }

  function renderStudentHome(str) {
    return `
      ${renderUrgentEmailBanner(str)}
      ${renderNextUpCard(str)}
      <div class="card">
        <div class="row-between">
          <div class="card-title">${escapeHtml(str.calendarAlarm)}</div>
          <button class="btn btn-outline btn-sm" id="btn-manage-cal">${escapeHtml(str.manage)}</button>
        </div>
        ${state.calendarEvents.slice(0, 4).map((evt) => `
          <div class="row-between" style="padding:10px;border:1px solid var(--border-card);border-radius:12px;background:var(--bg-canvas);">
            <div style="min-width:0;">
              <div style="font-weight:700;font-size:0.9rem;">${escapeHtml(loc(evt, "title", "titleAr"))}</div>
              <div class="muted" style="font-size:0.78rem;">${escapeHtml(evt.courseCode)} • ${escapeHtml(formatDateTime(evt.eventTimeMillis))}</div>
            </div>
            <div class="row-wrap" style="gap:6px;flex-shrink:0;">
              <button class="btn btn-outline btn-sm" data-ring-evt="${escapeHtml(evt.id)}" aria-label="${escapeHtml(str.previewRingtone)}">🔔</button>
              <button class="switch-btn ${evt.alarmEnabled ? "on" : ""}" data-toggle-alarm="${escapeHtml(evt.id)}" aria-label="${escapeHtml(str.calendarAlarm)}">
                <span class="switch-knob"></span>
              </button>
            </div>
          </div>
        `).join("")}
      </div>
    `;
  }

  function renderInstructorHome(str) {
    const activeClusters = D.REPEATED_QUESTIONS.filter((c) => !state.answeredQuestionIds.includes(c.id));
    const primary = activeClusters[0];

    const repeatedCard = primary
      ? `
        <div class="card">
          <div class="row-between">
            <div class="card-title">${escapeHtml(str.repeatedQuestions)}</div>
            <span class="badge badge-tag">${primary.askCount}× • ${escapeHtml(primary.courseCode)}</span>
          </div>
          <div style="font-weight:700;">${escapeHtml(loc(primary, "topic", "topicAr"))}</div>
          <div class="muted">${escapeHtml(loc(primary, "summary", "summaryAr"))}</div>
          <button class="btn btn-primary btn-full" id="btn-open-unified-reply" data-cluster-id="${escapeHtml(primary.id)}">
            ${escapeHtml(str.unifiedReplyAll)}
          </button>
        </div>
      `
      : `
        <div class="card">
          <div class="card-title">${escapeHtml(str.repeatedQuestions)}</div>
          <div class="row-wrap" style="color:var(--status-completed);font-weight:700;">
            <span>✓</span>
            <span>${escapeHtml(str.allCaughtUp)}</span>
          </div>
        </div>
      `;

    const topEmails = state.emails
      .slice()
      .sort((a, b) => D.PRIORITY_META[a.priority].rank - D.PRIORITY_META[b.priority].rank)
      .slice(0, 3);

    return `
      ${repeatedCard}
      ${renderNextUpCard(str)}
      ${renderUrgentEmailBanner(str)}
      <div class="row-between">
        <div class="card-title">${escapeHtml(str.inbox)}</div>
        <button class="btn btn-outline btn-sm" id="btn-ins-open-inbox">${escapeHtml(str.openFullInbox)}</button>
      </div>
      ${topEmails.map((email) => {
        const adapted = D.getAdaptedEmail(email, state.languageCode);
        return `
          <div class="card clickable" data-open-email="${escapeHtml(email.id)}">
            <div class="row-between">
              <span style="font-weight:700;font-size:0.88rem;">${escapeHtml(loc(email, "senderName", "senderNameAr"))}</span>
              <span class="muted" style="font-size:0.75rem;">${escapeHtml(loc(email, "receivedAtLabel", "receivedAtAr"))}</span>
            </div>
            <div style="font-weight:600;font-size:0.94rem;">${escapeHtml(adapted.subject)}</div>
            <div class="row-between">
              ${renderPriorityBadge(email.priority)}
              <span class="badge badge-tag">${escapeHtml(email.courseTag)}</span>
            </div>
          </div>
        `;
      }).join("")}
    `;
  }

  function renderInboxScreen(str) {
    const rtl = D.isRtl(state.languageCode);
    if (state.selectedEmailId) {
      const email = state.emails.find((e) => e.id === state.selectedEmailId);
      if (email) {
        const adapted = D.getAdaptedEmail(email, state.languageCode);
        const showAdapted = state.languageCode !== "en" && !state.showOriginalEmail;
        const subject = showAdapted ? adapted.subject : email.subject;
        const body = showAdapted ? adapted.body : email.fullBody;
        const savedReply = state.sentEmailReplies[email.id];

        const toneMap = {
          FORMAL: str.toneFormal,
          CONCISE: str.toneConcise,
          SUPPORTIVE: str.toneSupportive
        };

        return `
          <div class="row-between">
            <button class="btn btn-outline btn-sm" id="btn-back-inbox">${rtl ? "→" : "←"} ${escapeHtml(str.backToInbox)}</button>
            ${state.languageCode !== "en" ? `
              <div class="row-wrap">
                <button class="chip ${!state.showOriginalEmail ? "selected" : ""}" id="btn-view-adapted">${escapeHtml(str.adapted)}</button>
                <button class="chip ${state.showOriginalEmail ? "selected" : ""}" id="btn-view-original">${escapeHtml(str.original)}</button>
              </div>
            ` : ""}
          </div>

          <div class="card">
            <div class="row-between">
              ${renderPriorityBadge(email.priority)}
              <span class="badge badge-tag">${escapeHtml(email.courseTag)}</span>
            </div>
            <div class="card-title" style="font-size:1.1rem;">${escapeHtml(subject)}</div>
            <div class="muted">${escapeHtml(loc(email, "senderName", "senderNameAr"))} (${escapeHtml(email.senderEmail)}) • ${escapeHtml(loc(email, "receivedAtLabel", "receivedAtAr"))}</div>
            <div class="divider"></div>
            <div style="white-space:pre-wrap;font-size:0.94rem;">${escapeHtml(body)}</div>
          </div>

          <div class="row-wrap">
            <button class="btn btn-tonal btn-sm" id="btn-summarize-email">${escapeHtml(str.summarizeEmail)}</button>
            <button class="btn btn-outline btn-sm" id="btn-extract-dates">${escapeHtml(str.extractDates)}</button>
          </div>

          ${state.emailSummary ? `
            <div class="card" style="background:var(--bg-variant);">
              <div style="font-weight:800;font-size:0.86rem;color:var(--primary);">${escapeHtml(str.summarizeEmail)}</div>
              <div style="white-space:pre-wrap;font-size:0.88rem;">${escapeHtml(state.emailSummary)}</div>
            </div>
          ` : ""}

          ${savedReply ? `
            <div class="card">
              <div class="badge badge-success">${escapeHtml(str.sentReplyBadge)}</div>
              <div style="white-space:pre-wrap;font-size:0.88rem;">${escapeHtml(savedReply)}</div>
            </div>
          ` : ""}

          <div class="card">
            <div class="row-between">
              <div class="card-title">${escapeHtml(str.smartReply)}</div>
              <div class="row-wrap">
                ${["FORMAL", "CONCISE", "SUPPORTIVE"].map((t) => `
                  <button class="chip ${state.replyTone === t ? "selected" : ""}" data-tone-reply="${t}">${escapeHtml(toneMap[t])}</button>
                `).join("")}
              </div>
            </div>
            <button class="btn btn-tonal btn-sm" id="btn-gen-smart-reply">${escapeHtml(str.smartReply)}</button>
            <textarea id="email-reply-box" class="field" placeholder="${escapeHtml(str.writeReplyPlaceholder)}">${escapeHtml(state.smartReplyDraft)}</textarea>
            <button class="btn btn-primary btn-full" id="btn-send-email-reply">${escapeHtml(str.sendReply)}</button>
          </div>
        `;
      }
    }

    let list = state.emails.slice();
    if (state.priorityFilter) list = list.filter((e) => e.priority === state.priorityFilter);
    list.sort((a, b) => state.sortByPriority
      ? D.PRIORITY_META[a.priority].rank - D.PRIORITY_META[b.priority].rank
      : b.timestampMillis - a.timestampMillis);

    return `
      <div class="row-between">
        <h2 class="section-title">${escapeHtml(str.inbox)}</h2>
        <div class="row-wrap">
          <button class="chip ${state.sortByPriority ? "selected" : ""}" id="btn-toggle-sort">${escapeHtml(str.prioritySort)}</button>
          <button class="btn btn-primary btn-sm" id="btn-open-compose">+ ${escapeHtml(str.compose)}</button>
        </div>
      </div>

      <div class="row-scroll">
        <button class="chip ${!state.priorityFilter ? "selected" : ""}" data-prio-filter="ALL">${escapeHtml(str.all)}</button>
        ${["CRITICAL", "HIGH", "NORMAL", "LOW"].map((p) => {
          const meta = D.PRIORITY_META[p];
          return `<button class="chip ${state.priorityFilter === p ? "selected" : ""}" data-prio-filter="${p}">${escapeHtml(isArabic() ? meta.labelAr : meta.labelEn)}</button>`;
        }).join("")}
      </div>

      ${list.map((email) => {
        const adapted = D.getAdaptedEmail(email, state.languageCode);
        return `
          <div class="card clickable" data-open-email="${escapeHtml(email.id)}">
            <div class="row-between">
              <span style="font-weight:${email.unread ? 800 : 600};">${escapeHtml(loc(email, "senderName", "senderNameAr"))}</span>
              <span class="muted" style="font-size:0.75rem;">${escapeHtml(loc(email, "receivedAtLabel", "receivedAtAr"))}</span>
            </div>
            <div style="font-weight:${email.unread ? 700 : 500};font-size:0.95rem;">${escapeHtml(adapted.subject)}</div>
            <div class="row-between">
              <div class="row-wrap">
                ${renderPriorityBadge(email.priority)}
                ${email.replied ? `<span class="badge badge-success">✓</span>` : ""}
              </div>
              <span class="badge badge-tag">${escapeHtml(email.courseTag)}</span>
            </div>
          </div>
        `;
      }).join("")}
    `;
  }

  function renderRepliedAndReviewed(str) {
    const rtl = D.isRtl(state.languageCode);
    const answeredClusters = D.REPEATED_QUESTIONS.filter((c) => state.answeredQuestionIds.includes(c.id));
    const reviewedEmails = state.emails.filter((e) => e.replied || e.viewed);

    return `
      <div class="row-wrap">
        <button class="btn btn-outline btn-sm" id="btn-close-history">${rtl ? "→" : "←"} ${escapeHtml(str.home)}</button>
        <h2 class="section-title">${escapeHtml(str.repliedReviewed)}</h2>
      </div>

      ${!answeredClusters.length && !reviewedEmails.length ? `
        <div class="card"><div class="muted">${escapeHtml(str.noHistoryYet)}</div></div>
      ` : ""}

      ${state.role === "instructor" && answeredClusters.length ? `
        <div style="font-weight:800;color:var(--primary);">${escapeHtml(str.answeredRepeatedHeader)}</div>
        ${answeredClusters.map((c) => `
          <div class="card">
            <div class="row-between">
              <span class="badge badge-tag">${escapeHtml(c.courseCode)} • ${c.askCount}</span>
              <span class="badge badge-success">${escapeHtml(str.answeredBadge)}</span>
            </div>
            <div class="card-title">${escapeHtml(loc(c, "topic", "topicAr"))}</div>
            ${state.answeredQuestionReplies[c.id] ? `
              <div style="padding:10px;border-radius:10px;background:var(--bg-canvas);border:1px solid var(--border-card);white-space:pre-wrap;font-size:0.86rem;">
                ${escapeHtml(state.answeredQuestionReplies[c.id])}
              </div>
            ` : ""}
          </div>
        `).join("")}
      ` : ""}

      ${reviewedEmails.length ? `
        <div style="font-weight:800;color:var(--primary);">${escapeHtml(str.repliedEmailsHeader)} (${reviewedEmails.length})</div>
        ${reviewedEmails.map((email) => {
          const adapted = D.getAdaptedEmail(email, state.languageCode);
          const replySaved = state.sentEmailReplies[email.id];
          return `
            <div class="card clickable" data-open-email="${escapeHtml(email.id)}">
              <div class="row-between">
                <span style="font-weight:700;">${escapeHtml(loc(email, "senderName", "senderNameAr"))}</span>
                <span class="badge ${email.replied ? "badge-success" : "badge-tag"}">${escapeHtml(email.replied ? str.repliedLabel : str.reviewedLabel)}</span>
              </div>
              <div style="font-weight:600;">${escapeHtml(adapted.subject)}</div>
              <div class="row-between">
                ${renderPriorityBadge(email.priority)}
                <span class="muted" style="font-size:0.76rem;">${escapeHtml(email.courseTag)} • ${escapeHtml(loc(email, "receivedAtLabel", "receivedAtAr"))}</span>
              </div>
              ${replySaved ? `<div class="muted" style="padding:8px;border-radius:8px;background:var(--bg-canvas);border:1px solid var(--border-card);">${escapeHtml(replySaved)}</div>` : ""}
            </div>
          `;
        }).join("")}
      ` : ""}
    `;
  }

  function renderRoadmapsScreen(str) {
    const ar = isArabic();
    const major = D.MAJORS.find((m) => m.id === state.selectedMajorId) || D.MAJORS[0];
    const completedCount = major.courses.filter((c) => state.courseStatuses[c.code] === "completed").length;
    const totalCount = major.courses.length;
    const pct = totalCount ? Math.round((completedCount / totalCount) * 100) : 0;

    const stagesMap = {};
    major.courses.forEach((c) => {
      if (!stagesMap[c.stage]) stagesMap[c.stage] = [];
      stagesMap[c.stage].push(c);
    });
    const stageKeys = Object.keys(stagesMap).map(Number).sort((a, b) => a - b);

    return `
      <div class="row-between">
        <h2 class="section-title">${escapeHtml(str.roadmaps)}</h2>
        <span class="badge badge-success">${completedCount}/${totalCount} (${pct}%)</span>
      </div>

      <div class="row-scroll">
        ${D.MAJORS.map((m) => `
          <button class="chip ${m.id === major.id ? "selected" : ""}" data-select-major="${m.id}">
            ${escapeHtml(ar ? m.nameAr : m.nameEn)}
          </button>
        `).join("")}
      </div>

      <div class="progress-track"><div class="progress-fill" style="width:${pct}%"></div></div>

      <div class="row-between">
        <div class="row-wrap">
          <button class="btn btn-outline btn-sm" id="btn-zoom-out">−</button>
          <button class="btn btn-outline btn-sm" id="btn-zoom-reset">${Math.round(state.roadmapZoom * 100)}%</button>
          <button class="btn btn-outline btn-sm" id="btn-zoom-in">+</button>
        </div>
        <div class="row-wrap">
          ${D.COURSE_STATUSES.map((s) => `
            <span class="badge" style="background:${s.color}22;color:${s.color};">${escapeHtml(ar ? s.labelAr : s.labelEn)}</span>
          `).join("")}
        </div>
      </div>

      <div class="roadmap-viewport" id="roadmap-viewport">
        <div class="roadmap-canvas" id="roadmap-canvas" style="transform:translate(${state.roadmapPanX}px, ${state.roadmapPanY}px) scale(${state.roadmapZoom});">
          <svg class="roadmap-svg-arrows" id="roadmap-arrows-svg"></svg>
          <div class="roadmap-stages">
            ${stageKeys.map((st) => `
              <div class="roadmap-stage-row">
                <div style="font-weight:800;font-size:0.8rem;color:var(--primary);">${escapeHtml(str.stageLabel)} ${st}</div>
                <div class="roadmap-nodes-row">
                  ${stagesMap[st].map((course) => {
                    const stKey = state.courseStatuses[course.code] || "not_started";
                    const stMeta = D.COURSE_STATUSES.find((s) => s.key === stKey) || D.COURSE_STATUSES[3];
                    return `
                      <div class="course-node" data-course-node="${escapeHtml(course.code)}" style="border-color:${stMeta.color};">
                        <div>
                          <div class="row-between">
                            <strong style="font-size:0.86rem;color:${stMeta.color};">${escapeHtml(course.code)}</strong>
                            <span class="muted" style="font-size:0.72rem;">${course.credits} ${escapeHtml(str.creditsShort)}</span>
                          </div>
                          <div style="font-weight:600;font-size:0.78rem;line-height:1.25;margin-top:4px;">${escapeHtml(loc(course, "title", "titleAr"))}</div>
                        </div>
                        <div>
                          <div style="font-size:0.7rem;font-weight:700;color:${stMeta.color};">${escapeHtml(ar ? stMeta.labelAr : stMeta.labelEn)}</div>
                          <div class="course-node-status-bar">
                            ${D.COURSE_STATUSES.map((opt) => `
                              <button class="status-dot-btn" data-set-course="${escapeHtml(course.code)}" data-status="${opt.key}"
                                style="${stKey === opt.key ? `background:${opt.color};color:#fff;border-color:${opt.color};` : ""}">
                                ${escapeHtml(ar ? opt.shortAr : opt.shortEn)}
                              </button>
                            `).join("")}
                          </div>
                        </div>
                      </div>
                    `;
                  }).join("")}
                </div>
              </div>
            `).join("")}
          </div>
        </div>
      </div>
    `;
  }

  function renderCompanionSvg() {
    const outfit = state.companionGender === "girl" ? "var(--primary)" : "#28558A";
    const hair = "#3A281E";
    const deskOrCup = state.pomodoroIsBreak
      ? `<rect x="74" y="64" width="14" height="20" rx="4" fill="#F59E0B"/><path d="M81 62 L85 50" stroke="#fff" stroke-width="2.5" stroke-linecap="round"/>`
      : `<rect x="34" y="76" width="52" height="14" rx="4" fill="#fff" stroke="var(--border-card)"/><line x1="60" y1="76" x2="60" y2="90" stroke="${outfit}" stroke-width="2.5"/>`;

    return `
      <svg width="122" height="122" viewBox="0 0 120 120" aria-hidden="true">
        <rect x="14" y="88" width="92" height="12" rx="6" fill="#D6C7B2"/>
        <rect x="42" y="56" width="36" height="34" rx="12" fill="${outfit}"/>
        ${state.companionGender === "girl" ? `<circle cx="60" cy="36" r="23" fill="${hair}"/><rect x="39" y="36" width="42" height="22" rx="10" fill="${hair}"/>` : ""}
        <circle cx="60" cy="34" r="19" fill="#F6D5B8"/>
        <path d="M41 34 A19 19 0 0 1 79 34 Z" fill="${hair}"/>
        <circle cx="53.5" cy="34" r="2.5" fill="#1E293B"/>
        <circle cx="66.5" cy="34" r="2.5" fill="#1E293B"/>
        <path d="M55 40 Q60 44 65 40" stroke="#9A4E3B" stroke-width="2" fill="none" stroke-linecap="round"/>
        ${deskOrCup}
      </svg>
    `;
  }

  function renderPomodoroScreen(str) {
    // ZERO explanatory text, descriptions, or notes anywhere on Pomodoro screen!
    const locked = isFocusLocked();
    const progress30 = state.totalPomodoroMinutes % 30;
    const pct30 = Math.round((progress30 / 30) * 100);

    return `
      ${!locked ? `
        <div class="row-between">
          <h2 class="section-title">${escapeHtml(str.pomodoro)}</h2>
          <span class="badge badge-tag">🏆 ${state.totalPomodoroMinutes} ${escapeHtml(str.minuteUnit)}</span>
        </div>
        <div class="row-wrap">
          <button class="chip ${state.companionGender === "girl" ? "selected" : ""}" data-companion="girl">${escapeHtml(str.girlLabel)}</button>
          <button class="chip ${state.companionGender === "boy" ? "selected" : ""}" data-companion="boy">${escapeHtml(str.boyLabel)}</button>
        </div>
      ` : `
        <div style="display:flex;justify-content:center;color:var(--primary);padding-top:8px;">
          ${ICONS.lock}
        </div>
      `}

      <div class="card">
        <div class="pomodoro-hero">
          ${renderCompanionSvg()}
          <div style="text-align:center;">
            <div style="font-size:0.78rem;font-weight:800;color:var(--highlight);">${escapeHtml(state.pomodoroIsBreak ? str.breakTime : str.studyFocus)}</div>
            <div id="pomodoro-clock-el" class="pomodoro-clock">${formatClock(state.pomodoroSeconds)}</div>
          </div>
        </div>

        ${!locked ? `
          <div class="row-wrap" style="justify-content:center;">
            ${[25, 30, 45, 50, 60].map((d) => `
              <button class="chip ${state.pomodoroDuration === d && !state.pomodoroIsBreak ? "selected" : ""}" data-pomo-dur="${d}">${d}${escapeHtml(str.minutesShort)}</button>
            `).join("")}
          </div>
          <div class="branch-grid">
            <button class="btn btn-primary" id="btn-pomo-start" ${state.pomodoroRunning ? "disabled" : ""}>▶ ${escapeHtml(str.startFocus)}</button>
            <button class="btn btn-outline" id="btn-pomo-break" ${state.pomodoroRunning ? "disabled" : ""}>☕ ${escapeHtml(str.shortBreak)}</button>
          </div>
        ` : ""}

        ${state.pomodoroRunning ? `
          <button class="btn btn-danger-outline btn-full" id="btn-pomo-exit">✕ ${escapeHtml(str.exitPomodoro)}</button>
        ` : ""}
      </div>

      <div class="card">
        <div class="row-between">
          <span style="font-weight:700;font-size:0.9rem;">🏆 ${escapeHtml(str.milestone30)}</span>
          <span style="font-weight:800;color:var(--primary);">${progress30} / 30</span>
        </div>
        <div class="progress-track"><div class="progress-fill" style="width:${pct30}%"></div></div>
      </div>
    `;
  }

  function renderChatScreen(str) {
    const showGen = state.role === "student";
    const channel = showGen && state.chatTab === "general" ? "general" : "direct";
    const msgs = state.chatMessages.filter((m) => m.channel === channel);

    return `
      <h2 class="section-title">${escapeHtml(str.chat)}</h2>
      ${showGen ? `
        <div class="branch-grid">
          <button class="chip ${channel === "direct" ? "selected" : ""}" data-chat-tab="direct">${escapeHtml(str.directInstructorChat)}</button>
          <button class="chip ${channel === "general" ? "selected" : ""}" data-chat-tab="general">${escapeHtml(str.generalStudentChat)}</button>
        </div>
      ` : `<span class="badge badge-tag">${escapeHtml(str.directChatNotice)}</span>`}

      ${msgs.map((m) => `
        <div class="card">
          <div class="row-between">
            <strong style="color:var(--primary);font-size:0.86rem;">${escapeHtml(loc(m, "senderName", "senderNameAr"))}</strong>
            ${m.courseTag ? `<span class="badge badge-tag">${escapeHtml(m.courseTag)}</span>` : ""}
          </div>
          <div style="white-space:pre-wrap;font-size:0.92rem;">${escapeHtml(loc(m, "text", "textAr"))}</div>
          ${m.imageDataUrl ? `<img src="${m.imageDataUrl}" alt="Attachment" style="width:100%;max-height:190px;object-fit:cover;border-radius:10px;"/>` : ""}
        </div>
      `).join("")}

      <div class="card">
        ${state.chatAttachmentDataUrl ? `<img src="${state.chatAttachmentDataUrl}" alt="Preview" style="width:72px;height:72px;object-fit:cover;border-radius:10px;"/>` : ""}
        <div class="row-wrap">
          ${channel === "direct" ? `
            <label class="btn btn-outline btn-sm" style="cursor:pointer;">
              📎<input type="file" accept="image/*" id="chat-photo-input" hidden />
            </label>
          ` : ""}
          <input id="chat-course-input" class="field" style="width:92px;" value="TM351" />
          <input id="chat-msg-input" class="field" style="flex:1;" placeholder="${escapeHtml(str.writeMessagePlaceholder)}" />
          <button class="btn btn-primary btn-sm" id="btn-chat-send">${escapeHtml(str.sendLabel)}</button>
        </div>
      </div>
    `;
  }

  function renderCalendarScreen(str) {
    return `
      <h2 class="section-title">${escapeHtml(str.calendarAlarm)}</h2>
      ${renderUrgentEmailBanner(str)}

      <div class="card">
        <div class="card-title">${escapeHtml(str.scheduleEventTitle)}</div>
        <div class="row-wrap">
          <input id="cal-course-input" class="field" style="width:104px;" placeholder="TM351" />
          <input id="cal-title-input" class="field" style="flex:1;" placeholder="${escapeHtml(str.eventTitlePlaceholder)}" />
        </div>
        <div class="row-wrap">
          <select id="cal-offset-select" class="field" style="flex:1;">
            <option value="1">${escapeHtml(str.in1Min)}</option>
            <option value="60" selected>${escapeHtml(str.in1Hour)}</option>
            <option value="1440">${escapeHtml(str.in1Day)}</option>
            <option value="4320">${escapeHtml(str.in3Days)}</option>
          </select>
          <button class="btn btn-primary" id="btn-add-cal-event">${escapeHtml(str.saveEvent)}</button>
        </div>
      </div>

      <div class="card">
        <div class="card-title">${escapeHtml(str.ringtoneVolumeTitle)}</div>
        <div class="row-scroll">
          ${D.RINGTONES.map((r) => `
            <button class="chip ${state.alarmRingtoneId === r.id ? "selected" : ""}" data-ringtone-id="${r.id}">${escapeHtml(loc(r, "label", "labelAr"))}</button>
          `).join("")}
        </div>
        <input type="range" id="alarm-vol-slider" min="0.1" max="1" step="0.05" value="${state.alarmVolume}" />
        <button class="btn btn-outline btn-sm" id="btn-preview-ringtone">${escapeHtml(str.previewRingtone)}</button>
      </div>

      ${state.calendarEvents.map((evt) => `
        <div class="card">
          <div class="row-between">
            <div style="min-width:0;flex:1;">
              <div class="card-title">${escapeHtml(loc(evt, "title", "titleAr"))}</div>
              <div class="muted">${escapeHtml(evt.courseCode)} • ${escapeHtml(formatDateTime(evt.eventTimeMillis))} • <span data-countdown-ms="${evt.eventTimeMillis}">${formatCountdown(evt.eventTimeMillis, str)}</span></div>
            </div>
            <div class="row-wrap">
              <button class="btn btn-outline btn-sm" data-ring-evt="${escapeHtml(evt.id)}" aria-label="${escapeHtml(str.previewRingtone)}">🔔</button>
              <button class="switch-btn ${evt.alarmEnabled ? "on" : ""}" data-toggle-alarm="${escapeHtml(evt.id)}" aria-label="${escapeHtml(str.calendarAlarm)}"><span class="switch-knob"></span></button>
              <button class="btn btn-outline btn-sm" data-del-evt="${escapeHtml(evt.id)}" aria-label="${escapeHtml(str.cancel)}">🗑</button>
            </div>
          </div>
        </div>
      `).join("")}
    `;
  }

  function renderMaterialsScreen(str, readOnly) {
    return `
      <h2 class="section-title">${escapeHtml(str.materials)}</h2>
      ${!readOnly ? `
        <div class="card">
          <div class="card-title">${escapeHtml(str.publishMaterialTitle)}</div>
          <div class="row-wrap">
            <input id="mat-course-input" class="field" style="width:105px;" value="TM351" />
            <input id="mat-title-input" class="field" style="flex:1;" placeholder="${escapeHtml(str.materialTitlePlaceholder)}" />
          </div>
          <input id="mat-desc-input" class="field" placeholder="${escapeHtml(str.notesSummaryPlaceholder)}" />
          <input id="mat-url-input" class="field" placeholder="https://lms.aou.edu.sa/..." />
          <button class="btn btn-primary btn-full" id="btn-publish-material">${escapeHtml(str.publishToCourse)}</button>
        </div>
      ` : ""}
      ${state.facultyMaterials.map((m) => `
        <div class="card">
          <div class="row-between">
            <span class="badge badge-tag">${escapeHtml(m.courseCode)} • ${escapeHtml(m.materialType.toUpperCase())}</span>
            ${!readOnly ? `<button class="btn btn-outline btn-sm" data-del-mat="${escapeHtml(m.id)}">🗑</button>` : ""}
          </div>
          <div class="card-title">${escapeHtml(loc(m, "title", "titleAr"))}</div>
          ${(m.description || m.descriptionAr) ? `<div class="muted">${escapeHtml(loc(m, "description", "descriptionAr"))}</div>` : ""}
          ${m.resourceUrl ? `<a class="btn btn-outline btn-sm" href="${escapeHtml(m.resourceUrl)}" target="_blank" rel="noopener noreferrer" style="text-decoration:none;">🔗 ${escapeHtml(m.resourceUrl)}</a>` : ""}
        </div>
      `).join("")}
    `;
  }

  function renderSettingsScreen(str) {
    const curLang = D.LANGUAGES.find((l) => l.code === state.languageCode) || D.LANGUAGES[0];
    const initials = (state.session && state.session.name ? state.session.name.slice(0, 2) : "AU").toUpperCase();

    return `
      <h2 class="section-title">${escapeHtml(str.settings)}</h2>
      <div class="card">
        <div class="row-wrap" style="gap:14px;">
          <div class="avatar-wrap">
            <div class="avatar-circle">
              ${state.profilePhotoDataUrl ? `<img src="${state.profilePhotoDataUrl}" alt="Profile"/>` : escapeHtml(initials)}
            </div>
            <label class="avatar-camera-btn">
              ${ICONS.camera}
              <input type="file" accept="image/*" id="profile-photo-input" hidden />
            </label>
          </div>
          <div style="min-width:0;flex:1;">
            <div class="card-title">${escapeHtml(state.session.name)}</div>
            <div class="muted">${escapeHtml(state.session.email)}</div>
            <div style="font-size:0.8rem;font-weight:700;color:var(--primary);">${escapeHtml(state.role === "instructor" ? str.instructorRole : str.studentRole)}</div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="row-between">
          <span style="font-weight:700;">${escapeHtml(str.darkMode)}</span>
          <button class="switch-btn ${state.darkMode ? "on" : ""}" id="btn-settings-dark"><span class="switch-knob"></span></button>
        </div>
        <div style="font-weight:700;font-size:0.9rem;">${escapeHtml(str.fontSize)}</div>
        <div class="row-scroll">
          ${[
            { s: 0.88, label: str.fontSmall },
            { s: 1, label: str.fontNormal },
            { s: 1.14, label: str.fontLarge },
            { s: 1.28, label: str.fontXLarge }
          ].map((opt) => `
            <button class="chip ${Math.abs(state.fontScale - opt.s) < 0.02 ? "selected" : ""}" data-font-scale="${opt.s}">${escapeHtml(opt.label)}</button>
          `).join("")}
        </div>
      </div>

      <div class="card">
        <button class="btn btn-outline btn-full" id="btn-settings-lang">🌐 ${escapeHtml(str.language)}: ${escapeHtml(curLang.nativeName)}</button>
        <div style="font-weight:700;font-size:0.92rem;">${escapeHtml(str.saudiBranchesTitle)}</div>
        <div class="branch-grid">
          ${D.SAUDI_BRANCHES.map((b) => `
            <button class="chip ${state.aouBranch === b ? "selected" : ""}" data-select-branch="${escapeHtml(b)}" style="justify-content:flex-start;overflow:hidden;text-overflow:ellipsis;">
              ${escapeHtml(formatBranchChip(b))}
            </button>
          `).join("")}
        </div>
        <div class="muted" style="font-size:0.78rem;">${escapeHtml(str.regionalBranchesTitle)}</div>
        <div class="row-scroll">
          ${D.REGIONAL_BRANCHES.map((b) => `
            <button class="chip ${state.aouBranch === b ? "selected" : ""}" data-select-branch="${escapeHtml(b)}">${escapeHtml(formatBranchChip(b))}</button>
          `).join("")}
        </div>
      </div>

      <button class="btn btn-outline btn-full" id="btn-open-install">📲 ${escapeHtml(str.installApp)}</button>
      <button class="btn btn-primary btn-full" id="btn-sign-out">${escapeHtml(str.signOut)}</button>
    `;
  }

  function renderStudentTools(str) {
    const rtl = D.isRtl(state.languageCode);
    if (state.studentSubTool !== "hub") {
      let inner = "";
      if (state.studentSubTool === "calendar") inner = renderCalendarScreen(str);
      else if (state.studentSubTool === "settings") inner = renderSettingsScreen(str);
      else if (state.studentSubTool === "materials") inner = renderMaterialsScreen(str, true);
      else if (state.studentSubTool === "study") {
        inner = `
          <h2 class="section-title">${escapeHtml(str.studyAssistant)}</h2>
          <div class="card">
            <div class="muted">${escapeHtml(str.studyAssistantIntro)}</div>
          </div>
          ${state.studyMessages.map((m) => `
            <div class="card" style="${m.user ? "background:var(--primary-container);color:var(--on-primary-container);" : ""}">
              <div style="white-space:pre-wrap;">${escapeHtml(m.text)}</div>
            </div>
          `).join("")}
          <div class="row-wrap">
            <input id="study-ask-input" class="field" style="flex:1;" placeholder="${escapeHtml(str.askStudyQuestion)}" />
            <button class="btn btn-primary btn-sm" id="btn-study-ask">${escapeHtml(str.askBtn)}</button>
          </div>
        `;
      } else if (state.studentSubTool === "qa") {
        const term = (state.freshmanSearch || "").toLowerCase();
        const filtered = D.FRESHMEN_QA.filter((q) => !term ||
          q.q.toLowerCase().includes(term) ||
          q.a.toLowerCase().includes(term) ||
          (q.qAr || "").includes(state.freshmanSearch) ||
          (q.aAr || "").includes(state.freshmanSearch)
        );
        inner = `
          <h2 class="section-title">${escapeHtml(str.freshmenQa)}</h2>
          <input id="qa-search-input" class="field" value="${escapeHtml(state.freshmanSearch)}" placeholder="${escapeHtml(str.searchQaPlaceholder)}" />
          ${filtered.map((q) => `
            <div class="card">
              <span class="badge badge-tag">${escapeHtml(loc(q, "category", "categoryAr"))}</span>
              <div class="card-title">${escapeHtml(loc(q, "q", "qAr"))}</div>
              <div class="muted">${escapeHtml(loc(q, "a", "aAr"))}</div>
            </div>
          `).join("")}
        `;
      } else if (state.studentSubTool === "voice") {
        inner = `
          <h2 class="section-title">${escapeHtml(str.voiceNotes)}</h2>
          <div class="card">
            <input id="voice-title-input" class="field" placeholder="${escapeHtml(str.voiceTitlePlaceholder)}" />
            <button class="btn ${state.recordingVoice ? "btn-danger-outline" : "btn-primary"} btn-full" id="btn-voice-toggle">
              ${state.recordingVoice ? `⏹ ${escapeHtml(str.stopSaveRecording)}` : `🎙 ${escapeHtml(str.recordVoiceNote)}`}
            </button>
          </div>
          ${state.voiceNotes.map((v) => `
            <div class="card">
              <div class="row-between">
                <div><strong>${escapeHtml(v.title)}</strong><div class="muted">${v.durationSec}s</div></div>
                <button class="btn btn-tonal btn-sm" data-play-voice="${escapeHtml(v.id)}">${state.playingVoiceId === v.id ? "⏸" : "▶"}</button>
              </div>
            </div>
          `).join("")}
        `;
      } else if (state.studentSubTool === "notes") {
        inner = `
          <h2 class="section-title">${escapeHtml(str.personalNotes)}</h2>
          <div class="card">
            <div class="row-wrap">
              <input id="note-course-input" class="field" style="width:105px;" placeholder="TM351" />
              <input id="note-title-input" class="field" style="flex:1;" placeholder="${escapeHtml(str.noteTitlePlaceholder)}" />
            </div>
            <textarea id="note-body-input" class="field" placeholder="${escapeHtml(str.writeNotesPlaceholder)}"></textarea>
            <input id="note-url-input" class="field" placeholder="${escapeHtml(str.optionalLinkPlaceholder)}" />
            <button class="btn btn-primary btn-full" id="btn-save-note">${escapeHtml(str.saveNote)}</button>
          </div>
          ${state.manualNotes.map((n) => `
            <div class="card">
              <div class="row-between">
                <div class="card-title">${escapeHtml(n.title)} ${n.course ? `• ${escapeHtml(n.course)}` : ""}</div>
                <button class="btn btn-outline btn-sm" data-del-note="${escapeHtml(n.id)}">🗑</button>
              </div>
              <div style="white-space:pre-wrap;font-size:0.9rem;">${escapeHtml(n.body)}</div>
              ${n.url ? `<a href="${escapeHtml(n.url)}" target="_blank" rel="noopener noreferrer">${escapeHtml(n.url)}</a>` : ""}
            </div>
          `).join("")}
        `;
      }
      return `
        <button class="btn btn-outline btn-sm" id="btn-back-tools" style="align-self:flex-start;">${rtl ? "→" : "←"} ${escapeHtml(str.backToTools)}</button>
        ${inner}
      `;
    }

    const items = [
      { id: "calendar", label: str.calendarAlarm, icon: "📅" },
      { id: "study", label: str.studyAssistant, icon: "✨" },
      { id: "qa", label: str.freshmenQa, icon: "❓" },
      { id: "voice", label: str.voiceNotes, icon: "🎙" },
      { id: "notes", label: str.personalNotes, icon: "📝" },
      { id: "materials", label: str.facultyMaterials, icon: "📂" },
      { id: "settings", label: str.settings, icon: "⚙️" }
    ];

    return `
      <h2 class="section-title">${escapeHtml(str.tools)}</h2>
      ${items.map((it) => `
        <div class="card clickable" data-open-subtool="${it.id}">
          <div class="row-wrap">
            <span style="font-size:1.15rem;">${it.icon}</span>
            <span style="font-weight:700;">${escapeHtml(it.label)}</span>
          </div>
        </div>
      `).join("")}
    `;
  }

  // --- Modals & Overlays ---
  function renderModals(str) {
    const ar = isArabic();
    let html = "";

    if (state.unifiedReplyClusterId) {
      const cluster = D.REPEATED_QUESTIONS.find((c) => c.id === state.unifiedReplyClusterId);
      if (cluster) {
        html += `
          <div class="modal-backdrop">
            <div class="modal-panel">
              <div class="card-title">${escapeHtml(str.unifiedReplyAll)} (${escapeHtml(cluster.courseCode)})</div>
              <div class="muted">${cluster.askCount} ${escapeHtml(str.studentsCountLabel)} • ${escapeHtml(loc(cluster, "topic", "topicAr"))}</div>
              <textarea id="unified-reply-textarea" class="field" rows="5">${escapeHtml(state.unifiedReplyText)}</textarea>
              <div class="row-between">
                <button class="btn btn-outline" id="btn-cancel-unified">${escapeHtml(str.cancel)}</button>
                <button class="btn btn-primary" id="btn-confirm-unified">${escapeHtml(str.sendReply)}</button>
              </div>
            </div>
          </div>
        `;
      }
    }

    if (state.showComposeModal) {
      html += `
        <div class="modal-backdrop">
          <div class="modal-panel">
            <div class="card-title">${escapeHtml(str.compose)}</div>
            <input id="comp-to" class="field" type="email" placeholder="${escapeHtml(str.recipientEmail)}" />
            <input id="comp-subj" class="field" placeholder="${escapeHtml(str.subject)}" />
            <textarea id="comp-body" class="field" rows="4" placeholder="${escapeHtml(str.message)}"></textarea>
            <div class="row-between">
              <button class="btn btn-outline" id="btn-cancel-compose">${escapeHtml(str.cancel)}</button>
              <button class="btn btn-primary" id="btn-send-compose">${escapeHtml(str.sendReply)}</button>
            </div>
          </div>
        </div>
      `;
    }

    if (state.selectedCourseCode) {
      const major = D.MAJORS.find((m) => m.id === state.selectedMajorId) || D.MAJORS[0];
      const course = major.courses.find((c) => c.code === state.selectedCourseCode);
      if (course) {
        const curStatus = state.courseStatuses[course.code] || "not_started";
        html += `
          <div class="modal-backdrop" id="course-modal-backdrop">
            <div class="modal-panel">
              <div class="card-title">${escapeHtml(course.code)} — ${escapeHtml(loc(course, "title", "titleAr"))}</div>
              <div class="muted">${course.credits} ${escapeHtml(str.creditsShort)} • ${escapeHtml(str.stageLabel)} ${course.stage}</div>
              <div class="muted">${escapeHtml(str.prerequisitesLabel)} ${course.prerequisites.length ? escapeHtml(course.prerequisites.join(", ")) : escapeHtml(str.noneLabel)}</div>
              <div class="branch-grid">
                ${D.COURSE_STATUSES.map((opt) => `
                  <button class="chip ${curStatus === opt.key ? "selected" : ""}" data-modal-course-status="${opt.key}" data-course-code="${escapeHtml(course.code)}"
                    style="${curStatus === opt.key ? `border-color:${opt.color};background:${opt.color}22;color:${opt.color};` : ""}">
                    ${escapeHtml(ar ? opt.labelAr : opt.labelEn)}
                  </button>
                `).join("")}
              </div>
              <button class="btn btn-outline btn-full" id="btn-close-course-modal">${escapeHtml(str.close)}</button>
            </div>
          </div>
        `;
      }
    }

    if (state.showLangModal) {
      html += `
        <div class="modal-backdrop">
          <div class="modal-panel">
            <div class="card-title">${escapeHtml(str.language)}</div>
            <div style="display:flex;flex-direction:column;gap:6px;max-height:56dvh;overflow-y:auto;">
              ${D.LANGUAGES.map((l) => `
                <button class="drawer-item ${state.languageCode === l.code ? "active" : ""}" data-choose-lang="${l.code}">
                  <span>${escapeHtml(l.nativeName)} (${escapeHtml(l.englishName)})</span>
                  <span>${state.languageCode === l.code ? "✓" : ""}</span>
                </button>
              `).join("")}
            </div>
            <button class="btn btn-outline btn-full" id="btn-close-lang">${escapeHtml(str.close)}</button>
          </div>
        </div>
      `;
    }

    if (state.showInstallModal) {
      html += `
        <div class="modal-backdrop">
          <div class="modal-panel">
            <div class="row-wrap">
              ${ICON_SVG_BADGE}
              <div class="card-title">${escapeHtml(str.installApp)}</div>
            </div>
            <div class="card" style="background:var(--bg-canvas);">
              <strong>${escapeHtml(str.installIosTitle)}</strong>
              <div class="muted">${str.installIosSteps}</div>
            </div>
            <div class="card" style="background:var(--bg-canvas);">
              <strong>${escapeHtml(str.installDesktopTitle)}</strong>
              <div class="muted">${str.installDesktopSteps}</div>
              ${state.deferredInstallPrompt ? `<button class="btn btn-primary btn-sm" id="btn-trigger-pwa-install">${escapeHtml(str.installNowBtn)}</button>` : ""}
            </div>
            <button class="btn btn-outline btn-full" id="btn-close-install">${escapeHtml(str.close)}</button>
          </div>
        </div>
      `;
    }

    if (state.milestoneAward) {
      html += `
        <div class="modal-backdrop">
          <div class="modal-panel" style="text-align:center;">
            <div style="font-size:2rem;">🏆</div>
            <div class="card-title">${escapeHtml(str.milestoneAchieved.replace("{m}", String(state.milestoneAward)))}</div>
            <button class="btn btn-primary btn-full" id="btn-dismiss-milestone">${escapeHtml(str.continueLabel)}</button>
          </div>
        </div>
      `;
    }

    if (state.ringingEvent) {
      html += `
        <div class="alarm-overlay">
          <div class="modal-panel" style="text-align:center;">
            <div style="font-size:2.4rem;">🔔</div>
            <div class="badge badge-tag" style="align-self:center;">${escapeHtml(state.ringingEvent.courseCode)}</div>
            <div class="card-title" style="font-size:1.3rem;">${escapeHtml(loc(state.ringingEvent, "title", "titleAr"))}</div>
            <button class="btn btn-tonal btn-full" id="btn-alarm-snooze">${escapeHtml(str.snooze5Min)}</button>
            <button class="btn btn-primary btn-full" id="btn-alarm-dismiss">${escapeHtml(str.dismissAlarm)}</button>
          </div>
        </div>
      `;
    }

    return html;
  }

  function renderSnackbar() {
    let el = document.getElementById("unisync-snackbar");
    if (!state.snackbar) {
      if (el) el.remove();
      return;
    }
    if (!el) {
      el = document.createElement("div");
      el.id = "unisync-snackbar";
      el.className = "snackbar";
      document.body.appendChild(el);
    }
    el.textContent = state.snackbar;
  }

  // --- SVG Prerequisite Arrows for Roadmap ---
  function drawRoadmapArrows() {
    const svg = document.getElementById("roadmap-arrows-svg");
    const canvas = document.getElementById("roadmap-canvas");
    if (!svg || !canvas) return;
    const major = D.MAJORS.find((m) => m.id === state.selectedMajorId) || D.MAJORS[0];
    const nodes = {};
    canvas.querySelectorAll("[data-course-node]").forEach((el) => {
      nodes[el.getAttribute("data-course-node")] = {
        x: el.offsetLeft + el.offsetWidth / 2,
        top: el.offsetTop,
        bottom: el.offsetTop + el.offsetHeight
      };
    });

    const cRectW = canvas.scrollWidth;
    const cRectH = canvas.scrollHeight;
    svg.setAttribute("width", String(cRectW));
    svg.setAttribute("height", String(cRectH));

    let paths = "";
    major.courses.forEach((course) => {
      const dst = nodes[course.code];
      if (!dst) return;
      course.prerequisites.forEach((preCode) => {
        const src = nodes[preCode];
        if (!src) return;
        const done = state.courseStatuses[preCode] === "completed";
        const stroke = done ? "#1F6E5A" : "#64748B";
        const x1 = src.x;
        const y1 = src.bottom;
        const x2 = dst.x;
        const y2 = dst.top;
        const midY = (y1 + y2) / 2;
        paths += `<path d="M ${x1} ${y1} C ${x1} ${midY}, ${x2} ${midY}, ${x2} ${y2}" fill="none" stroke="${stroke}" stroke-opacity="${done ? 0.78 : 0.42}" stroke-width="2.2" />`;
        paths += `<polygon points="${x2 - 4},${y2 - 7} ${x2 + 4},${y2 - 7} ${x2},${y2}" fill="${stroke}" fill-opacity="${done ? 0.85 : 0.55}" />`;
      });
    });
    svg.innerHTML = paths;
  }

  // --- Main Render ---
  function render() {
    applyThemeAndDirection();
    const root = document.getElementById("app");
    const str = D.getStrings(state.languageCode);

    if (!state.session) {
      root.innerHTML = renderLoginScreen(str) + renderModals(str);
      bindEvents();
      return;
    }

    const locked = isFocusLocked();
    const navItems = state.role === "student"
      ? [
          { key: "home", label: str.home, icon: ICONS.home },
          { key: "inbox", label: str.inbox, icon: ICONS.inbox },
          { key: "pomodoro", label: str.pomodoro, icon: ICONS.timer },
          { key: "chat", label: str.chat, icon: ICONS.chat },
          { key: "roadmaps", label: str.roadmaps, icon: ICONS.map },
          { key: "tools", label: str.tools, icon: ICONS.tools }
        ]
      : [
          { key: "home", label: str.home, icon: ICONS.home },
          { key: "inbox", label: str.inbox, icon: ICONS.inbox },
          { key: "calendar", label: str.calendarAlarm, icon: ICONS.calendar },
          { key: "materials", label: str.materials, icon: ICONS.materials },
          { key: "chat", label: str.chat, icon: ICONS.chat },
          { key: "settings", label: str.settings, icon: ICONS.settings }
        ];

    const activeKey = state.showRepliedReviewed
      ? ""
      : state.role === "student"
        ? state.studentTab
        : state.instructorTab;

    const historyCount =
      (state.role === "instructor" ? state.answeredQuestionIds.length : 0) +
      state.emails.filter((e) => e.replied || e.viewed).length;

    let bodyHtml = "";
    if (state.showRepliedReviewed && !locked) {
      bodyHtml = renderRepliedAndReviewed(str);
    } else if (state.role === "student") {
      if (state.studentTab === "home") bodyHtml = renderStudentHome(str);
      else if (state.studentTab === "inbox") bodyHtml = renderInboxScreen(str);
      else if (state.studentTab === "pomodoro") bodyHtml = renderPomodoroScreen(str);
      else if (state.studentTab === "chat") bodyHtml = renderChatScreen(str);
      else if (state.studentTab === "roadmaps") bodyHtml = renderRoadmapsScreen(str);
      else bodyHtml = renderStudentTools(str);
    } else {
      if (state.instructorTab === "home") bodyHtml = renderInstructorHome(str);
      else if (state.instructorTab === "inbox") bodyHtml = renderInboxScreen(str);
      else if (state.instructorTab === "calendar") bodyHtml = renderCalendarScreen(str);
      else if (state.instructorTab === "materials") bodyHtml = renderMaterialsScreen(str, false);
      else if (state.instructorTab === "chat") bodyHtml = renderChatScreen(str);
      else bodyHtml = renderSettingsScreen(str);
    }

    const drawerHtml = state.drawerOpen && !locked ? `
      <div class="drawer-backdrop" id="drawer-backdrop">
        <aside class="drawer-sheet" role="navigation" aria-label="${escapeHtml(str.menu)}">
          <div class="drawer-header">
            ${ICON_SVG_BADGE}
            <div style="min-width:0;">
              <div style="font-weight:900;font-size:1.05rem;">UniSync</div>
              <div class="muted" style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${escapeHtml(state.session.name)}</div>
              <div style="font-size:0.75rem;font-weight:700;color:var(--primary);">${escapeHtml(state.role === "instructor" ? str.instructorRole : str.studentRole)}</div>
            </div>
          </div>

          <button class="drawer-item ${state.showRepliedReviewed ? "active" : ""}" id="btn-drawer-history">
            <span class="drawer-item-left">${ICONS.history}<span>${escapeHtml(str.repliedReviewed)}</span></span>
            ${historyCount > 0 ? `<span class="drawer-count">${historyCount}</span>` : ""}
          </button>

          <div class="divider"></div>

          ${navItems.map((it) => `
            <button class="drawer-item ${activeKey === it.key ? "active" : ""}" data-nav-key="${it.key}">
              <span class="drawer-item-left">${it.icon}<span>${escapeHtml(it.label)}</span></span>
            </button>
          `).join("")}

          <div class="divider"></div>

          <button class="drawer-item" id="btn-drawer-lang">
            <span class="drawer-item-left">🌐 <span>${escapeHtml(str.language)}</span></span>
          </button>

          <button class="drawer-item" id="btn-drawer-signout">
            <span class="drawer-item-left">${ICONS.signOut}<span>${escapeHtml(str.signOut)}</span></span>
          </button>
        </aside>
      </div>
    ` : "";

    root.innerHTML = `
      ${!locked ? `
        <header class="topbar">
          <div class="topbar-start">
            <button class="icon-btn" id="btn-open-drawer" aria-label="${escapeHtml(str.menu)}">${ICONS.menu}</button>
            ${ICON_SVG_BADGE}
            <h1 class="topbar-title">UniSync</h1>
          </div>
          <div class="topbar-actions">
            <button class="icon-btn" id="btn-top-lang" aria-label="${escapeHtml(str.language)}">🌐</button>
            <button class="icon-btn" id="btn-top-dark" aria-label="${escapeHtml(str.darkMode)}">${state.darkMode ? ICONS.light : ICONS.dark}</button>
            ${state.role === "student" ? `<button class="icon-btn" id="btn-top-settings" aria-label="${escapeHtml(str.settings)}">${ICONS.settings}</button>` : ""}
          </div>
        </header>
      ` : ""}

      <div class="workspace-body">
        ${!locked ? `
          <nav class="nav-rail" aria-label="${escapeHtml(str.menu)}">
            ${navItems.map((it) => `
              <button class="nav-item ${activeKey === it.key ? "active" : ""}" data-nav-key="${it.key}">
                <span class="nav-pill">${it.icon}</span>
                <span class="nav-label">${escapeHtml(it.label)}</span>
              </button>
            `).join("")}
          </nav>
        ` : ""}

        <main class="main-scroll ${locked ? "focus-locked" : ""}">
          ${bodyHtml}
        </main>
      </div>

      ${!locked ? `
        <nav class="bottom-nav" aria-label="${escapeHtml(str.menu)}">
          ${navItems.map((it) => `
            <button class="nav-item ${activeKey === it.key ? "active" : ""}" data-nav-key="${it.key}">
              <span class="nav-pill">${it.icon}</span>
              <span class="nav-label">${escapeHtml(it.label)}</span>
            </button>
          `).join("")}
        </nav>
      ` : ""}

      ${drawerHtml}
      ${renderModals(str)}
    `;

    bindEvents();
    if (state.role === "student" && state.studentTab === "roadmaps" && !state.showRepliedReviewed) {
      requestAnimationFrame(drawRoadmapArrows);
    }
    renderSnackbar();
  }

  // --- Event Binding ---
  function bindEvents() {
    const str = D.getStrings(state.languageCode);
    const on = (id, ev, fn) => {
      const el = document.getElementById(id);
      if (el) el.addEventListener(ev, fn);
    };

    // Login screen
    document.querySelectorAll("[data-select-role]").forEach((btn) => {
      btn.addEventListener("click", () => {
        state.role = btn.getAttribute("data-select-role");
        state.emails = D.getSampleEmails(state.role);
        state.calendarEvents = D.getDefaultCalendarEvents(state.role);
        persist();
        render();
      });
    });

    on("login-lang-btn", "click", () => { state.showLangModal = true; render(); });
    on("login-dark-btn", "click", () => { state.darkMode = !state.darkMode; persist(); render(); });
    on("btn-ms-login", "click", startMicrosoftLogin);
    on("btn-academic-login", "click", () => {
      const nameVal = (document.getElementById("academic-name-input").value || "").trim();
      const emailVal = (document.getElementById("academic-email-input").value || "").trim();
      if (!emailVal || !emailVal.includes("@")) {
        showToast(str.toastInvalidEmail);
        return;
      }
      state.session = {
        mode: "academic",
        name: nameVal || emailVal.split("@")[0],
        email: emailVal
      };
      state.emails = D.getSampleEmails(state.role);
      state.calendarEvents = D.getDefaultCalendarEvents(state.role);
      persist();
      render();
    });
    on("btn-demo-login", "click", () => {
      const ar = isArabic();
      state.session = {
        mode: "demo",
        name: state.role === "instructor" ? (ar ? "د. طارق المنصور" : "Dr. Tariq Al-Mansoor") : (ar ? "سُهيلة أحمد" : "Sohaila Ahmad"),
        email: state.role === "instructor" ? "t.almansoor@aou.edu.sa" : "2341098@aou.edu.sa"
      };
      state.emails = D.getSampleEmails(state.role);
      state.calendarEvents = D.getDefaultCalendarEvents(state.role);
      persist();
      render();
    });

    // Global navigation & Drawer
    on("btn-open-drawer", "click", () => { state.drawerOpen = true; render(); });
    on("drawer-backdrop", "click", (e) => { if (e.target.id === "drawer-backdrop") { state.drawerOpen = false; render(); } });
    on("btn-drawer-history", "click", () => { state.showRepliedReviewed = true; state.drawerOpen = false; render(); });
    on("btn-drawer-lang", "click", () => { state.drawerOpen = false; state.showLangModal = true; render(); });
    on("btn-close-history", "click", () => { state.showRepliedReviewed = false; render(); });
    on("btn-drawer-signout", "click", signOut);
    on("btn-sign-out", "click", signOut);
    on("btn-top-lang", "click", () => { state.showLangModal = true; render(); });
    on("btn-top-dark", "click", () => { state.darkMode = !state.darkMode; persist(); render(); });
    on("btn-top-settings", "click", () => {
      state.showRepliedReviewed = false;
      state.studentTab = "tools";
      state.studentSubTool = "settings";
      render();
    });

    document.querySelectorAll("[data-nav-key]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const key = btn.getAttribute("data-nav-key");
        state.showRepliedReviewed = false;
        state.drawerOpen = false;
        if (state.role === "student") {
          state.studentTab = key;
          if (key === "tools") state.studentSubTool = "hub";
        } else {
          state.instructorTab = key;
        }
        render();
      });
    });

    // Quick Jumps
    on("btn-open-next-cal", "click", () => {
      if (state.role === "student") { state.studentTab = "tools"; state.studentSubTool = "calendar"; }
      else state.instructorTab = "calendar";
      render();
    });
    on("btn-manage-cal", "click", () => { state.studentTab = "tools"; state.studentSubTool = "calendar"; render(); });
    on("btn-ins-open-inbox", "click", () => { state.instructorTab = "inbox"; render(); });

    // Urgent Banner & Inbox Items
    document.querySelectorAll("[data-open-email]").forEach((el) => {
      el.addEventListener("click", () => {
        const id = el.getAttribute("data-open-email");
        state.showRepliedReviewed = false;
        if (state.role === "student") state.studentTab = "inbox";
        else state.instructorTab = "inbox";
        state.selectedEmailId = id;
        state.showOriginalEmail = false;
        state.emailSummary = "";
        state.smartReplyDraft = "";
        state.emails = state.emails.map((m) => (m.id === id ? Object.assign({}, m, { unread: false, viewed: true }) : m));
        persist();
        render();
      });
    });

    // Inbox Controls & Full Email Reader
    document.querySelectorAll("[data-prio-filter]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const p = btn.getAttribute("data-prio-filter");
        state.priorityFilter = p === "ALL" ? null : p;
        render();
      });
    });
    on("btn-toggle-sort", "click", () => { state.sortByPriority = !state.sortByPriority; render(); });
    on("btn-back-inbox", "click", () => { state.selectedEmailId = null; render(); });
    on("btn-view-adapted", "click", () => { state.showOriginalEmail = false; render(); });
    on("btn-view-original", "click", () => { state.showOriginalEmail = true; render(); });
    on("btn-summarize-email", "click", () => {
      const email = state.emails.find((e) => e.id === state.selectedEmailId);
      if (email) { state.emailSummary = D.summarizeEmail(email, state.languageCode); render(); }
    });
    on("btn-extract-dates", "click", () => {
      const email = state.emails.find((e) => e.id === state.selectedEmailId);
      if (!email) return;
      const items = D.extractDates(email, state.languageCode);
      items.forEach((it, idx) => {
        state.calendarEvents.push({
          id: "cal_" + Date.now() + "_" + idx,
          title: `${it.title} (${it.dateLabel})`,
          courseCode: it.courseCode,
          category: it.category,
          eventTimeMillis: Date.now() + (idx + 1) * 36 * 3600000,
          alarmEnabled: true
        });
      });
      persist();
      showToast(str.toastDatesAdded);
    });
    document.querySelectorAll("[data-tone-reply]").forEach((btn) => {
      btn.addEventListener("click", () => { state.replyTone = btn.getAttribute("data-tone-reply"); render(); });
    });
    on("btn-gen-smart-reply", "click", () => {
      const email = state.emails.find((e) => e.id === state.selectedEmailId);
      if (email) {
        state.smartReplyDraft = D.generateSmartReply(email, state.replyTone, state.role, state.session.name, state.languageCode);
        render();
      }
    });
    on("email-reply-box", "input", (e) => { state.smartReplyDraft = e.target.value; });
    on("btn-send-email-reply", "click", async () => {
      const email = state.emails.find((e) => e.id === state.selectedEmailId);
      const box = document.getElementById("email-reply-box");
      const text = (box ? box.value : state.smartReplyDraft).trim();
      if (!email || !text) return;

      if (email.fromGraph && state.msAccessToken) {
        try {
          await fetch(`https://graph.microsoft.com/v1.0/me/messages/${email.id}/reply`, {
            method: "POST",
            headers: {
              Authorization: `Bearer ${state.msAccessToken}`,
              "Content-Type": "application/json"
            },
            body: JSON.stringify({ comment: text })
          });
        } catch (_) {}
      }

      state.emails = state.emails.map((e) => (e.id === email.id ? Object.assign({}, e, { replied: true, unread: false, viewed: true }) : e));
      state.sentEmailReplies[email.id] = text;
      state.smartReplyDraft = "";
      persist();
      render();
      showToast(`${str.toastReplySent} ${loc(email, "senderName", "senderNameAr")}`);
    });

    // Compose Modal
    on("btn-open-compose", "click", () => { state.showComposeModal = true; render(); });
    on("btn-cancel-compose", "click", () => { state.showComposeModal = false; render(); });
    on("btn-send-compose", "click", async () => {
      const to = (document.getElementById("comp-to").value || "").trim();
      const subj = (document.getElementById("comp-subj").value || "").trim();
      const body = (document.getElementById("comp-body").value || "").trim();
      if (!to || !subj || !body) return;
      if (state.msAccessToken) {
        try {
          await fetch("https://graph.microsoft.com/v1.0/me/sendMail", {
            method: "POST",
            headers: {
              Authorization: `Bearer ${state.msAccessToken}`,
              "Content-Type": "application/json"
            },
            body: JSON.stringify({
              message: { subject: subj, body: { contentType: "Text", content: body }, toRecipients: [{ emailAddress: { address: to } }] }
            })
          });
        } catch (_) {}
      }
      state.emails.unshift({
        id: "sent_" + Date.now(),
        senderName: str.youSent,
        senderNameAr: str.youSent,
        senderEmail: state.session.email,
        recipientEmail: to,
        subject: subj,
        fullBody: body,
        receivedAtLabel: str.justNow,
        receivedAtAr: str.justNow,
        timestampMillis: Date.now(),
        priority: "NORMAL",
        courseTag: "OUTBOX",
        unread: false,
        viewed: true,
        replied: true
      });
      state.showComposeModal = false;
      persist();
      render();
      showToast(`${str.toastEmailSent} ${to}`);
    });

    // Instructor Unified Reply (Hides cluster immediately & adds to Replied & Reviewed)
    on("btn-open-unified-reply", "click", (e) => {
      const cid = e.currentTarget.getAttribute("data-cluster-id");
      const cluster = D.REPEATED_QUESTIONS.find((c) => c.id === cid);
      if (!cluster) return;
      state.unifiedReplyClusterId = cid;
      state.unifiedReplyText = isArabic()
        ? `أعزائي طلاب مقرر ${cluster.courseCode}،\n\nبخصوص «${cluster.topicAr}»: يُرجى الالتزام بالدليل المحدّث للمقرر وتعليمات التسليم على نظام إدارة التعلم (LMS).\n\nمع أطيب التحيات،\nعضو هيئة التدريس`
        : `Dear ${cluster.courseCode} Students,\n\nRegarding "${cluster.topic}": Please follow the updated course guide and submission instructions on the AOU LMS.\n\nBest regards,\nCourse Instructor`;
      render();
    });
    on("btn-cancel-unified", "click", () => { state.unifiedReplyClusterId = null; render(); });
    on("btn-confirm-unified", "click", () => {
      const cluster = D.REPEATED_QUESTIONS.find((c) => c.id === state.unifiedReplyClusterId);
      const txt = (document.getElementById("unified-reply-textarea").value || "").trim();
      if (!cluster || !txt) return;
      if (!state.answeredQuestionIds.includes(cluster.id)) state.answeredQuestionIds.push(cluster.id);
      state.answeredQuestionReplies[cluster.id] = txt;
      state.emails = state.emails.map((e) =>
        e.courseTag === cluster.courseCode ? Object.assign({}, e, { replied: true, unread: false, viewed: true }) : e
      );
      state.chatMessages.push({
        id: "chat_" + Date.now(),
        senderName: state.session.name,
        channel: "direct",
        courseTag: cluster.courseCode,
        text: `[${cluster.courseCode}] ${txt}`
      });
      state.unifiedReplyClusterId = null;
      persist();
      render();
      showToast(`${str.toastUnifiedSent} (${cluster.askCount})`);
    });

    // Roadmaps Pan/Zoom + Course Status
    document.querySelectorAll("[data-select-major]").forEach((btn) => {
      btn.addEventListener("click", () => {
        state.selectedMajorId = btn.getAttribute("data-select-major");
        state.roadmapPanX = 0;
        state.roadmapPanY = 0;
        state.roadmapZoom = 1;
        persist();
        render();
      });
    });
    on("btn-zoom-in", "click", () => { state.roadmapZoom = Math.min(2, +(state.roadmapZoom + 0.15).toFixed(2)); render(); });
    on("btn-zoom-out", "click", () => { state.roadmapZoom = Math.max(0.45, +(state.roadmapZoom - 0.15).toFixed(2)); render(); });
    on("btn-zoom-reset", "click", () => { state.roadmapZoom = 1; state.roadmapPanX = 0; state.roadmapPanY = 0; render(); });

    const viewport = document.getElementById("roadmap-viewport");
    const canvas = document.getElementById("roadmap-canvas");
    if (viewport && canvas) {
      let dragging = false;
      let startX = 0, startY = 0;
      viewport.addEventListener("pointerdown", (ev) => {
        if (ev.target.closest("button")) return;
        dragging = true;
        startX = ev.clientX - state.roadmapPanX;
        startY = ev.clientY - state.roadmapPanY;
      });
      window.addEventListener("pointermove", (ev) => {
        if (!dragging) return;
        state.roadmapPanX = ev.clientX - startX;
        state.roadmapPanY = ev.clientY - startY;
        canvas.style.transform = `translate(${state.roadmapPanX}px, ${state.roadmapPanY}px) scale(${state.roadmapZoom})`;
      });
      window.addEventListener("pointerup", () => { dragging = false; });
    }

    document.querySelectorAll("[data-set-course]").forEach((btn) => {
      btn.addEventListener("click", (ev) => {
        ev.stopPropagation();
        const code = btn.getAttribute("data-set-course");
        const st = btn.getAttribute("data-status");
        state.courseStatuses[code] = st;
        persist();
        render();
      });
    });
    document.querySelectorAll("[data-course-node]").forEach((node) => {
      node.addEventListener("click", (ev) => {
        if (ev.target.closest("button")) return;
        state.selectedCourseCode = node.getAttribute("data-course-node");
        render();
      });
    });
    document.querySelectorAll("[data-modal-course-status]").forEach((btn) => {
      btn.addEventListener("click", () => {
        state.courseStatuses[btn.getAttribute("data-course-code")] = btn.getAttribute("data-modal-course-status");
        persist();
        render();
      });
    });
    on("btn-close-course-modal", "click", () => { state.selectedCourseCode = null; render(); });

    // Pomodoro Controls
    document.querySelectorAll("[data-companion]").forEach((btn) => {
      btn.addEventListener("click", () => { state.companionGender = btn.getAttribute("data-companion"); persist(); render(); });
    });
    document.querySelectorAll("[data-pomo-dur]").forEach((btn) => {
      btn.addEventListener("click", () => {
        if (state.pomodoroRunning) return;
        const d = Number(btn.getAttribute("data-pomo-dur"));
        state.pomodoroDuration = d;
        state.pomodoroSeconds = d * 60;
        state.pomodoroIsBreak = false;
        render();
      });
    });
    on("btn-pomo-start", "click", () => {
      state.pomodoroIsBreak = false;
      state.pomodoroSeconds = state.pomodoroDuration * 60;
      state.pomodoroRunning = true;
      engageFocusLock();
      render();
    });
    on("btn-pomo-break", "click", () => {
      state.pomodoroIsBreak = true;
      state.pomodoroSeconds = 5 * 60;
      state.pomodoroRunning = true;
      render();
    });
    on("btn-pomo-exit", "click", () => {
      state.pomodoroRunning = false;
      state.pomodoroIsBreak = false;
      state.pomodoroSeconds = state.pomodoroDuration * 60;
      releaseFocusLock();
      render();
    });
    on("btn-dismiss-milestone", "click", () => { state.milestoneAward = null; render(); });

    // Calendar & Alarm Controls
    on("btn-add-cal-event", "click", () => {
      const title = (document.getElementById("cal-title-input").value || "").trim();
      const course = (document.getElementById("cal-course-input").value || "AOU").trim().toUpperCase();
      const offsetMin = Number(document.getElementById("cal-offset-select").value || 60);
      if (!title) return;
      state.calendarEvents.push({
        id: "cal_" + Date.now(),
        title,
        courseCode: course,
        category: "assignment",
        eventTimeMillis: Date.now() + offsetMin * 60000,
        alarmEnabled: true
      });
      state.calendarEvents.sort((a, b) => a.eventTimeMillis - b.eventTimeMillis);
      persist();
      render();
      showToast(str.toastEventSaved);
    });
    document.querySelectorAll("[data-toggle-alarm]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const id = btn.getAttribute("data-toggle-alarm");
        state.calendarEvents = state.calendarEvents.map((e) => (e.id === id ? Object.assign({}, e, { alarmEnabled: !e.alarmEnabled }) : e));
        persist();
        render();
      });
    });
    document.querySelectorAll("[data-ring-evt]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const id = btn.getAttribute("data-ring-evt");
        const evt = state.calendarEvents.find((e) => e.id === id);
        if (evt) startAlarmLoop(evt);
      });
    });
    document.querySelectorAll("[data-del-evt]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const id = btn.getAttribute("data-del-evt");
        state.calendarEvents = state.calendarEvents.filter((e) => e.id !== id);
        persist();
        render();
      });
    });
    document.querySelectorAll("[data-ringtone-id]").forEach((btn) => {
      btn.addEventListener("click", () => { state.alarmRingtoneId = btn.getAttribute("data-ringtone-id"); persist(); render(); });
    });
    on("alarm-vol-slider", "input", (e) => { state.alarmVolume = Number(e.target.value); persist(); });
    on("btn-preview-ringtone", "click", () => playToneOnce(state.alarmRingtoneId, state.alarmVolume));
    on("btn-alarm-dismiss", "click", () => { stopAlarmLoop(); state.ringingEvent = null; render(); });
    on("btn-alarm-snooze", "click", () => {
      const evt = state.ringingEvent;
      stopAlarmLoop();
      state.ringingEvent = null;
      if (evt) {
        state.calendarEvents = state.calendarEvents.map((e) => (e.id === evt.id ? Object.assign({}, e, { eventTimeMillis: Date.now() + 300000 }) : e));
        persist();
      }
      render();
    });

    // Chat
    document.querySelectorAll("[data-chat-tab]").forEach((btn) => {
      btn.addEventListener("click", () => { state.chatTab = btn.getAttribute("data-chat-tab"); render(); });
    });
    on("chat-photo-input", "change", (e) => {
      const file = e.target.files && e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onloadend = () => { state.chatAttachmentDataUrl = reader.result; render(); };
      reader.readAsDataURL(file);
    });
    on("btn-chat-send", "click", () => {
      const course = (document.getElementById("chat-course-input").value || "").trim().toUpperCase();
      const text = (document.getElementById("chat-msg-input").value || "").trim();
      if (!text && !state.chatAttachmentDataUrl) return;
      const ch = state.role === "student" && state.chatTab === "general" ? "general" : "direct";
      state.chatMessages.push({
        id: "chat_" + Date.now(),
        senderName: state.session.name,
        channel: ch,
        courseTag: course,
        text,
        imageDataUrl: ch === "direct" ? state.chatAttachmentDataUrl : ""
      });
      state.chatAttachmentDataUrl = "";
      persist();
      render();
    });

    // Materials & Tools
    on("btn-publish-material", "click", () => {
      const course = (document.getElementById("mat-course-input").value || "AOU").trim().toUpperCase();
      const title = (document.getElementById("mat-title-input").value || "").trim();
      const desc = (document.getElementById("mat-desc-input").value || "").trim();
      const url = (document.getElementById("mat-url-input").value || "").trim();
      if (!title) return;
      state.facultyMaterials.unshift({
        id: "mat_" + Date.now(),
        authorName: state.session.name,
        courseCode: course,
        title,
        description: desc,
        resourceUrl: url,
        materialType: url ? "link" : "slides"
      });
      persist();
      render();
      showToast(str.toastMaterialPublished);
    });
    document.querySelectorAll("[data-del-mat]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const id = btn.getAttribute("data-del-mat");
        state.facultyMaterials = state.facultyMaterials.filter((m) => m.id !== id);
        persist();
        render();
      });
    });

    document.querySelectorAll("[data-open-subtool]").forEach((card) => {
      card.addEventListener("click", () => { state.studentSubTool = card.getAttribute("data-open-subtool"); render(); });
    });
    on("btn-back-tools", "click", () => { state.studentSubTool = "hub"; render(); });

    on("btn-study-ask", "click", () => {
      const q = (document.getElementById("study-ask-input").value || "").trim();
      if (!q) return;
      state.studyMessages.push({ user: true, text: q });
      const ans = isArabic()
        ? `خطة دراسة سريعة حول «${q}»:\n1. مراجعة المفهوم الأساسي في دليل المقرر.\n2. تطبيق عملي عبر جلسة بومودورو مركزة (25 دقيقة) دون تشتيت.\n3. حل مسألتين من نماذج الاختبارات السابقة.`
        : `Focused Study Plan for "${q}":\n1. Review core concept definitions in the AOU course guide.\n2. Run one distraction-free 25-minute Pomodoro sprint to summarize key steps.\n3. Solve two past TMA/MTA practice problems.`;
      state.studyMessages.push({ user: false, text: ans });
      render();
    });

    on("qa-search-input", "input", (e) => {
      state.freshmanSearch = e.target.value;
      render();
      const inp = document.getElementById("qa-search-input");
      if (inp) { inp.focus(); inp.setSelectionRange(inp.value.length, inp.value.length); }
    });

    on("btn-voice-toggle", "click", () => {
      if (state.recordingVoice) stopVoiceRecording();
      else startVoiceRecording();
    });
    document.querySelectorAll("[data-play-voice]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const vn = state.voiceNotes.find((x) => x.id === btn.getAttribute("data-play-voice"));
        if (vn) togglePlayVoice(vn);
      });
    });

    on("btn-save-note", "click", () => {
      const title = (document.getElementById("note-title-input").value || "").trim();
      const course = (document.getElementById("note-course-input").value || "").trim().toUpperCase();
      const body = (document.getElementById("note-body-input").value || "").trim();
      const url = (document.getElementById("note-url-input").value || "").trim();
      if (!title) return;
      state.manualNotes.unshift({ id: "note_" + Date.now(), title, course, body, url });
      persist();
      render();
      showToast(str.toastNoteSaved);
    });
    document.querySelectorAll("[data-del-note]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const id = btn.getAttribute("data-del-note");
        state.manualNotes = state.manualNotes.filter((n) => n.id !== id);
        persist();
        render();
      });
    });

    // Settings
    on("profile-photo-input", "change", (e) => {
      const file = e.target.files && e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onloadend = () => { state.profilePhotoDataUrl = reader.result; persist(); render(); };
      reader.readAsDataURL(file);
    });
    on("btn-settings-dark", "click", () => { state.darkMode = !state.darkMode; persist(); render(); });
    document.querySelectorAll("[data-font-scale]").forEach((btn) => {
      btn.addEventListener("click", () => { state.fontScale = Number(btn.getAttribute("data-font-scale")); persist(); render(); });
    });
    on("btn-settings-lang", "click", () => { state.showLangModal = true; render(); });
    on("btn-close-lang", "click", () => { state.showLangModal = false; render(); });
    document.querySelectorAll("[data-choose-lang]").forEach((btn) => {
      btn.addEventListener("click", () => {
        setLanguage(btn.getAttribute("data-choose-lang"));
      });
    });
    document.querySelectorAll("[data-select-branch]").forEach((btn) => {
      btn.addEventListener("click", () => { state.aouBranch = btn.getAttribute("data-select-branch"); persist(); render(); });
    });

    // Install Modal
    on("btn-open-install", "click", () => { state.showInstallModal = true; render(); });
    on("btn-close-install", "click", () => { state.showInstallModal = false; render(); });
    on("btn-trigger-pwa-install", "click", async () => {
      if (!state.deferredInstallPrompt) return;
      state.deferredInstallPrompt.prompt();
      await state.deferredInstallPrompt.userChoice;
      state.deferredInstallPrompt = null;
      state.showInstallModal = false;
      render();
    });
  }

  function signOut() {
    stopAlarmLoop();
    releaseFocusLock();
    state.session = null;
    state.msAccessToken = null;
    state.drawerOpen = false;
    state.showRepliedReviewed = false;
    state.selectedEmailId = null;
    state.pomodoroRunning = false;
    persist();
    render();
  }

  window.addEventListener("beforeinstallprompt", (e) => {
    e.preventDefault();
    state.deferredInstallPrompt = e;
    render();
  });

  if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
      navigator.serviceWorker.register("./sw.js").catch(() => {});
    });
  }

  // Sync URL query language on startup (defaults to Arabic 'ar')
  syncUrlLanguage(state.languageCode);
  handleMicrosoftCallbackIfPresent().then(render);
})();
