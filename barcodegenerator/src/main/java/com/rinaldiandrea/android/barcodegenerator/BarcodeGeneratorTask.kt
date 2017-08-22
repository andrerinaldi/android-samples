package com.rinaldiandrea.android.barcodegenerator

import android.graphics.Bitmap
import android.os.AsyncTask
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.util.*

/**
 * @author arinaldi
 */

class BarcodeGeneratorTask(private val mListener: Listener?) : AsyncTask<String, Void, Bitmap?>() {

    // region AsyncTask implementation

    override fun doInBackground(vararg strings: String): Bitmap? {
        val barcode = strings[0]
        val barcodeType = Integer.valueOf(strings[1])!!
        val width = Integer.valueOf(strings[2])!!
        val height = Integer.valueOf(strings[3])!!
        return getBitmap(barcode, barcodeType, width, height)
    }

    override fun onPostExecute(bitmap: Bitmap?) {
        super.onPostExecute(bitmap)

        mListener?.onBitmapReady(bitmap)
    }

    // endregion

    interface Listener {

        fun onBitmapReady(bitmap: Bitmap?)
    }

    companion object {

        fun getBitmap(barcode: String, barcodeType: Int, width: Int, height: Int): Bitmap? {
            var barcodeBitmap: Bitmap? = null
            val barcodeFormat = convertToZXingFormat(barcodeType)
            try {
                barcodeBitmap = encodeAsBitmap(barcode, barcodeFormat, width, height)
            } catch (e: WriterException) {
                e.printStackTrace()
            }

            return barcodeBitmap
        }

        private fun convertToZXingFormat(format: Int): BarcodeFormat {
            when (format) {
                8 -> return BarcodeFormat.CODABAR
                1 -> return BarcodeFormat.CODE_128
                2 -> return BarcodeFormat.CODE_39
                4 -> return BarcodeFormat.CODE_93
                32 -> return BarcodeFormat.EAN_13
                64 -> return BarcodeFormat.EAN_8
                128 -> return BarcodeFormat.ITF
                512 -> return BarcodeFormat.UPC_A
                1024 -> return BarcodeFormat.UPC_E
                else -> return BarcodeFormat.CODE_128
            }
        }

        private val WHITE = 0x00FFFFFF.toInt()
        private val BLACK = 0xFF002A54.toInt()

        @Throws(WriterException::class)
        private fun encodeAsBitmap(contents: String?, format: BarcodeFormat, img_width: Int, img_height: Int): Bitmap? {
            if (contents == null) {
                return null
            }
            var hints: MutableMap<EncodeHintType, Any>? = null
            val encoding = guessAppropriateEncoding(contents)
            if (encoding != null) {
                hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
                hints.put(EncodeHintType.CHARACTER_SET, encoding)
            }
            val writer = MultiFormatWriter()
            val result: BitMatrix
            try {
                result = writer.encode(contents, format, img_width, img_height, hints)
            } catch (iae: IllegalArgumentException) {
                // Unsupported format
                return null
            }

            val width = result.width
            val height = result.height
            val pixels = IntArray(width * height)
            for (y in 0..height - 1) {
                val offset = y * width
                for (x in 0..width - 1) {
                    pixels[offset + x] = if (result.get(x, y)) BLACK else WHITE
                }
            }

            val bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
        }

        private fun guessAppropriateEncoding(contents: CharSequence): String? {
            // Very crude at the moment
            for (i in 0..contents.length - 1) {
                if (contents[i].toInt() > 0xFF) {
                    return "UTF-8"
                }
            }
            return null
        }
    }
}