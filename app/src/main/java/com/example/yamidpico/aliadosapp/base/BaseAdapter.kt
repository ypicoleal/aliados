package com.example.yamidpico.aliadosapp.base

import android.support.v7.widget.RecyclerView
import android.view.View

abstract class BaseAdapter(protected val listener: OnItemClickListener): RecyclerView.Adapter<BaseAdapter.BaseViewHolder>() {

    interface OnItemClickListener {
        fun onClick(position: Int)
    }

    inner class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        init {
            itemView.setOnClickListener { listener.onClick(adapterPosition) }
        }
    }
}