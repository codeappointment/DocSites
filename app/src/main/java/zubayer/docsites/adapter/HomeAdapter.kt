package zubayer.docsites.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import zubayer.docsites.R
import zubayer.docsites.databinding.HomeItemBinding
import zubayer.docsites.model.HomeAdapterData
import zubayer.docsites.util.Helper
import zubayer.docsites.util.HomeAdapterClickListener

class HomeAdapter(val homeAdapterData: HomeAdapterData, val homeAdapterClickListener: HomeAdapterClickListener) :
    RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        return HomeViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.home_item,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.setUpView(
            homeAdapterData.headings[position],
            homeAdapterData.descriptions[position],
            homeAdapterData.hints[position]
        )
        holder.setUpImage(position)
    }

    override fun getItemCount(): Int {
        return homeAdapterData.headings.size
    }

    inner class HomeViewHolder(val homeView: View) : RecyclerView.ViewHolder(homeView) {
        val binding = HomeItemBinding.bind(homeView)
        val context = homeView.context
        var title = "BSMMU"

        init {
            itemView.setOnClickListener {
                homeAdapterClickListener.onClick(context, title)
            }
        }

        fun setUpView(titleText: String, headerText: String, subHeaderText: String) {
            title = titleText
            binding.title.text = titleText
            binding.header.text = headerText
            binding.subHeader.text = subHeaderText
            Helper.setTypeFace(listOf(binding.title, binding.header, binding.subHeader))
        }

        fun setUpImage(position: Int) {
            when (position) {
                0 -> Glide.with(context)
                    .load("https://firebasestorage.googleapis.com/v0/b/docsites-c20b8.appspot.com/o/uiImage%2F1536342915895?alt=media&token=274a9d19-64f1-410e-8da7-924e4f9f60cf")
                    .into(binding.image)
                1 -> Glide.with(context)
                    .load("https://firebasestorage.googleapis.com/v0/b/docsites-c20b8.appspot.com/o/uiImage%2F1536342932044?alt=media&token=06a51b0b-16e5-44c5-b92c-19e4fe709a8b")
                    .into(binding.image)
                2 -> Glide.with(context)
                    .load("https://firebasestorage.googleapis.com/v0/b/docsites-c20b8.appspot.com/o/uiImage%2F1536342948323?alt=media&token=5d4c6c41-4185-4ad9-8d64-8183057cfc8a")
                    .into(binding.image)
                3 -> Glide.with(context)
                    .load("https://firebasestorage.googleapis.com/v0/b/docsites-c20b8.appspot.com/o/uiImage%2F1536342971955?alt=media&token=09705bb8-51bd-46a0-950d-edc8502f2ca1")
                    .into(binding.image)
                4 -> Glide.with(context)
                    .load("https://firebasestorage.googleapis.com/v0/b/docsites-c20b8.appspot.com/o/uiImage%2F1536342971955?alt=media&token=09705bb8-51bd-46a0-950d-edc8502f2ca1")
                    .into(binding.image)
                5 -> Glide.with(context)
                    .load("https://firebasestorage.googleapis.com/v0/b/docsites-c20b8.appspot.com/o/uiImage%2F1536342971955?alt=media&token=09705bb8-51bd-46a0-950d-edc8502f2ca1")
                    .into(binding.image)
                6 -> Glide.with(context)
                    .load("https://firebasestorage.googleapis.com/v0/b/docsites-c20b8.appspot.com/o/uiImage%2F1536342990795?alt=media&token=57ab2ed1-d15d-44bc-bb90-14abd29f27f6")
                    .into(binding.image)
                7 -> Glide.with(context)
                    .load("https://firebasestorage.googleapis.com/v0/b/docsites-c20b8.appspot.com/o/uiImage%2F1536343007728?alt=media&token=7078ecf0-c333-4d6c-a58a-24329077330f")
                    .into(binding.image)
                8 -> Glide.with(context)
                    .load("https://firebasestorage.googleapis.com/v0/b/docsites-c20b8.appspot.com/o/uiImage%2F1536343061379?alt=media&token=a11e0a6b-1c5f-43f0-b571-d1e9478442a8")
                    .into(binding.image)
                9 -> Glide.with(context)
                    .load("https://firebasestorage.googleapis.com/v0/b/docsites-c20b8.appspot.com/o/uiImage%2F1536343022885?alt=media&token=81449d24-9943-4299-b38f-7b668233e5bd")
                    .into(binding.image)
            }
        }
    }
}