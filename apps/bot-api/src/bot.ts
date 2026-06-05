import type { Context, Telegraf } from "telegraf";
import { Markup } from "telegraf";
import { grantPaidAccess, hasPaidAccess } from "./access.js";
import { copy } from "./copy.js";
import { createPrivateChannelInvite } from "./invite.js";
import { luckKeyboard, mainKeyboard } from "./keyboards.js";
import {
  getLuckOfferByPayload,
  isForeverPayload,
  isKnownPaymentPayload,
  sendForeverInvoice,
  sendLuckInvoice
} from "./payments.js";
import { rememberSubscriberChat } from "./subscribers.js";

async function sendFreshInvite(
  ctx: Context,
  bot: Telegraf<Context>,
  successText = copy.paymentSuccess
) {
  try {
    const invite = await createPrivateChannelInvite(bot);

    await ctx.reply(
      `${successText}\n\n${invite.invite_link}\n\n${copy.support}`,
      Markup.removeKeyboard()
    );
  } catch (error) {
    console.error("Failed to create invite link", error);
    await ctx.reply(copy.paymentFailure);
  }
}

export function registerBot(bot: Telegraf<Context>) {
  bot.use(async (ctx, next) => {
    if (ctx.chat?.type === "private") {
      await rememberSubscriberChat(ctx.chat.id);
    }

    return next();
  });

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

  bot.action("open_luck", async (ctx) => {
    await ctx.answerCbQuery();
    await ctx.reply(copy.luckIntro, luckKeyboard);
  });

  bot.action("back_main", async (ctx) => {
    await ctx.answerCbQuery();
    await ctx.reply(copy.welcome, mainKeyboard);
  });

  bot.action("luck_50", async (ctx) => {
    await ctx.answerCbQuery();
    await sendLuckInvoice(bot, ctx.chat!.id, "50");
  });

  bot.action("luck_25", async (ctx) => {
    await ctx.answerCbQuery();
    await sendLuckInvoice(bot, ctx.chat!.id, "25");
  });

  bot.action("luck_15", async (ctx) => {
    await ctx.answerCbQuery();
    await sendLuckInvoice(bot, ctx.chat!.id, "15");
  });

  bot.action("luck_5", async (ctx) => {
    await ctx.answerCbQuery();
    await sendLuckInvoice(bot, ctx.chat!.id, "5");
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

    if (!isKnownPaymentPayload(payload)) {
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

    if (isForeverPayload(successfulPayment.invoice_payload)) {
      await grantPaidAccess(ctx.from.id);
      await sendFreshInvite(ctx, bot);
      return;
    }

    const luckOffer = getLuckOfferByPayload(successfulPayment.invoice_payload);

    if (!luckOffer) {
      return;
    }

    const isWinner = Math.random() * 100 < luckOffer.chancePercent;

    if (!isWinner) {
      await ctx.reply(copy.luckLose, mainKeyboard);
      return;
    }

    await grantPaidAccess(ctx.from.id);
    await sendFreshInvite(ctx, bot, copy.luckWin);
  });

  bot.hears(/купить/i, async (ctx) => {
    await sendForeverInvoice(bot, ctx.chat.id);
  });

  bot.hears(/удача/i, async (ctx) => {
    await ctx.reply(copy.luckIntro, luckKeyboard);
  });

  bot.hears(/ссылка|доступ/i, async (ctx) => {
    await ctx.reply(copy.alreadyPaid, mainKeyboard);
  });
}
