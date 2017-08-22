package com.rinaldiandrea.android.barcodegenerator

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.util.*

/**
 * @author arinaldi
 */
class BarcodeGenerator private constructor(barcodeColor: Int, backgroundColor: Int) : IBarcodeGenerator {

    companion object {
        val FORMAT_CODABAR: Int = 8
        val FORMAT_CODE_128: Int = 1
        val FORMAT_CODE_39: Int = 2
        val FORMAT_CODE_93: Int = 4
        val FORMAT_EAN_13: Int = 32
        val FORMAT_EAN_8: Int = 64
        val FORMAT_ITF: Int = 128
        val FORMAT_UPC_A: Int = 512
        val FORMAT_UPC_E: Int = 1024

        fun factory(): IBarcodeGenerator {
            return BarcodeGenerator(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
        }

        fun factory(barcodeColor: Int, backgroundColor: Int): IBarcodeGenerator {
            return BarcodeGenerator(barcodeColor, backgroundColor)
        }
    }

    private var mBarcodeColor: Int = barcodeColor
    private var mBackgroundColor: Int = backgroundColor

    // region IBarcodeGenerator implementation

    override fun generate(barcode: String, barcodeType: Int, width: Int, height: Int): Bitmap? {
        var barcodeBitmap: Bitmap? = null
        val barcodeFormat = convertToZXingFormat(barcodeType)
        try {
            barcodeBitmap = encodeAsBitmap(barcode, barcodeFormat, width, height)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        return barcodeBitmap
    }

    // endregion

    // region Private methods

    private fun convertToZXingFormat(format: Int): BarcodeFormat {
        when (format) {
            FORMAT_CODABAR -> return BarcodeFormat.CODABAR
            FORMAT_CODE_128 -> return BarcodeFormat.CODE_128
            FORMAT_CODE_39 -> return BarcodeFormat.CODE_39
            FORMAT_CODE_93 -> return BarcodeFormat.CODE_93
            FORMAT_EAN_13 -> return BarcodeFormat.EAN_13
            FORMAT_EAN_8 -> return BarcodeFormat.EAN_8
            FORMAT_ITF -> return BarcodeFormat.ITF
            FORMAT_UPC_A -> return BarcodeFormat.UPC_A
            FORMAT_UPC_E -> return BarcodeFormat.UPC_E
            else -> return BarcodeFormat.CODE_128
        }
    }

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
                pixels[offset + x] = if (result.get(x, y)) mBarcodeColor else mBackgroundColor
            }
        }

        val bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun guessAppropriateEncoding(contents: CharSequence): String? {
        for (i in 0..contents.length - 1) {
            if (contents[i].toInt() > 0xFF) {
                return "UTF-8"
            }
        }
        return null
    }

    // endregion
}