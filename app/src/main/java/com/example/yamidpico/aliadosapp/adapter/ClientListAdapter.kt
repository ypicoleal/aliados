package com.example.yamidpico.aliadosapp.adapter

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.yamidpico.aliadosapp.R
import com.example.yamidpico.aliadosapp.model.Client
import kotlinx.android.synthetic.main.item_clients.view.*

class ClientListAdapter(private val listener: OnDeleteClickListener) : RecyclerView.Adapter<ClientListAdapter.ClientsViewHolder>() {

    interface OnDeleteClickListener {
        fun onDeleteClick(position: Int)
    }

    private var clients = listOf<Client>()

    override fun onCreateViewHolder(parent: ViewGroup, pviewType1: Int): ClientsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_clients, parent, false)
        return ClientsViewHolder(view)
    }

    override fun getItemCount() = clients.size

    override fun onBindViewHolder(holder: ClientsViewHolder, position: Int) {
        val client = clients[position]

        Log.e("tales", "$client")

        holder.itemView.apply {
            user.text = client.fullName
            document.text = client.document
        }
    }

    fun updateItems(clients: List<Client>) {
        this.clients = clients
        notifyDataSetChanged()
    }

    inner class ClientsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.delete.setOnClickListener {
                listener.onDeleteClick(adapterPosition)
            }
        }
    }
}