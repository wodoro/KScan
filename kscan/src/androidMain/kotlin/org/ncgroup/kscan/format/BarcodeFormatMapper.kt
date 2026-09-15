package org.ncgroup.kscan.format

import org.ncgroup.kscan.BarcodeFormat
import zxingcpp.BarcodeReader

internal object BarcodeFormatMapper {

    private val formats = FormatMap(
        mapOf(
            BarcodeReader.Format.QR_CODE to BarcodeFormat.FORMAT_QR_CODE,
            BarcodeReader.Format.CODE_128 to BarcodeFormat.FORMAT_CODE_128,
            BarcodeReader.Format.CODE_39 to BarcodeFormat.FORMAT_CODE_39,
            BarcodeReader.Format.CODE_93 to BarcodeFormat.FORMAT_CODE_93,
            BarcodeReader.Format.CODABAR to BarcodeFormat.FORMAT_CODABAR,
            BarcodeReader.Format.DATA_MATRIX to BarcodeFormat.FORMAT_DATA_MATRIX,
            BarcodeReader.Format.EAN_13 to BarcodeFormat.FORMAT_EAN_13,
            BarcodeReader.Format.EAN_8 to BarcodeFormat.FORMAT_EAN_8,
            BarcodeReader.Format.ITF to BarcodeFormat.FORMAT_ITF,
            BarcodeReader.Format.UPC_A to BarcodeFormat.FORMAT_UPC_A,
            BarcodeReader.Format.UPC_E to BarcodeFormat.FORMAT_UPC_E,
            BarcodeReader.Format.PDF_417 to BarcodeFormat.FORMAT_PDF417,
            BarcodeReader.Format.AZTEC to BarcodeFormat.FORMAT_AZTEC,
        ),
    )

    // Named rather than left empty for every format: an empty set asks zxing-cpp
    // for every symbology it knows, including the ones KScan has no name for,
    // which would only be decoded to be reported as TYPE_UNKNOWN and dropped.
    fun toZxingCppFormats(appFormats: List<BarcodeFormat>): Set<BarcodeReader.Format> = formats.platformFormats(appFormats).toSet()

    /**
     * zxing-cpp names a symbology's variants as well as the symbology itself, and
     * reports the variant whenever it can tell one: an extended Code 39 comes back
     * as `CODE_39_EXT` rather than `CODE_39`. Asking for a family enables its
     * variants, so one has to fold back onto the family KScan named, or a barcode
     * the caller did ask for is reported as unknown and dropped.
     */
    fun toAppFormat(zxingCppFormat: BarcodeReader.Format): BarcodeFormat {
        val named = formats.appFormat(zxingCppFormat)
        if (named != BarcodeFormat.TYPE_UNKNOWN) return named

        return zxingCppFormat.symbology()?.let(formats::appFormat) ?: BarcodeFormat.TYPE_UNKNOWN
    }

    /**
     * The symbology this format belongs to, or `null` where zxing-cpp names none.
     *
     * The low byte is the symbology and the high byte the variant, with `0x20`
     * standing for the symbology itself. Folding cannot over-reach: EAN-13 and
     * UPC-A are both variants of one EAN/UPC symbology, which KScan does not name,
     * so each is only ever matched by the exact lookup above.
     */
    private fun BarcodeReader.Format.symbology(): BarcodeReader.Format? {
        val family = (value and 0xFF) or SYMBOLOGY_ITSELF

        return BarcodeReader.Format.entries.firstOrNull { it.value == family }
    }

    private const val SYMBOLOGY_ITSELF = 0x2000
}
