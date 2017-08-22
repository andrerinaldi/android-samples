package com.rinaldiandrea.android.barcodegenerator

import android.graphics.Bitmap

/**
 * @author arinaldi
 */
interface IBarcodeGenerator {

    /**
     * Generates a bitmap representing the given barcode.
     *
     * @param barcode the string to convert in bitmap
     * @param barcodeType the format of the output barcode
     * @param width the desired width of the output barcode
     * @param height the desired height of the output barcode
     *
     * @return the barcode bitmap, which can be null if something goes wrong
     */
    fun generate(barcode: String, barcodeType: Int, width: Int, height: Int): Bitmap?
}