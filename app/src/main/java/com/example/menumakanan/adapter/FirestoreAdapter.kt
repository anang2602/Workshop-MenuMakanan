package com.example.menumakanan.adapter

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*

abstract class FirestoreAdapter<VH : RecyclerView.ViewHolder>(private var query: Query?) :
    RecyclerView.Adapter<VH>(), EventListener<QuerySnapshot> {

    companion object {
        const val TAG = "Fire Store Adapter"
    }

    private var mRegistration: ListenerRegistration? = null

    private var mSnapshots = ArrayList<DocumentSnapshot>()

    fun stopListening() {
        if (mRegistration != null) {
            mRegistration!!.remove()
            mRegistration = null
        }

        mSnapshots.clear()
        notifyDataSetChanged()
    }

    fun startListening() {
        if (mRegistration == null) {
            mRegistration = query?.addSnapshotListener(this)
        }
    }

    fun setQuery(query: Query) {
        stopListening()

        mSnapshots.clear()
        notifyDataSetChanged()

        this.query = query
        startListening()
    }

    override fun getItemCount() = mSnapshots.size

    protected open fun getSnapshot(index: Int): DocumentSnapshot {
        return mSnapshots[index]
    }

    protected open fun onError(e: FirebaseFirestoreException) {}

    protected open fun onDataChanged() {}

    override fun onEvent(p0: QuerySnapshot?, p1: FirebaseFirestoreException?) {

        if (p1 != null) {
            Log.w(TAG, "onEvent:error", p1)
            onError(p1)
            return
        }

        for (change in p0!!.documentChanges) {

            when (change.type) {
                DocumentChange.Type.ADDED -> {
                    onDocumentAdded(change)
                }
                DocumentChange.Type.MODIFIED -> {
                    onDocumentModified(change)
                }
                DocumentChange.Type.REMOVED -> {
                    onDocumentRemoved(change)
                }
            }
        }

        onDataChanged()

    }

    protected open fun onDocumentAdded(change: DocumentChange) {
        mSnapshots.add(change.newIndex, change.document)
        notifyItemInserted(change.newIndex)
    }

    protected open fun onDocumentModified(change: DocumentChange) {
        if (change.oldIndex == change.newIndex) {
            mSnapshots[change.oldIndex] = change.document
            notifyDataSetChanged()
        } else {
            mSnapshots.removeAt(change.oldIndex)
            mSnapshots.add(change.newIndex, change.document)
            notifyItemMoved(change.oldIndex, change.newIndex)
        }
    }

    protected open fun onDocumentRemoved(change: DocumentChange) {
        mSnapshots.removeAt(change.oldIndex)
        notifyItemRemoved(change.oldIndex)
    }

}