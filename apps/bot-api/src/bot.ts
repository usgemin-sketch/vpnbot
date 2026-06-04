import type { Context, Telegraf } from "telegraf";
import { Markup } from "telegraf";
import { grantPaidAccess, hasPaidAccess } from "./access.js";
import { copy } from "./copy.js";
import { createPrivateChannelInvite } from "./invite.js";
import { mainKeyboard } from "./keyboards.js";
import { isForeverPayload, sendForeverInvoice } from "./payments.js";

async function sendFreshInvite(ctx: Context, bot: Telegraf<Context>) {
  try {
    const invite = await createPrivateChannelInvite(bot);

    await ctx.reply(
      `${copy.paymentSuccess}\n\n${invite.invite_link}\n\n${copy.support}`,
      Markup.removeKeyboard()
    );
  } catch (error) {
    console.error("Failed to create invite link", error);
    await ctx.reply(copy.paymentFailure);
  }
}

export function registerBot(bot: Telegraf<Context>) {
  bot.start(async (ctx) => {
    await ctx.reply(copy.welcome, mainKeyboard);
  });

  bot.command("buy", async (ctx) => {
    await sendForeverInvoice(bot, ctx.chat.id);
  });

  bot.action("buy_forever", async (ctx) => {
    await ctx.answerCbQuery();
    await sendForeverInvoice(bot, ctx.chat!.id);
  });

  bot.action("restore_access", async (ctx) => {
    await ctx.answerCbQuery();

    const userId = ctx.from?.id;

    if (!userId || !(await hasPaidAccess(userId))) {
      await ctx.reply(copy.noAccessYet, mainKeyboard);
      return;
    }

    await sendFreshInvite(ctx, bot);
  });

  bot.on("pre_checkout_query", async (ctx) => {
    const payload = ctx.preCheckoutQuery.invoice_payload;

    if (!isForeverPayload(payload)) {
      await ctx.answerPreCheckoutQuery(false, copy.preCheckoutFailure);
      return;
    }

    await ctx.answerPreCheckoutQuery(true);
  });

  bot.on("message", async (ctx, next) => {
    const successfulPayment = ctx.message && "successful_payment" in ctx.message
      ? ctx.message.successful_payment
      : undefined;

    if (!successfulPayment) {
      return next();
    }

    if (!isForeverPayload(successfulPayment.invoice_payload)) {
      return;
    }

    await grantPaidAccess(ctx.from.id);
    await sendFreshInvite(ctx, bot);
  });

  bot.hears(/купить/i, async (ctx) => {
    await sendForeverInvoice(bot, ctx.chat.id);
  });

  bot.hears(/ссылка|доступ/i, async (ctx) => {
    await ctx.reply(copy.alreadyPaid, mainKeyboard);
  });
}
