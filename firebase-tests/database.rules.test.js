import fs from "node:fs";
import path from "node:path";
import { after, before, beforeEach, test } from "node:test";
import assert from "node:assert/strict";
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from "@firebase/rules-unit-testing";
import { get, ref, set, update } from "firebase/database";

const projectId = "demo-menu-kiosk";
const rules = fs.readFileSync(path.resolve("../database.rules.json"), "utf8");
const enabledUid = process.env.KIOSK_UID_1 ?? "REPLACE_WITH_KIOSK_UID_1";
const otherEnabledUid = process.env.KIOSK_UID_2 ?? "REPLACE_WITH_KIOSK_UID_2";
const disabledUid = "disabled-kiosk";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({ projectId, database: { rules } });
});

beforeEach(async () => {
  await testEnv.clearDatabase();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.database();
    await set(ref(db), {
      branch2: {
        categories: { Drinks: { coffee: { name: "Coffee", price: 100 } } },
        appSettings: { backgroundTheme: "Dark" },
        inventory: {
          Drinks: { coffee: { sizes: { Medium: { stock: 5 } } } },
        },
      },
    });
  });
});

after(async () => testEnv.cleanup());

function kiosk(uid) {
  return testEnv.authenticatedContext(uid, {
    firebase: { sign_in_provider: "anonymous" },
  }).database();
}

function validOrder(uid = enabledUid, id = "123e4567-e89b-12d3-a456-426614174000") {
  return {
    orderId: id,
    submittedByUid: uid,
    orderNumber: "123E4567",
    customerName: "Guest",
    items: [{ name: "Coffee", size: "Medium", quantity: 1, price: 100, subtotal: 100 }],
    total: 100,
    paymentMethod: "COUNTER",
    paymentStatus: "PAY_AT_COUNTER",
    timestamp: { ".sv": "timestamp" },
  };
}

test("protected branch access requires a manually allowlisted UID", async () => {
  await assertSucceeds(get(ref(kiosk(enabledUid), "branch2/categories")));
  await assertFails(get(ref(kiosk(disabledUid), "branch2/categories")));
  await assertFails(get(ref(testEnv.unauthenticatedContext().database(), "branch2/categories")));
});

test("registered kiosk can atomically create an order and decrement stock", async () => {
  const id = "123e4567-e89b-12d3-a456-426614174000";
  await assertSucceeds(update(ref(kiosk(enabledUid)), {
    [`branch2/logs/${id}`]: validOrder(),
    "branch2/inventory/Drinks/coffee/sizes/Medium/stock": 4,
  }));
});

test("duplicate and cross-UID orders are rejected", async () => {
  const id = "123e4567-e89b-12d3-a456-426614174000";
  await assertSucceeds(set(ref(kiosk(enabledUid), `branch2/logs/${id}`), validOrder()));
  await assertFails(set(ref(kiosk(enabledUid), `branch2/logs/${id}`), validOrder()));
  await assertFails(set(ref(kiosk(disabledUid), `branch2/logs/${id}`), validOrder(disabledUid)));
  await assertFails(get(ref(kiosk(otherEnabledUid), `branch2/logs/${id}`)));
});

test("malformed orders are rejected", async () => {
  const id = "123e4567-e89b-12d3-a456-426614174000";
  const malformed = validOrder();
  malformed.paymentStatus = "BANK_CONFIRMED";
  await assertFails(set(ref(kiosk(enabledUid), `branch2/logs/${id}`), malformed));
});

test("stock cannot increase, become negative, or be changed by disabled kiosks", async () => {
  const stock = (db) => ref(db, "branch2/inventory/Drinks/coffee/sizes/Medium/stock");
  await assertFails(set(stock(kiosk(enabledUid)), 6));
  await assertFails(set(stock(kiosk(enabledUid)), -1));
  await assertFails(set(stock(kiosk(disabledUid)), 4));
  await assertSucceeds(set(stock(kiosk(enabledUid)), 4));
});

test("a kiosk cannot read another kiosk's existing order", async () => {
  const otherUid = otherEnabledUid;
  const id = "123e4567-e89b-12d3-a456-426614174000";
  await assertSucceeds(set(ref(kiosk(enabledUid), `branch2/logs/${id}`), validOrder()));
  await assertFails(get(ref(kiosk(otherUid), `branch2/logs/${id}`)));
  assert.equal((await get(ref(kiosk(enabledUid), `branch2/logs/${id}/orderNumber`))).val(), "123E4567");
});
