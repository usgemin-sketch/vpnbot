import type { Context, Telegraf } from "telegraf";
import { copy } from "./copy.js";
import { listSubscriberChats } from "./subscribers.js";

const PROMO_INTERVAL_MS = 3 * 60 * 60 * 1000;

let promoTimer: NodeJS.Timeout | undefined;

async function sendPromo(bot: Telegraf<Context>) {
  const chatIds = await listSubscriberChats();

  if (chatIds.length === 0) {
    return;
  }

  for (const chatId of chatIds) {
    try {
      await bot.telegram.sendMessage(chatId, copy.promoMessage);
    } catch (error) {
      console.error(`Failed to send promo message to chat ${chatId}`, error);
    }
  }
}

export function startPromoLoop(bot: Telegraf<Context>) {
  if (promoTimer) {
    return;
  }

  promoTimer = setInterval(() => {
    void sendPromo(bot);
  }, PROMO_INTERVAL_MS);
}
