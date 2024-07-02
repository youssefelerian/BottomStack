package com.example.scalableviewtest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.scalableviewtest.adapter.ItemsAdapter
import com.example.scalableviewtest.databinding.ActivityMainBinding
import com.example.scalableviewtest.model.ItemsModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val itemsAdapter = ItemsAdapter()

    //    private val recyclerView by lazy { findViewById<ScalableView>(R.id.scalableView)?.findViewById<RecyclerView>(R.id.rv) }
//    private val stackLayoutManager by lazy { StackLayoutManagerDep(this, recyclerView) }
    private val items = listOf(
        ItemsModel("1", "AED", "AED - United Arab Emirates"),
        ItemsModel("2", "USD", "USD - United States of America"),
        ItemsModel("3", "GBP", "GBP - United Kingdom"),
        ItemsModel("4", "INR", "INR - India"),
        ItemsModel("5", "PKR", "PKR - Pakistan"),
        ItemsModel("6", "BDT", "BDT - Bangladesh"),
        ItemsModel("7", "CAD", "CAD - Canada"),
        ItemsModel("8", "JPY", "JPY - Japan"),
        ItemsModel("9", "AFN", "AFN - Afghanistan"),
        ItemsModel("10", "ALL", "ALL - Albania"),
        ItemsModel("11", "AZN", "AZN - Azerbaijan"),
        ItemsModel("12", "BTN", "BTN - Bhutan"),
        ItemsModel("13", "COP", "COP - Columbia"),
        ItemsModel("14", "KHR", "KHR - Cambodia"),
        ItemsModel("15", "EGP", "EGP - Egypt"),
        ItemsModel("16", "SAR", "SAR - Saudi Arabia"),
        ItemsModel("17", "KWD", "KWD - Kuwait"),
        ItemsModel("18", "QTR", "QTR - Qatar"),
        ItemsModel("19", "BHD", "BHD - Bahrain"),
        ItemsModel("20", "OMR", "OMR - Oman"),
        ItemsModel("21", "AED", "AED - United Arab Emirates"),
        ItemsModel("22", "USD", "USD - United States of America"),
        ItemsModel("23", "GBP", "GBP - United Kingdom"),
        ItemsModel("24", "INR", "INR - India"),
        ItemsModel("25", "PKR", "PKR - Pakistan"),
        ItemsModel("26", "BDT", "BDT - Bangladesh"),
        ItemsModel("27", "CAD", "CAD - Canada"),
        ItemsModel("28", "JPY", "JPY - Japan"),
        ItemsModel("29", "AFN", "AFN - Afghanistan"),
        ItemsModel("30", "ALL", "ALL - Albania"),
        ItemsModel("31", "AZN", "AZN - Azerbaijan"),
        ItemsModel("32", "BTN", "BTN - Bhutan"),
        ItemsModel("33", "COP", "COP - Columbia"),
        ItemsModel("34", "KHR", "KHR - Cambodia"),
        ItemsModel("35", "EGP", "EGP - Egypt"),
        ItemsModel("36", "SAR", "SAR - Saudi Arabia"),
        ItemsModel("37", "KWD", "KWD - Kuwait"),
        ItemsModel("38", "QTR", "QTR - Qatar"),
        ItemsModel("39", "BHD", "BHD - Bahrain"),
        ItemsModel("40", "OMR", "OMR - Oman"),
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        itemsAdapter.itemList = items


        binding.recyclerView.run {
             addContent(R.layout.content_view)
             setAdapter(itemsAdapter)

        }
    }
}