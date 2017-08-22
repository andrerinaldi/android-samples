package com.rinaldiandrea.android.barcodegenerator.async

import android.graphics.Bitmap
import android.os.AsyncTask
import com.rinaldiandrea.android.barcodegenerator.BarcodeGenerator
import kotlin.properties.Delegates

/**
 * An example of async task which uses the BarcodeGenerator class and returns the generated bitmap.
 *
 * @author arinaldi
 */
class BarcodeGeneratorTask(private val mListener: Listener) : AsyncTask<String, Void, Bitmap?>() {

    private var mBarcodeGenerator: BarcodeGenerator by Delegates.notNull()

    init {
        mBarcodeGenerator = BarcodeGenerator()
    }

    // region AsyncTask implementation

    override fun doInBackground(vararg strings: String): Bitmap? {
        val barcode = strings[0]
        val barcodeType = Integer.valueOf(strings[1])!!
        val width = Integer.valueOf(strings[2])!!
        val height = Integer.valueOf(strings[3])!!
        return mBarcodeGenerator.generate(barcode, barcodeType, width, height)
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        super.onPostExecute(bitmap)
        mListener.onBitmapReady(bitmap)
    }

    // endregion

    interface Listener {

        fun onBitmapReady(bitmap: Bitmap?)
    }
}