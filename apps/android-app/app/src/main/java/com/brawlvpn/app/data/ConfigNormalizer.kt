package com.brawlvpn.app.data

object ConfigNormalizer {
    fun withNullsDns(rawConfig: String): String {
        val normalizedLine = "DNS = ${NullsDns.wireGuardLine}"
        val lines = rawConfig.lines().toMutableList()

        var inInterface = false
        var dnsReplaced = false

        for (index in lines.indices) {
            val line = lines[index]
            val trimmed = line.trim()

            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                if (inInterface && !dnsReplaced) {
                    lines.add(index, normalizedLine)
                    dnsReplaced = true
                    break
                }
                inInterface = trimmed.equals("[Interface]", ignoreCase = true)
                continue
            }

            if (inInterface && trimmed.startsWith("DNS", ignoreCase = true)) {
                lines[index] = normalizedLine
                dnsReplaced = true
                break
            }
        }

        if (!dnsReplaced) {
            val interfaceIndex = lines.indexOfFirst { it.trim().equals("[Interface]", ignoreCase = true) }
            if (interfaceIndex >= 0) {
                lines.add(interfaceIndex + 1, normalizedLine)
            }
        }

        return lines.joinToString(separator = "\n")
    }
}
