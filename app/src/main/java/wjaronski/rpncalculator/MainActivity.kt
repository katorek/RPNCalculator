package wjaronski.rpncalculator

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.annotation.RequiresApi
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
//import wjaronski.rpncalculator.R.id.textView
import java.util.*
import java.util.stream.IntStream

class MainActivity : AppCompatActivity() {

    private var stack = LinkedList<Double>()
    private val screenVals = arrayListOf<String>()
    private var currentVal: String = ""
    private var precision: Int = 2
    private var lastStack = LinkedList<Double>()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        screenVals.ensureCapacity(5)

        IntStream.range(0, 4).forEach({ e -> screenVals.add("") })
        printToScreen()
        setUpButtons()
    }

    override fun onResume() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        textView.setTextColor(Color.parseColor(prefs.getString("font_color", "#000000")))
        textView.textSize = prefs.getString("font_size", "30").toFloat()
        textView.setBackgroundColor(Color.parseColor(prefs.getString("background_color", "#FFFFFF")))
        precision = prefs.getString("precision", "2").toInt()
        updateView()

        super.onResume()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun setUpButtons() {
        mapOf<String, Button>(
                Pair(".", buttonSeparator),
                Pair("0", button0),
                Pair("1", button1),
                Pair("2", button2),
                Pair("3", button3),
                Pair("4", button4),
                Pair("5", button5),
                Pair("6", button6),
                Pair("7", button7),
                Pair("8", button8),
                Pair("9", button9))
                .forEach { s, button -> button.setOnClickListener { writeToScreen(s) } }

        mapOf<String, Button>(
                Pair("+", buttonSum),
                Pair("-", buttonSub),
                Pair("*", buttonMul),
                Pair("/", buttonDiv),
                Pair("^", buttonPow),
                Pair("sqrt", buttonSqrt))
                .forEach { s, btn -> btn.setOnClickListener { calc(s) } }


        buttonEnter.setOnClickListener { enter() }
        buttonFlip.setOnClickListener { flip() }
        buttonAC.setOnClickListener { dropAll() }
        buttonDrop.setOnClickListener { drop() }
        buttonSwap.setOnClickListener { swap() }
        buttonDel.setOnClickListener { del(); }
        buttonSettings.setOnClickListener { showSettings() }
        buttonUndo.setOnClickListener { undo() }
    }

    private fun dropAll() {
        saveStack()
        stack.clear()
        updateView()
    }

    private fun drop() {
        saveStack()
        if (stack.size > 1)
            stack.removeLast();
        updateView()
    }

    private fun swap() {
        saveStack()
        if (stack.size > 1) {
            val t1 = stack.pollLast();
            val t2 = stack.pollLast();
            stack.add(t1)
            stack.add(t2)
        }
        updateView()
    }

    private fun del() {
        saveStack()
        currentVal = if (currentVal.length > 0) currentVal.substring(0, currentVal.length - 1) else ""
        updateView()
    }

    private fun undo() {
        val tmpStack = LinkedList<Double>()
        tmpStack.addAll(stack)
        stack.clear()
        stack.addAll(lastStack)

        lastStack.clear()
        lastStack.addAll(tmpStack)
        updateView()
    }

    fun showSettings() {
        val i = Intent(this, SettingsActivity::class.java)
        startActivity(i)
    }

    fun flip() {
        stack.add(stack.pollLast() * -1)
        updateView()
    }

    fun writeToScreen(v: String) {
        if (v.equals(".") && currentVal.contains(".")) return
        currentVal += v

        updateView()
    }

    fun enter() {
        if (currentVal.toDoubleOrNull() != null)
            stack.addLast(currentVal.toDouble())
        currentVal = ""
        updateView()
    }

    fun calc(operand: String) {

        if (operand.equals("sqrt")) {
            saveStack()
            stack.add(Math.sqrt(stack.pollLast()))
            updateView()
            return
        }
        if (stack.size < 2) return
        saveStack()
        val d2 = stack.pollLast()
        val d1 = stack.pollLast()
        var d3 = 0.0
        when (operand) {
            "+" -> d3 = d1 + d2
            "-" -> d3 = d1 - d2
            "*" -> d3 = d1 * d2
            "/" -> d3 = d1 / d2
            "^" -> d3 = Math.pow(d1, d2)
        }
        stack.addLast(d3)
        updateView()
    }

    fun updateView() {
        if (stack.size > 4) {
            for (i in stack.size - 4..stack.size - 1) {
                screenVals[stack.size - i - 1] = stack[i].format(precision)
            }
        } else {
            for (i in 1..4) {
                val tmp = stack.getOrNull(stack.size - i)
                screenVals[i - 1] = "" + if (tmp == null) "" else tmp.format(precision)
            }
        }
        printToScreen()
    }

    fun printToScreen() {
        textView.text = ""
        for (i in 1..4) {
            textView.text = "\t" + i + ": " + screenVals[i - 1] + "\n" + textView.text
        }
        textView.append("\n\t: " + currentVal)
    }

    fun Double.format(digits: Int) = java.lang.String.format("%.${digits}f", this)

    fun saveStack() {
        lastStack.clear()
        lastStack.addAll(stack)
    }
}
