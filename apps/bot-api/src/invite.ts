import type { Context, Telegraf } from "telegraf";
import { config } from "./config.js";

export async function createPrivateChannelInvite(bot: Telegraf<Context>) {
  const expireDate = Math.floor(Date.now() / 1000) + 60 * 60;

  return bot.telegram.createChatInviteLink(config.PRIVATE_CHANNEL_ID, {
    creates_join_request: false,
    expire_date: expireDate,
    member_limit: 1,
    name: `paid-${Date.now()}`
  });
}
