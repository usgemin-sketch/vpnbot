import { Markup } from "telegraf";

export const mainKeyboard = Markup.inlineKeyboard([
  [Markup.button.callback("Купить VPN навсегда", "buy_forever")],
  [Markup.button.callback("Получить ссылку еще раз", "restore_access")]
]);
