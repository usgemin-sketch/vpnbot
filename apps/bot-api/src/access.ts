import { mkdir, readFile, writeFile } from "node:fs/promises";
import path from "node:path";

const storageDir = path.resolve(process.cwd(), "data");
const storageFile = path.join(storageDir, "paid-users.json");

const paidUsers = new Set<number>();
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
      paidUsers.add(id);
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);

    if (!message.includes("ENOENT")) {
      console.error("Failed to load paid users", error);
    }
  }
}

async function persist() {
  await mkdir(storageDir, { recursive: true });
  await writeFile(storageFile, JSON.stringify([...paidUsers]), "utf8");
}

export async function hasPaidAccess(userId: number) {
  await ensureLoaded();
  return paidUsers.has(userId);
}

export async function grantPaidAccess(userId: number) {
  await ensureLoaded();
  paidUsers.add(userId);
  await persist();
}
