package com.brenbailey.wheredyouparkthecar
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity(R.layout.activity_main){

    private lateinit var topAppBar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        topAppBar = findViewById(R.id.topAppBar)
        setSupportActionBar(topAppBar)
        /*
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.favorite -> {
                    Log.d("distance", "clicked")
                    true
                }
                else -> false
            }
        }

         */
    }
}