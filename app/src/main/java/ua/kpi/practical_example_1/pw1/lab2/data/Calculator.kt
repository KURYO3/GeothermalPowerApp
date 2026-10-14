package ua.kpi.practical_example_1.pw1.lab2.data

class Calculator {
    // temp - температура, flow - витрата води, efficiency - ккд, return - потужність в кВт
    fun calculatePowerAdvanced(temp: String, flow: String, efficiency: String): Double {
        // Перетворення вхідного рядка в число, якщо не можливо - 0
        val t = temp.toDoubleOrNull() ?: 0.0
        val f = flow.toDoubleOrNull() ?: 0.0

        // Перетворення ккд у %
        val eff = efficiency.toDoubleOrNull()?.div(100) ?: 0.0

        val deltaT = if (t > 50) t - 50 else 0.0

        // Спрощена формула теплової потужності + ккд
        return deltaT * f * 4.18 * eff
    }
}