package org.ncgroup.kscan.format

import org.ncgroup.kscan.BarcodeFormat
import zxingcpp.BarcodeReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarcodeFormatMapperTest {

    @Test
    fun `GIVEN empty list WHEN toZxingCppFormats THEN returns every mapped format`() {
        val result = BarcodeFormatMapper.toZxingCppFormats(emptyList())

        assertEquals(13, result.size)
        assertTrue(BarcodeReader.Format.QR_CODE in result)
    }

    @Test
    fun `GIVEN all formats WHEN toZxingCppFormats THEN returns every mapped format`() {
        val result = BarcodeFormatMapper.toZxingCppFormats(listOf(BarcodeFormat.FORMAT_ALL_FORMATS))

        assertEquals(13, result.size)
    }

    @Test
    fun `GIVEN single format WHEN toZxingCppFormats THEN returns only that format`() {
        val result = BarcodeFormatMapper.toZxingCppFormats(listOf(BarcodeFormat.FORMAT_QR_CODE))

        assertEquals(setOf(BarcodeReader.Format.QR_CODE), result)
    }

    @Test
    fun `GIVEN multiple formats WHEN toZxingCppFormats THEN returns each of them`() {
        val result = BarcodeFormatMapper.toZxingCppFormats(
            listOf(BarcodeFormat.FORMAT_QR_CODE, BarcodeFormat.FORMAT_EAN_13),
        )

        assertEquals(setOf(BarcodeReader.Format.QR_CODE, BarcodeReader.Format.EAN_13), result)
    }

    @Test
    fun `GIVEN an unknown format WHEN toZxingCppFormats THEN it is left out`() {
        val result = BarcodeFormatMapper.toZxingCppFormats(listOf(BarcodeFormat.TYPE_UNKNOWN))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `GIVEN qr code zxing-cpp format WHEN toAppFormat THEN returns app format`() {
        val result = BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.QR_CODE)

        assertEquals(BarcodeFormat.FORMAT_QR_CODE, result)
    }

    @Test
    fun `GIVEN ean13 zxing-cpp format WHEN toAppFormat THEN returns app format`() {
        val result = BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.EAN_13)

        assertEquals(BarcodeFormat.FORMAT_EAN_13, result)
    }

    @Test
    fun `GIVEN code128 zxing-cpp format WHEN toAppFormat THEN returns app format`() {
        val result = BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.CODE_128)

        assertEquals(BarcodeFormat.FORMAT_CODE_128, result)
    }

    @Test
    fun `GIVEN none zxing-cpp format WHEN toAppFormat THEN returns type unknown`() {
        val result = BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.NONE)

        assertEquals(BarcodeFormat.TYPE_UNKNOWN, result)
    }

    @Test
    fun `GIVEN a variant of a format WHEN toAppFormat THEN it folds onto the symbology`() {
        // Asking for a symbology enables its variants, and zxing-cpp reports the
        // variant when it can tell one, so these arrive for callers who only ever
        // named the family.
        assertEquals(BarcodeFormat.FORMAT_CODE_39, BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.CODE_39_EXT))
        assertEquals(BarcodeFormat.FORMAT_CODE_39, BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.CODE_39_STD))
        assertEquals(BarcodeFormat.FORMAT_QR_CODE, BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.QR_CODE_MODEL_2))
        assertEquals(BarcodeFormat.FORMAT_PDF417, BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.COMPACT_PDF_417))
        assertEquals(BarcodeFormat.FORMAT_AZTEC, BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.AZTEC_CODE))
        assertEquals(BarcodeFormat.FORMAT_ITF, BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.ITF_14))
    }

    @Test
    fun `GIVEN a retail variant WHEN toAppFormat THEN folding does not confuse it with its siblings`() {
        // EAN-13, EAN-8, UPC-A and UPC-E are four variants of one symbology KScan
        // does not name, so each has to come from the exact lookup rather than the
        // fold, and a fifth variant it does not name stays unknown.
        assertEquals(BarcodeFormat.FORMAT_EAN_13, BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.EAN_13))
        assertEquals(BarcodeFormat.FORMAT_UPC_A, BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.UPC_A))
        assertEquals(BarcodeFormat.TYPE_UNKNOWN, BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.ISBN))
        assertEquals(BarcodeFormat.TYPE_UNKNOWN, BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.EAN_5))
    }

    @Test
    fun `GIVEN a format KScan does not expose WHEN toAppFormat THEN returns type unknown`() {
        val result = BarcodeFormatMapper.toAppFormat(BarcodeReader.Format.MAXI_CODE)

        assertEquals(BarcodeFormat.TYPE_UNKNOWN, result)
    }
}
