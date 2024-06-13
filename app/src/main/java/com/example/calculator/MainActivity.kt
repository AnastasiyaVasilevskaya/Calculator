package com.example.calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var result: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            setNumberButtonListener()
            setOperatorButtonListeners()
            setEqualButtonListener()
            setClearButtonListener()
            setPercentButtonListener()
            setChangeSignButtonListener()
        }
    }

    private fun ActivityMainBinding.setNumberButtonListener() {
        val numberButtons = listOf(button0, button1, button2, button3, button4, button5, button6, button7, button8, button9, buttonDot)

        for (button in numberButtons) {
            button.setOnClickListener {
                onNumberClick(button.text.toString())
            }
        }
    }

    private fun ActivityMainBinding.setOperatorButtonListeners() {
        val operatorButtons = listOf(buttonPlus, buttonMinus, buttonMultiply, buttonDivide)

        for (button in operatorButtons) {
            button.setOnClickListener {
                onOperatorClick(button.text.toString())
            }
        }
    }

    private fun ActivityMainBinding.setEqualButtonListener() {
        buttonEqual.setOnClickListener {
            onEqualClick()
        }
    }

    private fun ActivityMainBinding.setClearButtonListener() {
        buttonClear.setOnClickListener {
            onClearClick()
        }
    }

    private fun ActivityMainBinding.setPercentButtonListener() {
        buttonPercent.setOnClickListener {
            onPercentClick()
        }
    }

    private fun ActivityMainBinding.setChangeSignButtonListener() {
        buttonChangeSign.setOnClickListener {
            onChangeSignClick()
        }
    }

    private fun onNumberClick(number: String) {
        if (number == ".") {
            val currentText = binding.operationDisplay.text.toString()
            if (currentText.isNotEmpty() && !currentText.last().toString()
                    .isOperator() && !currentText.endsWith(".")
            ) {
                binding.operationDisplay.append(number)
            }
        } else binding.operationDisplay.append(number)
    }

    private fun onOperatorClick(operator: String) {
        val currentText = binding.operationDisplay.text.toString()
        if (currentText.isNotEmpty() && !currentText.last().toString().isOperator()) {
            calculateResult()
            binding.operationDisplay.append(operator)
        }
    }

    private fun onEqualClick() {
        try {
            calculateResult()
        }catch (e:Exception) {
            binding.resultDisplay.text = "Error"
        }
    }

    private fun onClearClick() {
        binding.operationDisplay.text = ""
        binding.resultDisplay.text = "0"
        result = null
    }

    //Доработать
    private fun onPercentClick() {
        val currentText = binding.operationDisplay.text.toString()
        val currentValue = currentText.toDoubleOrNull() ?: return

        val percentageValue = currentValue / 100.0
        binding.operationDisplay.text = percentageValue.toString()
    }

    private fun onChangeSignClick() {
        var currentText = binding.operationDisplay.text.toString()
        if (currentText.isEmpty() || currentText == "0") return

        // Индекс первой цифры последнего числа
        val lastIndex = currentText.indexOfLast { it.isDigit() }

        // Если найдено число, меняем его знак
        if (lastIndex != -1) {
            val lastNumber = currentText.substring(lastIndex)
            currentText = currentText.removeRange(lastIndex, currentText.length)

            val newValue = if (currentText.endsWith("-")) {
                currentText.dropLast(1) + "+" + lastNumber
            } else if (currentText.endsWith("+")) {
                currentText.dropLast(1) + "-" + lastNumber
            }
//            else if (Regex("[÷×]$").containsMatchIn(currentText)) {
//                currentText + "-" + lastNumber
            else return

            binding.operationDisplay.text = newValue
        }
    }

    private fun calculateResult() {
        val expression = binding.operationDisplay.text.toString()
        val numbersAndOperators = expression.split("(?<=[-+×÷])|(?=[-+×÷])".toRegex())

        val numberStack = mutableListOf<Double>()
        val operatorStack = mutableListOf<String>()

        for (item in numbersAndOperators) {
            if (item.isNumeric()) {
                val number = item.toDouble()
                numberStack.add(number)
            } else if (item.isOperator()) {
                while (operatorStack.isNotEmpty() && hasPrecedence(item, operatorStack.last())) {
                    applyOperation(numberStack, operatorStack)
                }
                operatorStack.add(item)
            }
        }

        while (operatorStack.isNotEmpty()) {
            applyOperation(numberStack, operatorStack)
        }

        if (numberStack.size == 1) {
            result = numberStack.last()
            binding.resultDisplay.text = result?.toString() ?: ""
        } else {
            binding.resultDisplay.text = "Error"
        }
    }

    private fun applyOperation(
        numberStack: MutableList<Double>,
        operatorStack: MutableList<String>
    ) {
        val operator = operatorStack.removeLast()
        val operand2 = numberStack.removeLast()
        val operand1 = numberStack.removeLast()

        when (operator) {
            "+" -> numberStack.add(operand1 + operand2)
            "-" -> numberStack.add(operand1 - operand2)
            "×" -> numberStack.add(operand1 * operand2)
            "÷" -> {
                if (operand2 != 0.0) {
                    numberStack.add(operand1 / operand2)
                } else {
                    binding.resultDisplay.text = "Error"
                    result = null
                }
            }
        }
    }

    private fun hasPrecedence(op1: String, op2: String): Boolean {
        return !((op1 == "×" || op1 == "÷") && (op2 == "+" || op2 == "-"))
    }

    private fun String.isNumeric(): Boolean {
        return this.toDoubleOrNull() != null
    }

    private fun String.isOperator(): Boolean {
        return this in listOf("+", "-", "×", "÷")
    }
}