package me.penguinpistol.backdrop

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.penguinpistol.backdrop.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOpen.setOnClickListener {
            binding.backdrop.open()
        }
    }
}