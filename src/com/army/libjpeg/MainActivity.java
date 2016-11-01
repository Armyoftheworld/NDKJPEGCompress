package com.army.libjpeg;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
	private EditText text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		text = (EditText) findViewById(R.id.text);
	}

	public void chooseImage(View view) {
		if (ActivityCompat.checkSelfPermission(this,
				Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 20);
			return;
		}
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			startActivityForResult(Intent.createChooser(intent, "please choose a image"), 20);
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), "please install a file manager", Toast.LENGTH_SHORT).show();
			;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == 20) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				try {
					startActivityForResult(Intent.createChooser(intent, "please choose a image"), 20);
				} catch (Exception e) {
					Toast.makeText(getApplicationContext(), "please install a file manager", Toast.LENGTH_SHORT).show();
					;
				}
			} else {
				Toast.makeText(getApplicationContext(), "please open permission", Toast.LENGTH_SHORT).show();
			}
		} else {
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 20 && resultCode == RESULT_OK) {
			Uri uri = data.getData();
			String path = getImageAbsolutePath(this, uri);
			try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
				if (bitmap != null) {
					File file = new File(getExternalCacheDir(), "JPEG压缩.jpg");
					if (file.exists()) {
						file.delete();
					}
					File file2 = new File(getExternalCacheDir(), "采样比压缩.jpg");
					if (file2.exists()) {
						file2.delete();
					}
					JpegUtils.compressBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(),
							Integer.parseInt(text.getText().toString()), file.getAbsolutePath().getBytes(), true);
					revitionImageSize(path).compress(CompressFormat.JPEG, 100, new FileOutputStream(file2));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static int imgWidth = 960;
	public static int imgHeight = 1280;

	public Bitmap revitionImageSize(String path) throws IOException {
		System.out.println("path==" + path);
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)));
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, options);
		in.close();
		int i = 0;
		Bitmap bitmap = null;
		while (true) {
			if ((options.outWidth >> i <= imgWidth) && (options.outHeight >> i <= imgHeight)) {
				in = new BufferedInputStream(new FileInputStream(new File(path)));
				options.inSampleSize = (int) Math.pow(2.0D, i);
				options.inJustDecodeBounds = false;
				options.inScaled = true;
				bitmap = BitmapFactory.decodeStream(in, null, options);
				break;
			}
			i += 1;
		}
		return bitmap;
	}
	
	public String getPathFromUri(Uri uri){
		String path="";
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		if(cursor!=null&&cursor.moveToFirst()){
			
		}
		return path;
	}

	@SuppressLint("NewApi")
	public static String getImageAbsolutePath(Activity context, Uri imageUri) {
		if (context == null || imageUri == null)
			return null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT
				&& DocumentsContract.isDocumentUri(context, imageUri)) {
			if (isExternalStorageDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			} else if (isDownloadsDocument(imageUri)) {
				String id = DocumentsContract.getDocumentId(imageUri);
				Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			} else if (isMediaDocument(imageUri)) {
				String docId = DocumentsContract.getDocumentId(imageUri);
				String[] split = docId.split(":");
				String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				String selection = MediaStore.Images.Media._ID + "=?";
				String[] selectionArgs = new String[] { split[1] };
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		} // MediaStore (and general)
		else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(imageUri))
				return imageUri.getLastPathSegment();
			return getDataColumn(context, imageUri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
			return imageUri.getPath();
		}
		return null;
	}

	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		String column = MediaStore.Images.Media.DATA;
		String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}
}
