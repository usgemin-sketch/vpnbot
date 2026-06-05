import type { Context, Telegraf } from "telegraf";
import { config } from "./config.js";

const FOREVER_PAYLOAD = "brawl_vpn_forever";
const LUCK_PAYLOAD_PREFIX = "brawl_vpn_luck_";

export type LuckOffer = {
  id: "50" | "25" | "15" | "5";
  stars: number;
  chancePercent: number;
  payload: string;
  label: string;
  startParameter: string;
};

export const luckOffers: LuckOffer[] = [
  {
    id: "50",
    stars: 50,
    chancePercent: 20,
    payload: `${LUCK_PAYLOAD_PREFIX}50`,
    label: "Удача 50",
    startParameter: "luck-50"
  },
  {
    id: "25",
    stars: 25,
    chancePercent: 10,
    payload: `${LUCK_PAYLOAD_PREFIX}25`,
    label: "Удача 25",
    startParameter: "luck-25"
  },
  {
    id: "15",
    stars: 15,
    chancePercent: 3,
    payload: `${LUCK_PAYLOAD_PREFIX}15`,
    label: "Удача 15",
    startParameter: "luck-15"
  },
  {
    id: "5",
    stars: 5,
    chancePercent: 1,
    payload: `${LUCK_PAYLOAD_PREFIX}5`,
    label: "Удача 5",
    startParameter: "luck-5"
  }
];

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

export async function sendLuckInvoice(
  bot: Telegraf<Context>,
  chatId: number,
  offerId: LuckOffer["id"]
) {
  const offer = luckOffers.find((item) => item.id === offerId);

  if (!offer) {
    throw new Error(`Unknown luck offer: ${offerId}`);
  }

  await bot.telegram.sendInvoice(chatId, {
    title: `${config.PRODUCT_TITLE} - Удача`,
    description: `Шанс ${offer.chancePercent}% получить ссылку в приватную группу с APK.`,
    payload: offer.payload,
    provider_token: "",
    currency: "XTR",
    prices: [{ label: `${offer.label} (${offer.chancePercent}%)`, amount: offer.stars }],
    start_parameter: offer.startParameter
  });
}

export function isForeverPayload(payload: string) {
  return payload === FOREVER_PAYLOAD;
}

export function getLuckOfferByPayload(payload: string) {
  return luckOffers.find((offer) => offer.payload === payload);
}

export function isKnownPaymentPayload(payload: string) {
  return isForeverPayload(payload) || Boolean(getLuckOfferByPayload(payload));
}
