package com.example.fileupload;

import java.io.File;
import java.io.FileNotFoundException;

import com.example.fileupload.FileUploadAsynTask.FileUploadProgressListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends FragmentActivity  implements FileUploadProgressListener{

	private static final int FILE_UPLOAD_REQUEST_CODE = 101;
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		progressBar = (ProgressBar)findViewById(R.id.progressBarOne);
	}

	public void uploadFileClicked(View view) {
		openFilePicker(this, "*/*", "Select File", FILE_UPLOAD_REQUEST_CODE);
	}

	public void openFilePicker(Activity context, String mimeType,
			String heading, int requestCode) {

		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(mimeType);
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
		sIntent.putExtra("CONTENT_TYPE", mimeType);
		sIntent.addCategory(Intent.CATEGORY_DEFAULT);

		Intent chooserIntent;

		try {

			chooserIntent = Intent.createChooser(intent, heading);
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
					new Intent[] { sIntent });
			context.startActivityForResult(chooserIntent, requestCode);
		} catch (Exception ex) {
			Toast.makeText(context, "Unable To find The Appropriate App",
					Toast.LENGTH_SHORT).show();

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case FILE_UPLOAD_REQUEST_CODE:

			if (data != null)
				uploadData(data);

			break;

		default:
			break;
		}
	}

	private void uploadData(Intent data) {
		Uri uri = data.getData();

		String pathFromUri = getPathForFileScheme(this, uri);

		String fileName = null;

		long fileSize = 0;

		if (pathFromUri != null) {
			fileName = uri.getLastPathSegment();

			File resumeFile = new File(pathFromUri);
			fileSize = resumeFile.length();

		} else {

			fileName = getPropertyFromUri(MediaStore.MediaColumns.DISPLAY_NAME,
					this.getApplicationContext(), uri);
			String sizeOfFile = getPropertyFromUri(
					MediaStore.MediaColumns.SIZE, this.getApplicationContext(),
					uri);

			try {

				fileSize = Long.valueOf(sizeOfFile);

			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();

			}

		}
		
		if (TextUtils.isEmpty(fileName) ) {

			Toast.makeText(this.getApplicationContext(), "There is technical error please try again",Toast.LENGTH_LONG).show();
			return ;

		}
		
		FileUploadAsynTask fileUploadTask = new FileUploadAsynTask(this, (int)fileSize);
		try {
			fileUploadTask.execute(getContentResolver().openInputStream(uri));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String getPropertyFromUri(String property, Context context,
			Uri uri) {
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { property };
			Cursor cursor = null;

			try {
				cursor = context.getContentResolver().query(uri, projection,
						null, null, null);
				int column_index = cursor.getColumnIndexOrThrow(property);
				if (cursor.moveToFirst()) {

					String value = cursor.getString(column_index);
					return value;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null)
					cursor.close();
			}
		}

		return null;
	}

	public static final String getPathForFileScheme(Context context, Uri uri) {
		if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}

	@Override
	public void onUploadProgress(int downloadProgress) {
		
		progressBar.setProgress(downloadProgress);
		
	}

}
