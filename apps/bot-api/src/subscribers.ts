import { mkdir, readFile, writeFile } from "node:fs/promises";
import path from "node:path";

const storageDir = path.resolve(process.cwd(), "data");
const storageFile = path.join(storageDir, "subscriber-chats.json");

const subscriberChats = new Set<number>();
let loaded = false;

async function ensureLoaded() {
  if (loaded) {
    return;
  }

  loaded = true;

  try {
    const raw = await readFile(storageFile, "utf8");
    const ids = JSON.parse(raw) as number[];

    for (const id of ids) {
      subscriberChats.add(id);
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);

    if (!message.includes("ENOENT")) {
      console.error("Failed to load subscriber chats", error);
    }
  }
}

async function persist() {
  await mkdir(storageDir, { recursive: true });
  await writeFile(storageFile, JSON.stringify([...subscriberChats]), "utf8");
}

export async function rememberSubscriberChat(chatId: number) {
  await ensureLoaded();

  if (subscriberChats.has(chatId)) {
    return;
  }

  subscriberChats.add(chatId);
  await persist();
}

export async function listSubscriberChats() {
  await ensureLoaded();
  return [...subscriberChats];
}
