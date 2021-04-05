package com.pucosa.dotindicator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.pucosa.dotIndicator.DotIndicator

class MainActivity : AppCompatActivity() {

    var dotIndicator: DotIndicator? = null
    var btn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dotIndicator = findViewById(R.id.dotIndicator)
        btn = findViewById(R.id.mBtn)

        var index = 0

        dotIndicator?.setDotTint(resources.getColor(R.color.white))
        /*dotIndicator?.onClickListener = {
            index = it
            Toast.makeText(this, "$it index clicked", Toast.LENGTH_SHORT).show()
        }*/

        dotIndicator?.onSelectionChangeListener = { previousPos: Int, currentPos: Int ->
            Toast.makeText(this, "$currentPos selected from $previousPos", Toast.LENGTH_SHORT).show()
        }

        btn?.setOnClickListener {
            dotIndicator?.setSelectedIndex(++index)
        }
    }

}