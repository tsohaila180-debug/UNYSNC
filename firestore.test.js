const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_student_1";
const BOB_UID = "bob_instructor_2";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any UniSync collection", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(unauth.collection("materials").where("published", "==", true).get());
  await assertFails(unauth.collection("chatMessages").where("channel", "==", "general").get());
});

test("User profile: owner create, partial update, and cross-user isolation", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 2000);

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Noura Al-Mansoor",
      email: "noura@aou.edu.sa",
      role: "student",
      aouBranch: "Kuwait",
      majorId: "cs",
      companionGender: "girl",
      fontScale: "medium",
      darkMode: false,
      languageCode: "en",
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).update({
      fontScale: "large",
      darkMode: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field & immutable createdAt change
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user isolation
  await assertFails(bob.collection("users").doc(ALICE_UID).get());
  await assertFails(bob.collection("users").doc(ALICE_UID).update({ displayName: "Hack", updatedAt: past }));
});

test("Course progress, calendar events, and manual notes: owner-scoped CRUD", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("courseProgress").doc("TM351").set({
      userId: ALICE_UID,
      courseCode: "TM351",
      majorId: "cs",
      status: "in_progress",
      updatedAt: now,
    })
  );

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("courseProgress").get()
  );

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("calendarEvents").doc("evt_1").set({
      userId: ALICE_UID,
      title: "TM351 Midterm",
      courseCode: "TM351",
      category: "exam",
      eventTimeMillis: Date.now() + 86400000,
      alarmEnabled: true,
      ringtoneId: "chime",
      source: "manual",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("manualNotes").doc("note_1").set({
      userId: ALICE_UID,
      title: "Week 4 Summary",
      content: "Data management notes",
      courseTag: "TM351",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertFails(bob.collection("users").doc(ALICE_UID).collection("manualNotes").get());
  await assertFails(bob.collection("users").doc(ALICE_UID).collection("calendarEvents").get());
});

test("Faculty materials: filtered query succeeds, unfiltered fails, only author can edit/delete", async () => {
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    bob.collection("materials").doc("mat_1").set({
      authorId: BOB_UID,
      authorName: "Dr. Tariq",
      courseCode: "TM351",
      title: "Lecture 5 Slides",
      description: "Normalization and indexing",
      resourceUrl: "https://lms.aou.edu/tm351/lec5",
      materialType: "slides",
      published: true,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(alice.collection("materials").where("published", "==", true).get());
  await assertFails(alice.collection("materials").get());
  await assertFails(alice.collection("materials").doc("mat_1").delete());
});

test("Dual chat messages: general and participant-scoped direct queries work and block snooping", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bob = testEnv.authenticatedContext(BOB_UID).firestore();
  const eve = testEnv.authenticatedContext("eve_3").firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    alice.collection("chatMessages").doc("msg_gen_1").set({
      senderId: ALICE_UID,
      senderName: "Noura Al-Mansoor",
      senderRole: "student",
      channel: "general",
      participants: [],
      courseTag: "M269",
      text: "Hello everyone!",
      createdAt: now,
    })
  );

  await assertSucceeds(
    alice.collection("chatMessages").doc("msg_dir_1").set({
      senderId: ALICE_UID,
      senderName: "Noura Al-Mansoor",
      senderRole: "student",
      channel: "direct",
      participants: [ALICE_UID, BOB_UID],
      courseTag: "TM351",
      text: "Question about TMA part 2",
      createdAt: now,
    })
  );

  await assertSucceeds(
    bob.collection("chatMessages")
      .where("channel", "==", "direct")
      .where("participants", "array-contains", BOB_UID)
      .get()
  );

  await assertSucceeds(
    eve.collection("chatMessages").where("channel", "==", "general").get()
  );

  await assertFails(eve.collection("chatMessages").doc("msg_dir_1").get());
  await assertFails(eve.collection("chatMessages").get());
});
