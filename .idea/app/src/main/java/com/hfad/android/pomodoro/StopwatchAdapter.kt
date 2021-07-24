package com.hfad.android.pomodoro

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.hfad.android.pomodoro.databinding.StopwatchItemBinding

class StopwatchAdapter(private val listener: StopwatchListener) :
    ListAdapter<Stopwatch, StopwatchViewHolder>(
        itemComparator
    ) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopwatchViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = StopwatchItemBinding.inflate(layoutInflater, parent, false)
        return StopwatchViewHolder(binding,listener, binding.root.context.resources) // вот такая удобная передача binding'a в ViewHolder
    }

    override fun onBindViewHolder(holder: StopwatchViewHolder, position: Int) {
        holder.bind(getItem(position)) //holder.bind(stopwatches[position])
    }

    companion object {
        private val itemComparator = object : DiffUtil.ItemCallback<Stopwatch>() {
            override fun getChangePayload(oldItem: Stopwatch, newItem: Stopwatch)  = Any()

            override fun areItemsTheSame(oldItem: Stopwatch, newItem: Stopwatch): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Stopwatch, newItem: Stopwatch): Boolean {
                return oldItem.isStarted == newItem.isStarted && oldItem.currentMs == newItem.currentMs
            }
        }

    }

}