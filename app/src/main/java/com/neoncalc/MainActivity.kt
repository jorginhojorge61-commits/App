package com.neoncalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal

private val Night = Color(0xFF090914)
private val Panel = Color(0xFF121222)
private val SoftPanel = Color(0xFF1A1A2E)
private val Cyan = Color(0xFF36F5F2)
private val Pink = Color(0xFFFF3CAC)
private val Violet = Color(0xFF9B5CFF)
private val Ink = Color(0xFFF4F4FF)
private val Muted = Color(0xFF77778F)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 9, 20)
        window.navigationBarColor = android.graphics.Color.rgb(9, 9, 20)
        window.decorView.systemUiVisibility = 0
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Night) {
                    NeonCalculator()
                }
            }
        }
    }
}

@Composable
private fun NeonCalculator() {
    var expression by remember { mutableStateOf("0") }
    var justEvaluated by remember { mutableStateOf(false) }
    val preview = remember(expression) {
        runCatching { ExpressionParser(expression).parse() }
            .getOrNull()
            ?.takeIf { expression.lastOrNull()?.isDigit() == true || expression.lastOrNull() == ')' || expression.lastOrNull() == '%' }
            ?.let(::formatResult)
    }

    fun press(key: String) {
        when (key) {
            "AC" -> { expression = "0"; justEvaluated = false }
            "⌫" -> {
                expression = if (expression.length <= 1) "0" else expression.dropLast(1)
                justEvaluated = false
            }
            "=" -> {
                runCatching { formatResult(ExpressionParser(expression).parse()) }
                    .onSuccess { expression = it; justEvaluated = true }
                    .onFailure { expression = "Erro"; justEvaluated = true }
            }
            "+/−" -> {
                if (expression != "0" && expression != "Erro") {
                    expression = if (expression.startsWith("-")) expression.drop(1) else "-$expression"
                    justEvaluated = false
                }
            }
            "+", "−", "×", "÷" -> {
                if (expression == "Erro") expression = "0"
                val op = when (key) { "−" -> "-"; "×" -> "*"; "÷" -> "/"; else -> "+" }
                expression = if (expression.lastOrNull() in listOf('+', '-', '*', '/')) {
                    expression.dropLast(1) + op
                } else expression + op
                justEvaluated = false
            }
            else -> {
                if (expression == "Erro" || justEvaluated) {
                    expression = if (key == ".") "0." else key
                    justEvaluated = false
                } else if (key == ".") {
                    val currentNumber = expression.takeLastWhile { it.isDigit() || it == '.' }
                    if (!currentNumber.contains('.')) expression += if (currentNumber.isEmpty()) "0." else "."
                } else {
                    expression = if (expression == "0") key else expression + key
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Night).padding(horizontal = 24.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).background(Cyan, CircleShape).shadow(10.dp, CircleShape, ambientColor = Cyan, spotColor = Cyan))
            Text("  NEON", color = Cyan, fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Text(" / CALC", color = Muted, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.weight(1f))
            Text("v1.0", color = Muted, fontSize = 11.sp, letterSpacing = 1.sp)
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().background(
                Brush.verticalGradient(listOf(Panel, Color(0xFF0D0D1A))), RoundedCornerShape(26.dp)
            ).border(1.dp, Color(0xFF292940), RoundedCornerShape(26.dp)).padding(22.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text("SYSTEM READY  •  CALC MODE", color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(22.dp))
            Text(
                expression.replace("*", "×").replace("/", "÷").replace("-", "−"),
                color = Ink, fontSize = if (expression.length > 12) 32.sp else 44.sp,
                fontWeight = FontWeight.Light, textAlign = TextAlign.End,
                maxLines = 1, overflow = TextOverflow.Ellipsis, letterSpacing = 1.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                if (expression == "Erro") "CHECK INPUT" else preview?.let { "= $it" } ?: " ",
                color = Cyan, fontSize = 18.sp, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
        }

        val rows = listOf(
            listOf("AC", "+/−", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("⌫", "0", ".", "=")
        )
        Column(modifier = Modifier.weight(1.45f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEach { row ->
                Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { key ->
                        CalculatorKey(key = key, modifier = Modifier.weight(1f).fillMaxSize()) { press(key) }
                    }
                }
            }
        }
        Text("DESIGNED FOR THE AFTER HOURS", modifier = Modifier.fillMaxWidth(), color = Color(0xFF4F4F68),
            fontSize = 9.sp, textAlign = TextAlign.Center, letterSpacing = 2.sp)
    }
}

@Composable
private fun CalculatorKey(key: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val isOperator = key in listOf("÷", "×", "−", "+")
    val isAction = key in listOf("AC", "+/−", "%", "⌫")
    val isEquals = key == "="
    val foreground = when {
        isEquals -> Night
        isOperator -> Cyan
        isAction -> Pink
        else -> Ink
    }
    val background = when {
        isEquals -> Brush.linearGradient(listOf(Cyan, Color(0xFF83FFF8)))
        isOperator -> Brush.linearGradient(listOf(Color(0xFF192A35), Color(0xFF15202D)))
        isAction -> Brush.linearGradient(listOf(Color(0xFF2A172C), Color(0xFF201528)))
        else -> Brush.linearGradient(listOf(SoftPanel, Color(0xFF151525)))
    }
    Box(
        modifier = modifier.shadow(if (isEquals) 12.dp else 0.dp, RoundedCornerShape(20.dp),
            ambientColor = Cyan.copy(alpha = 0.4f), spotColor = Cyan.copy(alpha = 0.4f))
            .background(background, RoundedCornerShape(20.dp))
            .border(1.dp, when { isEquals -> Cyan; isOperator -> Cyan.copy(alpha = 0.23f); isAction -> Pink.copy(alpha = 0.2f); else -> Color(0xFF28283B) }, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(key, color = foreground, fontSize = if (key.length > 2) 17.sp else 24.sp,
            fontWeight = if (isEquals || isOperator) FontWeight.Bold else FontWeight.Medium)
    }
}

private fun formatResult(value: Double): String {
    require(value.isFinite()) { "Result is not finite" }
    if (value == 0.0) return "0"
    return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
}

/** A small recursive-descent parser; no dynamic code evaluation is used. */
private class ExpressionParser(private val source: String) {
    private var position = 0

    fun parse(): Double {
        val result = expression()
        if (position != source.length) error("Unexpected input")
        return result
    }

    private fun expression(): Double {
        var value = term()
        while (position < source.length && (source[position] == '+' || source[position] == '-')) {
            val operator = source[position++]
            val right = term()
            value = if (operator == '+') value + right else value - right
        }
        return value
    }

    private fun term(): Double {
        var value = unary()
        while (position < source.length && (source[position] == '*' || source[position] == '/')) {
            val operator = source[position++]
            val right = unary()
            value = if (operator == '*') value * right else {
                require(right != 0.0) { "Division by zero" }
                value / right
            }
        }
        return value
    }

    private fun unary(): Double {
        if (position < source.length && source[position] == '+') { position++; return unary() }
        if (position < source.length && source[position] == '-') { position++; return -unary() }
        var value = primary()
        while (position < source.length && source[position] == '%') {
            position++
            value /= 100.0
        }
        return value
    }

    private fun primary(): Double {
        if (position < source.length && source[position] == '(') {
            position++
            val value = expression()
            require(position < source.length && source[position] == ')') { "Missing closing parenthesis" }
            position++
            return value
        }
        val start = position
        var dots = 0
        while (position < source.length && (source[position].isDigit() || source[position] == '.')) {
            if (source[position] == '.') dots++
            require(dots <= 1) { "Invalid number" }
            position++
        }
        require(position > start && source.substring(start, position) != ".") { "Expected a number" }
        return source.substring(start, position).toDouble()
    }
}
