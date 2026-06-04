import express from "express";
import { Telegraf } from "telegraf";
import { registerBot } from "./bot.js";
import { config } from "./config.js";

const bot = new Telegraf(config.BOT_TOKEN);
registerBot(bot);

const app = express();
app.use(express.json());

app.get("/health", (_req, res) => {
  res.json({ ok: true, service: "brawl-vpn-bot-api" });
});

if (config.WEBHOOK_URL) {
  const webhookPath = `/telegram/${config.WEBHOOK_SECRET}`;

  app.use(bot.webhookCallback(webhookPath));

  app.listen(config.PORT, async () => {
    await bot.telegram.setWebhook(`${config.WEBHOOK_URL}${webhookPath}`);
    console.log(`Webhook server listening on ${config.PORT}`);
  });
} else {
  app.listen(config.PORT, () => {
    console.log(`HTTP server listening on ${config.PORT}`);
  });

  bot.launch().then(() => {
    console.log("Bot started in polling mode");
  });
}

process.once("SIGINT", () => bot.stop("SIGINT"));
process.once("SIGTERM", () => bot.stop("SIGTERM"));
