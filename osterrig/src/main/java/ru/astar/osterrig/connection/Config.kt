package ru.astar.osterrig.connection

import java.util.UUID

data class Config(
    val commandServiceUuid: UUID? = null,
    val commandSentUuid: UUID? = null,
    val readValueUuid: UUID? = null,
    val batteryServiceUuid: UUID? = null,
    val batteryValueUuid: UUID? = null,
    val mtuValue: Int = 0
) {
    class Builder {
        private var commandServiceUuid: UUID? = null
        private var commandSentUuid: UUID? = null
        private var readValueUuid: UUID? = null
        private var batteryServiceUuid: UUID? = null
        private var batteryValueUuid: UUID? = null
        private var mtuValue: Int = 0

        fun setCommandService(uuid: UUID): Builder {
            commandServiceUuid = uuid
            return this
        }

        fun setCommandSent(uuid: UUID): Builder {
            commandSentUuid = uuid
            return this
        }

        fun setReadValue(uuid: UUID): Builder {
            readValueUuid = uuid
            return this
        }

        fun setBatteryService(uuid: UUID): Builder {
            batteryServiceUuid = uuid
            return this
        }

        fun setBatteryValue(uuid: UUID): Builder {
            batteryValueUuid = uuid
            return this
        }

        fun setMtu(mtu: Int): Builder {
            mtuValue = mtu
            return this
        }

        fun build(): Config {
            return Config(
                commandServiceUuid,
                commandSentUuid,
                readValueUuid,
                batteryServiceUuid,
                batteryValueUuid
            )
        }
    }

    override fun toString(): String {
        val buffer = StringBuilder()
        buffer.append("Config[")
        commandServiceUuid?.let { buffer.append("  commandServiceUuid - $it\n") }
        commandSentUuid?.let { buffer.append("  commandSentUuid - $it\n") }
        readValueUuid?.let { buffer.append("  readValueUuid - $it\n") }
        batteryServiceUuid?.let { buffer.append("  batteryServiceUuid - $it\n") }
        batteryValueUuid?.let { buffer.append("  batteryValueUuid - $it\n") }
        buffer.append("]")
        return buffer.toString()
    }
}