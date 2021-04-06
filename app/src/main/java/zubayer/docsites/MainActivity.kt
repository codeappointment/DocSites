package zubayer.docsites

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import zubayer.docsites.adapter.HomeAdapter
import zubayer.docsites.databinding.ActivityMainBinding
import zubayer.docsites.model.HomeAdapterData
import zubayer.docsites.util.SpacingItemDecoration

class MainActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpView()


    }

    fun setUpView() {
       val headings = getResources().getStringArray(R.array.heading);
        val descriptions = getResources().getStringArray(R.array.description);
        val hints = getResources().getStringArray(R.array.hints);
        val homeAdapterData = HomeAdapterData(headings, descriptions, hints)
        val homeAdapter = HomeAdapter(homeAdapterData)
        binding.content.homeList.setHasFixedSize(true)
        binding.content.homeList.layoutManager = GridLayoutManager(applicationContext, 2, GridLayoutManager.VERTICAL, false )
        binding.content.homeList.adapter = homeAdapter
        val spacingInPixel = resources.getDimensionPixelSize(R.dimen.spacingItem)
        val spacingItemDecoration = SpacingItemDecoration(2, spacingInPixel)
        binding.content.homeList.addItemDecoration(spacingItemDecoration)


    }

}