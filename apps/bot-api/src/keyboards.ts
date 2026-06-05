import { Markup } from "telegraf";

export const mainKeyboard = Markup.inlineKeyboard([
  [Markup.button.callback("Купить VPN навсегда", "buy_forever")],
  [Markup.button.callback("Удача", "open_luck")],
  [Markup.button.callback("Получить ссылку еще раз", "restore_access")]
]);

export const luckKeyboard = Markup.inlineKeyboard([
  [Markup.button.callback("50 Stars - 20%", "luck_50")],
  [Markup.button.callback("25 Stars - 10%", "luck_25")],
  [Markup.button.callback("15 Stars - 3%", "luck_15")],
  [Markup.button.callback("5 Stars - 1%", "luck_5")],
  [Markup.button.callback("Назад", "back_main")]
]);
