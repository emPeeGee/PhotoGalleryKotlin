package com.empeegee.android.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class PhotoGalleryViewModel : ViewModel() {
    val galleryItemLiveDate: LiveData<List<GalleryItem>>

    init {
        galleryItemLiveDate = FlickrFetchr().fetchPhotos()
    }
}