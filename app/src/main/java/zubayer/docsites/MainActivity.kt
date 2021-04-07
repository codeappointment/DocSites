package zubayer.docsites

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import zubayer.docsites.adapter.DailogueAdapter
import zubayer.docsites.adapter.HomeAdapter
import zubayer.docsites.databinding.ActivityMainBinding
import zubayer.docsites.databinding.DailogueLayoutBinding
import zubayer.docsites.model.HomeAdapterData
import zubayer.docsites.util.Helper
import zubayer.docsites.util.HomeAdapterClickListener
import zubayer.docsites.util.SpacingItemDecoration

class MainActivity : AppCompatActivity(), HomeAdapterClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var options: Array<String>
    private lateinit var selected: String


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
        val homeAdapter = HomeAdapter(homeAdapterData, this)
        binding.content.homeList.setHasFixedSize(true)
        binding.content.homeList.layoutManager =
            GridLayoutManager(applicationContext, 2, GridLayoutManager.VERTICAL, false)
        binding.content.homeList.adapter = homeAdapter
        val spacingInPixel = resources.getDimensionPixelSize(R.dimen.spacingItem)
        val spacingItemDecoration = SpacingItemDecoration(2, spacingInPixel)
        binding.content.homeList.addItemDecoration(spacingItemDecoration)

    }

    fun setUpDalilogue(title: String, options: Array<String>) {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dailogue_layout,null)
        val dailogueBinding = DailogueLayoutBinding.bind(view)
        val dailogueAdapter = DailogueAdapter(options)
        dailogueBinding.title.text = title
        Helper.setTypeFace(listOf(dailogueBinding.title))
        dailogueBinding.options.setHasFixedSize(true)
        dailogueBinding.options.layoutManager = LinearLayoutManager(this)
        dailogueBinding.options.adapter = dailogueAdapter
        builder.setView(view)
        val dailogue = builder.create()
        dailogue.show()
        dailogueBinding.close.setOnClickListener {
            dailogue.dismiss()
        }
        dailogue.setOnDismissListener {
           // TODO: use things when dismiss
        }
    }

    override fun onClick(context: Context, title: String) {
        selected = title
        when(title) {
            "BSMMU" -> {
                options = getResources().getStringArray(R.array.bsmmuOptions);
                options.set(0, "Home page")
                setUpDalilogue(title, options)

            }
            "BCPS" -> {
                options = getResources().getStringArray(R.array.bcpsOptions);
                options.set(0, "Home page")
                setUpDalilogue(title, options)
            }
            "DGHS" -> {
                options = getResources().getStringArray(R.array.dghsOptions);
                options.set(0, "Home page")
                setUpDalilogue(title, options)
            }
            "MOHFW" -> {
                options = getResources().getStringArray(R.array.mohfwOptions);
                options.set(0, "Home page")
                setUpDalilogue(title, options)
            }
            "BPSC" -> {
                options = getResources().getStringArray(R.array.bpscOptions);
                options.set(0, "Home page")
                setUpDalilogue(title, options)
            }
            "Gazette" -> {
                options = getResources().getStringArray(R.array.gazetteOptions);
                setUpDalilogue(title, options)
            }
            "DGFP" -> {
                options = getResources().getStringArray(R.array.dgfpOptions);
                options.set(0, "Home page")
                setUpDalilogue(title, options)
            }
            "CCD" -> {

            }
            "BMDC" -> {
                options = getResources().getStringArray(R.array.bmdcOptions);
                setUpDalilogue(title, options)
            }
            "MBBS/BDS" -> {
                options = getResources().getStringArray(R.array.resultsOption);
                setUpDalilogue(title, options)
            }

        }
    }

}