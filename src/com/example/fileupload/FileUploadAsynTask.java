package com.example.fileupload;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.ParseException;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class FileUploadAsynTask extends AsyncTask<Object, Integer, Integer>{

	
	
	private String fileUploadUrl = "http://androidinterview.ddns.net/candidate/interview/api/4rss/document/upload";
	private FileUploadProgressListener listener;
	private int byteTransferred=0;
	private int bytesAvailable=0;
	private int fileSize;
	
	public FileUploadAsynTask(FileUploadProgressListener downloadListener,int fileSize) {
		this.listener = downloadListener;
		this.fileSize = fileSize;
	}
	@Override
	protected Integer doInBackground(Object... params) {
		
		try {
			
			String response = multipartRequest(fileUploadUrl, "",(InputStream) params[0], "uploadedFile");
			parseJson(response);
			
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return 1;	
	}

	private int parseJson(String response) {
		
		int responsOfApi=  0;//0 failure , 1 success
		JSONObject jsonResponse;
		try {
			jsonResponse = new JSONObject(response);
			if ( jsonResponse.isNull("status") )
			{
					int responseCode = jsonResponse.getInt("status");
					if (responseCode == 200 ) return 1;
					
					
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return responsOfApi;
		
	}

	public static interface FileUploadProgressListener{
		 public void onUploadProgress(int downloadProgress);
	}
	public String multipartRequest(String urlTo, String post,InputStream fileInputStream, String filefield) throws ParseException, IOException {
		HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		InputStream inputStream = null;
		
		String twoHyphens = "--";
		String boundary =  "*****"+Long.toString(System.currentTimeMillis())+"*****";
		String lineEnd = "\r\n";
		
		String result = "";
		
		int bytesRead, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1*1024*1024;
		
		try {
			
			URL url = new URL(urlTo);
			connection = (HttpURLConnection) url.openConnection();
			
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("User-Agent", "Android Multipart HTTP Client 1.0");
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
			
			outputStream = new DataOutputStream(connection.getOutputStream());
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"" + filefield + "\"; filename=\"" +"image"+"\"" + lineEnd);
			outputStream.writeBytes("Content-Type: image/jpeg" + lineEnd);
			outputStream.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
			outputStream.writeBytes(lineEnd);
			
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];
			
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			while(bytesRead > 0) {
				
				outputStream.write(buffer, 0, bufferSize);
				byteTransferred += bytesRead;
				publishProgress(bytesRead);
				
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
			
			outputStream.writeBytes(lineEnd);
			
			
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			
			inputStream = connection.getInputStream();
			result = this.convertStreamToString(inputStream);
			
			fileInputStream.close();
			inputStream.close();
			outputStream.flush();
			outputStream.close();
			
			return result;
		} catch(Exception e) {
			Log.e("MultipartRequest","Multipart Form Upload Error");
			e.printStackTrace();
			return "error";
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		
		super.onProgressUpdate(values);
		listener.onUploadProgress(byteTransferred/fileSize);
		
		
		
	}
	private String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
