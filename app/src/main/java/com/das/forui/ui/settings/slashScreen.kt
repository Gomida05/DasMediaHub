package com.das.forui.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.das.forui.R
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.das.forui.databinding.SplashScreenActivityBinding

class slashScreen: Fragment() {
    private var _binding: SplashScreenActivityBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = SplashScreenActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView= binding.recycl
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val dataList = listOf(
            MyData("Item 1", R.drawable.music_note_24dp),
            MyData("Item 2", R.drawable.pause_icon) )
        val adapter = MyAdapter(dataList)
        recyclerView.adapter = adapter
    }
}
data class MyData(val text: String, val image: Int)

class MyAdapter(private val dataList: List<MyData>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.test_screen, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        holder.textView.text = data.text
        holder.imageView.setImageResource(data.image)

        holder.button.setOnClickListener {
            Toast.makeText(it.context, "Button clicked!", Toast.LENGTH_SHORT).show()
        }
        holder.itemView.setOnClickListener {
            Toast.makeText(it.context, "Item clicked!", Toast.LENGTH_SHORT).show()
        }
    }
    override fun getItemCount() = dataList.size class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val button: Button = itemView.findViewById(R.id.button)
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}
