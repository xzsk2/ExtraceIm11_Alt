package com.track.ui.main;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.track.app.user.R;
import com.track.util.ExTraceApplication;
import com.track.util.Utility;

/**
 * 登陆活动
 */
public class LoginActivity extends AppCompatActivity {
	// 进度对话框模块
	private ProgressDialog prgDialog;
	private TextView errorMsg;
	private EditText unameET;
	private EditText pwdET;
	private ImageView mImageViewBack;
	private String url;
	private static JSONObject obj;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		/*
		mImageViewBack = (ImageView) findViewById(R.id.id_iv_back);
		mImageViewBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent loginIntent = new Intent(getApplicationContext(),
						MainActivity.class);
				loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(loginIntent);
			}
		});
		*/

		errorMsg = (TextView) findViewById(R.id.login_error);
		unameET = (EditText) findViewById(R.id.loginUsername);
		pwdET = (EditText) findViewById(R.id.loginPassword);
		// 初始化 Progress Dialog object
		prgDialog = new ProgressDialog(this);
		// 设置 Progress Dialog 文本
		prgDialog.setMessage("Please wait...");
		prgDialog.setCancelable(false);
	}

	/**
	 * 登陆按钮被触发
	 * 
	 * @param view
	 */
	public void loginUser(View view) {
		String uname = unameET.getText().toString();
		String pwd = pwdET.getText().toString();
		url = ((ExTraceApplication) getApplication()).getMiscServiceUrl();
		RequestParams params = new RequestParams();
		if (Utility.isNotNull(uname) && Utility.isNotNull(pwd)) {
			params.put("uname", uname);
			params.put("pwd", pwd);
			invokeWS(uname,pwd);
		} else {
			Toast.makeText(getApplicationContext(), "您还没有输入用户名或密码！",
					Toast.LENGTH_LONG).show();
		}
	}

	private void setInfotoSp(String uname, String pwd) {
		SharedPreferences sharedPreferences = getSharedPreferences("userInfo",
				Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();// 获取编辑器
		editor.putString("uname", uname);
		editor.putString("password", pwd);
		editor.commit();// 提交修改
	}

	/**
	 * 与webservice通信的方法
	 */
	public void invokeWS(String username,String password) {
		// 显示 Progress Dialog
		prgDialog.show();
		AsyncHttpClient client = new AsyncHttpClient();
		url += "doLogin/"+username+"/"+password+"?_type=json";
		Log.d("Login", "invokeWS: "+url);
		client.get(url, new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(int statusCode, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				// TODO Auto-generated method stub
				prgDialog.dismiss();
				// When Http response code is '404'
				if (statusCode == 404) {
					Toast.makeText(getApplicationContext(), "资源未找到",
							Toast.LENGTH_LONG).show();
				}
				// When Http response code is '500'
				else if (statusCode == 500) {
					Toast.makeText(getApplicationContext(), "服务器发生异常",
							Toast.LENGTH_LONG).show();
				}
				// When Http response code other than 404, 500
				else {
					Toast.makeText(getApplicationContext(),
							"设备网络异常或服务器为开启！" + statusCode + " " + url,
							Toast.LENGTH_LONG).show();
				}
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				// TODO Auto-generated method stub
				// 隐藏 Progress Dialog
				prgDialog.dismiss();
				String uname = unameET.getText().toString();
				String pwd = pwdET.getText().toString();
				setInfotoSp(uname, pwd);
				try {
					// 将服务端返回的字符串转换成JSON Object
					String str = new String(arg2);
					obj = new JSONObject(str);
					if (obj.getBoolean("status")) {
						Toast.makeText(getApplicationContext(), "登录成功!",
								Toast.LENGTH_LONG).show();
						// 进入HomeActivity
						((ExTraceApplication) getApplication()).getUserInfo();
						navigatetoMainActivity();
					} else {
						errorMsg.setText(obj.getString("error_msg"));
						Toast.makeText(getApplicationContext(),
								obj.getString("error_msg"), Toast.LENGTH_LONG)
								.show();
					}
				} catch (JSONException e) {
					Toast.makeText(
							getApplicationContext(),
							"Error Occured [Server's JSON response might be invalid]!",
							Toast.LENGTH_LONG).show();
					e.printStackTrace();

				}
			}

		});

	}

	/**
	 * 从LoginActivity跳转MainActivity
	 * 
	 * @throws JSONException
	 */
	public void navigatetoMainActivity() throws JSONException {
		Intent mainIntent = new Intent(getApplicationContext(),
				MainActivity.class);
		// mainIntent.putExtra("uname", obj.getString("username"));
		mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(mainIntent);
	}

	/**
	 * 注册按钮被触发
	 * 
	 * @param view
	 */
	public void navigatetoRegisterActivity(View view) {
		Intent loginIntent = new Intent(getApplicationContext(),
				RegisterActivity.class);
		loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(loginIntent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case android.R.id.home:
				/*
				Intent loginIntent = new Intent(getApplicationContext(),
						MainActivity.class);
				loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(loginIntent);
				*/
				finish();
				return true;
				default:
					return super.onOptionsItemSelected(item);
		}
	}
}
