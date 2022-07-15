package com.duran.howlstagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.duran.howlstagram.R
import com.duran.howlstagram.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AlarmFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = LayoutInflater.from(activity).inflate(R.layout.fragment_alarm, container, false)

        view.findViewById<RecyclerView>(R.id.alarmfragment_recyclerview).adapter = AlarmRecyclerviewAdapter()
        view.findViewById<RecyclerView>(R.id.alarmfragment_recyclerview).layoutManager = LinearLayoutManager(activity)

        return view
    }

    inner class AlarmRecyclerviewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        val alarmDTOList: ArrayList<AlarmDTO> = arrayListOf()

         init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid).addSnapshotListener { value, error ->
                alarmDTOList.clear()
                if(value == null) return@addSnapshotListener

                for(snapshot in value.documents){
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)

            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View): RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val view = holder.itemView

            when(alarmDTOList[position].kind){
                0 -> {
                    val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                    view.findViewById<TextView>(R.id.commentviewitem_textview_profile).text = str_0
                }
                1 -> {
                    val str_0 = alarmDTOList[position].userId + " " + getString(R.string.alarm_comment) + " of " + alarmDTOList[position].message
                    view.findViewById<TextView>(R.id.commentviewitem_textview_profile).text = str_0
                }
                2 -> {
                  val str_0 = alarmDTOList[position].userId + " " + getString(R.string.alarm_follow)
                  view.findViewById<TextView>(R.id.commentviewitem_textview_profile).text = str_0
                }
            }
        }
    }
}