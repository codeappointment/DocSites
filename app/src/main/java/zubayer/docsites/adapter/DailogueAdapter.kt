package zubayer.docsites.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import zubayer.docsites.R
import zubayer.docsites.databinding.DailogueItemBinding
import zubayer.docsites.util.Helper

class DailogueAdapter(val options: Array<String>) :
    RecyclerView.Adapter<DailogueAdapter.DailogueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailogueViewHolder {
        return DailogueViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.dailogue_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: DailogueViewHolder, position: Int) {
        val title = options[position]
        holder.binding.title.text = title
        Helper.setTypeFace(listOf(holder.binding.title))
    }

    override fun getItemCount(): Int {
        return options.size
    }

    inner class DailogueViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DailogueItemBinding.bind(itemView)

    }
}