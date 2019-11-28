package com.empeegee.android.photogallery

import android.app.Application
import androidx.lifecycle.*

class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {

    private val flickrFetchr = FlickrFetchr()
    private val mutableSearchTerms = MutableLiveData<String>()

    val galleryItemLiveDate: LiveData<List<GalleryItem>>
    val seachTerm: String
        get() = mutableSearchTerms.value ?: ""

    init {
        mutableSearchTerms.value = QueryPreferences.getStoredQuery(app)

        galleryItemLiveDate = Transformations.switchMap(mutableSearchTerms) { searchTerm ->
            if (searchTerm.isBlank()) {
                flickrFetchr.fetchPhotos()
            } else {
                flickrFetchr.searchPhotos(searchTerm)
            }
        }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerms.value = query
    }
}