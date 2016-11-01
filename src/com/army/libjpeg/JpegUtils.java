package com.army.libjpeg;

import android.graphics.Bitmap;

public class JpegUtils {
	static {
		System.loadLibrary("jpeg");
		System.loadLibrary("jpegbither");
	}

	public static native String compressBitmap(Bitmap bitmap, int w, int h, int quality, byte[] filename,
			boolean optimize);
}
