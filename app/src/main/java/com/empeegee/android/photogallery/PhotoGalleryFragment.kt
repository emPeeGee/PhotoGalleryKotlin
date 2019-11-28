package com.empeegee.android.photogallery

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager


class PhotoGalleryFragment : Fragment() {

    private lateinit var photoRecyclerView: RecyclerView
    private lateinit var photoGalleryViewModel: PhotoGalleryViewModel
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
        setHasOptionsMenu(true)

        photoGalleryViewModel =
            ViewModelProviders.of(this).get(PhotoGalleryViewModel::class.java)

        val responseHandler = Handler()
        thumbnailDownloader = ThumbnailDownloader(responseHandler) {
            photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindDrawable(drawable)
        }

        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)


        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val workRequest = OneTimeWorkRequest
            .Builder(PoolWorker::class.java)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance().enqueue(workRequest)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewLifecycleOwner.lifecycle.addObserver(
            thumbnailDownloader.viewLifecycleOwner
        )
        val view = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        photoRecyclerView.layoutManager = GridLayoutManager(context, 3)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoGalleryViewModel.galleryItemLiveDate.observe(
            viewLifecycleOwner, Observer { galleryItems ->
                photoRecyclerView.adapter = PhotoAdapter(galleryItems)
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewLifecycleOwner.lifecycle.removeObserver(
            thumbnailDownloader.viewLifecycleOwner
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView


        searchView.apply {

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(queryText: String): Boolean {
                    Log.d("AAA", "QueryTextSubmit $queryText")
                    photoGalleryViewModel.fetchPhotos(queryText)

                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.hideSoftInputFromWindow(searchView.windowToken, 0)
                    searchView.onActionViewCollapsed()

                    return true
                }

                override fun onQueryTextChange(queryText: String?): Boolean {
                    Log.d("AAA", "queryTextChanged $queryText")

                    return false
                }
            })


            setOnSearchClickListener {
                searchView.setQuery(photoGalleryViewModel.seachTerm, false)
            }

        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            } else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }




    private class PhotoHolder(itemView: ImageView) : RecyclerView.ViewHolder(itemView) {
        val bindDrawable: (Drawable) -> Unit = itemView::setImageDrawable

    }

    private inner class PhotoAdapter(private val galleryItems : List<GalleryItem>) : RecyclerView.Adapter<PhotoHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val view = layoutInflater
                .inflate(R.layout.list_item_gallery, parent, false) as ImageView


            return PhotoHolder(view)
        }

        override fun getItemCount(): Int {
            return galleryItems.size
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryItems[position]

            val placeHolder: Drawable = ContextCompat
                .getDrawable(requireContext(), R.drawable.bill_up_close) ?: ColorDrawable()
            holder.bindDrawable(placeHolder)
            thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        }

    }

    companion object {
        fun newInstance(): PhotoGalleryFragment {
            return PhotoGalleryFragment()
        }
    }
}