import "dotenv/config";
import { z } from "zod";

const schema = z.object({
  BOT_TOKEN: z.string().min(1),
  BOT_USERNAME: z.string().min(1),
  PRIVATE_CHANNEL_ID: z.string().min(1),
  WEBHOOK_SECRET: z.string().min(1).default("change-me"),
  WEBHOOK_URL: z.string().optional().default(""),
  PORT: z.coerce.number().default(3000),
  PRODUCT_TITLE: z.string().default("Brawl VPN Forever"),
  PRODUCT_DESCRIPTION: z
    .string()
    .default("Private APK access with permanent access to the private release channel."),
  PRODUCT_PRICE_STARS: z.coerce.number().int().positive().default(100)
});

export const config = schema.parse(process.env);
