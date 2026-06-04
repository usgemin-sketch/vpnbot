import type { Context, Telegraf } from "telegraf";
import { config } from "./config.js";

const FOREVER_PAYLOAD = "brawl_vpn_forever";

export async function sendForeverInvoice(
  bot: Telegraf<Context>,
  chatId: number
) {
  await bot.telegram.sendInvoice(chatId, {
    title: config.PRODUCT_TITLE,
    description: config.PRODUCT_DESCRIPTION,
    payload: FOREVER_PAYLOAD,
    provider_token: "",
    currency: "XTR",
    prices: [{ label: "Навсегда", amount: config.PRODUCT_PRICE_STARS }],
    start_parameter: "buy-brawl-vpn-forever"
  });
}

export function isForeverPayload(payload: string) {
  return payload === FOREVER_PAYLOAD;
}
