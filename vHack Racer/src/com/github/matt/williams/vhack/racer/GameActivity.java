package com.github.matt.williams.vhack.racer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class GameActivity extends Activity implements GLSurfaceView.Renderer,
		ConnectionCallback {

	private static final String TAG = "GameActivity";
	public static final String EXTRA_CONNECT = "Connect";
	private GLSurfaceView mGLSurfaceView;
	private int mWidth;
	private int mHeight;
	private Texture mTerrainTexture;
	private Texture mMapTexture;
	private Program mMapProgram;
	private float[] mViewMatrix = new float[16];
	private float[] mRotationMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];
	private float[] mPVMatrix = new float[16];
	private AccelerometerController mAccelerometerController;
	private AccelerometerEventReceiver mAccelerometerEventReceiver;
	private Program mSkyboxProgram;
	private Texture mSkyboxTexture;
	private Program mPointProgram;
	private Texture mBananaTexture;
	private Kart mKart;
	private List<Kart> mKarts = new ArrayList<Kart>();
	private Map mMap;
	private Texture mTuxTexture;
	private Program mTuxProgram;
	private static final float[] UV_COORDS = new float[768];
	private TextView mLapBoard;
	private int mCurrentLap;
	private Handler mLapHandler = new Handler();
	private boolean mLapsFinished;
	private TextView mFinished;
	private int totalLapTime;
	private Handler mTimerHandler = new Handler();
	private TextView mTimer;
	private SharedPreferences mUserHighScore;
	private SharedPreferences.Editor mUserHighScoreEditor;
	private int mBestTime;
	private static final String HIGH_SCORE_PREF = "high score";
	private static final String NAME_PREF = "name";
	private AlertDialog.Builder mAlert;
	private EditText mHighScoreInput;
	private String mTotalLapTimeFormatted;
	private String mUserName;
	private Handler mGameEndHandler = new Handler();
	static {
		for (int coordIndex = 0; coordIndex < UV_COORDS.length; coordIndex += 12) {
			UV_COORDS[coordIndex] = 0;
			UV_COORDS[coordIndex + 1] = 1;
			UV_COORDS[coordIndex + 2] = 1;
			UV_COORDS[coordIndex + 3] = 1;
			UV_COORDS[coordIndex + 4] = 1;
			UV_COORDS[coordIndex + 5] = 0;
			UV_COORDS[coordIndex + 6] = 1;
			UV_COORDS[coordIndex + 7] = 0;
			UV_COORDS[coordIndex + 8] = 0;
			UV_COORDS[coordIndex + 9] = 1;
			UV_COORDS[coordIndex + 10] = 0;
			UV_COORDS[coordIndex + 11] = 0;
		}
	}
	private AccelerometerEventBroadcaster mAccelerometerEventBroadcaster;
	private HapticsController mHapticsController;
	private SonyRemoteController mSonyRemoteController;
	private SoundController mSoundController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);

		mLapBoard = (TextView) findViewById(R.id.lapBoard);
		mFinished = (TextView) findViewById(R.id.finished);
		mTimer = (TextView) findViewById(R.id.timer);

		mAlert = new AlertDialog.Builder(this);
		mHighScoreInput = new EditText(this);

		mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setRenderer(this);
		mKart = new Kart("Matt", 19.8f, -23.9f, (float) (Math.PI / 2));
		mKarts.add(mKart);
		mKarts.add(new Kart("Alice", 21.875f, -28.1f, (float) (Math.PI / 2)));
		mKarts.add(new Kart("Bob", 23.96f, -23.9f, (float) (Math.PI / 2)));
		mKarts.add(new Kart("Charlie", 26.04f, -28.1f, (float) (Math.PI / 2)));
		Intent intent = getIntent();
		boolean connect = ((intent != null) && (intent.getBooleanExtra(
				EXTRA_CONNECT, false)));
		if (getPackageManager().hasSystemFeature("com.google.android.tv")) {
			if (connect) {
				mSonyRemoteController = new SonyRemoteController(this, mKart);
			} else {
				mAccelerometerEventReceiver = new AccelerometerEventReceiver(
						mKart);
			}
			mSoundController = new SoundController(this);
		} else {
			ControllerCallback controllerCallback;
			if (connect) {
				mAccelerometerEventBroadcaster = new AccelerometerEventBroadcaster(
						this);
				controllerCallback = mAccelerometerEventBroadcaster;
			} else {
				controllerCallback = mKart;
				mHapticsController = new HapticsController(this, mKart);
				mSoundController = new SoundController(this);
			}
			mAccelerometerController = new AccelerometerController(
					(SensorManager) getSystemService(Context.SENSOR_SERVICE),
					controllerCallback);
		}

		// lap counter
		mCurrentLap = mKart.getLapCount();
		mLapHandler.postDelayed(mLapBoardHandler, 1000);

		// setup for the user's high score
		mUserHighScore = getSharedPreferences(HIGH_SCORE_PREF,
				Context.MODE_PRIVATE);
		mUserHighScoreEditor = mUserHighScore.edit();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mAccelerometerEventBroadcaster != null) {
			mAccelerometerEventBroadcaster.start();
		}
		if (mAccelerometerController != null) {
			mAccelerometerController.start();
		}
		if (mAccelerometerEventReceiver != null) {
			mAccelerometerEventReceiver.start();
		}
		if (mHapticsController != null) {
			mHapticsController.start();
		}
		if (mSoundController != null) {
			mSoundController.start();
		}

		mTimerHandler.post(mLapTimerHandler);

		mBestTime = mUserHighScore.getInt(HIGH_SCORE_PREF, 0);
		mUserName = mUserHighScore.getString(NAME_PREF, "Matt");
	}

	@Override
	public void onPause() {
		if (mAccelerometerEventBroadcaster != null) {
			mAccelerometerEventBroadcaster.shutdown();
		}
		if (mAccelerometerController != null) {
			mAccelerometerController.stop();
		}
		if (mAccelerometerEventReceiver != null) {
			mAccelerometerEventReceiver.stop();
		}
		if (mHapticsController != null) {
			mHapticsController.stop();
		}
		if (mSoundController != null) {
			mSoundController.stop();
		}
		super.onPause();
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig cfg) {
		Resources resources = getResources();
		mSkyboxTexture = new Texture(BitmapFactory.decodeResource(resources,
				R.drawable.skybox));
		mSkyboxProgram = new Program(new VertexShader(
				resources.getString(R.string.skyboxVertexShader)),
				new FragmentShader(resources
						.getString(R.string.skyboxFragmentShader)));
		mSkyboxProgram.setVertexAttrib("xyz", new float[] { -50, 90, -50, -50,
				-90, -50, 50, 90, -50, 50, -90, -50, 50, 90, 50, 50, -90, 50,
				-50, 90, 50, -50, -90, 50, -50, 90, -50, -50, -90, -50 }, 3);
		mSkyboxProgram.setVertexAttrib("uv", new float[] { 0, 0, 0, 1, 0.25f,
				0, 0.25f, 1, 0.5f, 0, 0.5f, 1, 0.75f, 0, 0.75f, 1, 1.0f, 0,
				1.0f, 1 }, 2);
		mSkyboxTexture.use(GLES20.GL_TEXTURE0);
		mSkyboxProgram.setUniform("skybox", 0);

		mTerrainTexture = new Texture(BitmapFactory.decodeResource(resources,
				R.drawable.terrain));
		// Bitmap mapBitmap =
		// BitmapFactory.decodeStream(resources.openRawResource(R.raw.map));
		Bitmap mapBitmap = Bitmap.createBitmap(MapData.DATA, 64, 64,
				Bitmap.Config.ARGB_8888);
		mMap = new Map(mapBitmap);
		mMapTexture = new Texture(mapBitmap);
		mMapProgram = new Program(new VertexShader(
				resources.getString(R.string.mapVertexShader)),
				new FragmentShader(resources
						.getString(R.string.mapFragmentShader)));
		mTerrainTexture.use(GLES20.GL_TEXTURE0);
		mMapProgram.setUniform("terrain", 0);
		mMapProgram.setUniform("terrainSize", 1.0f / 40, 1.0f / 40);// Should
																	// be20.0f/mTerrainTexture.getWidth(),
																	// 20.0f/mTerrainTexture.getHeight());
																	// but
																	// getWidth()
																	// and
																	// getHeight()
																	// return
																	// 1200!
		mMapTexture.use(GLES20.GL_TEXTURE1);
		mMapProgram.setUniform("map", 1);
		mMapProgram.setUniform("mapSize", 1.0f / mMapTexture.getWidth(),
				1.0f / mMapTexture.getHeight());

		mBananaTexture = new Texture(BitmapFactory.decodeResource(resources,
				R.drawable.banana));
		mPointProgram = new Program(new VertexShader(
				resources.getString(R.string.pointVertexShader)),
				new FragmentShader(resources
						.getString(R.string.pointFragmentShader)));
		mBananaTexture.use(GLES20.GL_TEXTURE0);
		mPointProgram.setUniform("billboard", 0);

		mTuxTexture = new Texture(BitmapFactory.decodeResource(resources,
				R.drawable.tux), GLES20.GL_REPEAT);
		mTuxProgram = new Program(new VertexShader(
				resources.getString(R.string.tuxVertexShader)),
				new FragmentShader(resources
						.getString(R.string.pointFragmentShader)));
		mTuxTexture.use(GLES20.GL_TEXTURE0);
		mTuxProgram.setUniform("billboard", 0);

		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		GLES20.glClearDepthf(100.0f);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mWidth = width;
		mHeight = height;

		// Create a new perspective projection matrix. The height will stay the
		// same
		// while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 100.0f;

		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near,
				far);
	}

	public void onDrawFrame(GL10 gl) {
		mKart.update(mMap);

		// check if the user has completed a lap
		int currentLap = mKart.getLapCount();
		if (currentLap > mCurrentLap && currentLap <= Map.TOTAL_LAPS) {
			mCurrentLap = currentLap;

			if (currentLap == Map.TOTAL_LAPS) {
				mLapsFinished = true;
			}

			mLapHandler.post(mLapBoardHandler);
		}
		// if we are finished the course we don't need to update the lap counter
		if (currentLap > Map.TOTAL_LAPS) {
			mLapHandler.removeCallbacks(mLapBoardHandler);
			mTimerHandler.removeCallbacks(mLapTimerHandler);
		}

		float orientation = mKart.getOrientation();
		float position[] = mKart.getPosition();

		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		GLES20.glViewport(0, 0, mWidth, mHeight);

		Matrix.setIdentityM(mViewMatrix, 0);
		Matrix.rotateM(mViewMatrix, 0, (float) (orientation * 180 / Math.PI),
				0.0f, 1.0f, 0.0f);
		Matrix.multiplyMM(mPVMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthMask(false);

		mSkyboxProgram.setUniform("matrix", mPVMatrix);
		mSkyboxTexture.use(GLES20.GL_TEXTURE0);

		mSkyboxProgram.setVertexAttrib("xyz", new float[] { -50, 90, -50, -50,
				-90, -50, 50, 90, -50, 50, -90, -50, 50, 90, 50, 50, -90, 50,
				-50, 90, 50, -50, -90, 50, -50, 90, -50, -50, -90, -50 }, 3);
		mSkyboxProgram.setVertexAttrib("uv", new float[] { 0, 0, 0, 1, 0.25f,
				0, 0.25f, 1, 0.5f, 0, 0.5f, 1, 0.75f, 0, 0.75f, 1, 1.0f, 0,
				1.0f, 1 }, 2);
		mSkyboxProgram.setUniform("skybox", 0);
		mSkyboxProgram.use();
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 10);
		Utils.checkErrors("glDrawArrays");

		Matrix.setIdentityM(mViewMatrix, 0);
		Matrix.translateM(mViewMatrix, 0, 0, 0, -2);
		Matrix.rotateM(mViewMatrix, 0, (float) (orientation * 180 / Math.PI),
				0.0f, 1.0f, 0.0f);
		Matrix.translateM(mViewMatrix, 0, position[0], 0, position[1]);
		Matrix.multiplyMM(mPVMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
		mMapProgram.setUniform("matrix", mPVMatrix);

		mTerrainTexture.use(GLES20.GL_TEXTURE0);
		mMapTexture.use(GLES20.GL_TEXTURE1);
		mMapProgram.use();
		mMapProgram.setVertexAttrib("xz", new float[] { -100, -100, 100, -100,
				-100, 100, 100, 100 }, 2);
		mMapProgram.setVertexAttrib("uv", new float[] { -1, -1, 2, -1, -1, 2,
				2, 2 }, 2);
		mMapProgram.setUniform("terrain", 0);
		mMapProgram.setUniform("terrainSize", 1.0f / 40, 1.0f / 40);// Should
																	// be20.0f/mTerrainTexture.getWidth(),
																	// 20.0f/mTerrainTexture.getHeight());
																	// but
																	// getWidth()
																	// and
																	// getHeight()
																	// return
																	// 1200!
		mMapProgram.setUniform("map", 1);
		mMapProgram.setUniform("mapSize", 1.0f / mMapTexture.getWidth(),
				1.0f / mMapTexture.getHeight());
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		Utils.checkErrors("glDrawArrays");

		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthMask(true);
		mBananaTexture.use(GLES20.GL_TEXTURE0);
		Matrix.setIdentityM(mRotationMatrix, 0);
		Matrix.rotateM(mRotationMatrix, 0,
				(float) (-orientation * 180 / Math.PI), 0.0f, 1.0f, 0.0f);
		mPointProgram.setUniform("matrix", mPVMatrix);
		mPointProgram.setUniform("rotation", mRotationMatrix);
		float[] points = new float[] { 0, 30, 0, -30, 30, 0, -30, 0 };
		float[] vertices = pointsToVertices(points, 2);
		mPointProgram.setVertexAttrib("xz", vertices, 2);
		mPointProgram.setVertexAttrib("uv", UV_COORDS, 2);
		mPointProgram.use();
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length / 2);
		Utils.checkErrors("glDrawArrays");

		mTuxTexture.use(GLES20.GL_TEXTURE0);
		mTuxProgram.setUniform("matrix", mPVMatrix);
		mTuxProgram.setUniform("rotation", mRotationMatrix);
		mTuxProgram.setUniform("orientation", mKart.getOrientation());
		points = new float[4 * mKarts.size()];
		int pointIndex = 0;
		for (Kart kart : mKarts) {
			points[pointIndex++] = -kart.getPosition()[0];
			points[pointIndex++] = -kart.getPosition()[1];
			points[pointIndex++] = kart.getPosition()[2];
			points[pointIndex++] = kart.getOrientation();
		}
		vertices = pointsToVertices(points, 4);
		mTuxProgram.setVertexAttrib("xzyr", vertices, 4);
		mTuxProgram.setVertexAttrib("uv", UV_COORDS, 2);
		mTuxProgram.use();
		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices.length / 4);
		Utils.checkErrors("glDrawArrays");
	}

	private float[] pointsToVertices(float[] points, int size) {
		float[] vertices = new float[points.length * 6];
		int vertexIndex = 0;
		for (int pointIndex = 0; pointIndex < points.length / size; pointIndex++) {
			for (int index = 0; index < 6; index++) {
				for (int subIndex = 0; subIndex < size; subIndex++) {
					vertices[vertexIndex++] = points[pointIndex * size
							+ subIndex];
				}
			}
		}
		return vertices;
	}

	public void onConnected() {
		new AlertDialog.Builder(this).setTitle("TV Connected OK")
				.setMessage("Yay!").create();
	}

	public void onConnectionFailed() {
		new AlertDialog.Builder(this).setTitle("TV Connection Failed")
				.setMessage("Check your network connection and retry")
				.setNeutralButton("OK", new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						finish();
					}
				}).create();
	}

	Runnable mLapBoardHandler = new Runnable() {
		public void run() {
			mLapBoard.setText(mCurrentLap + "/" + Map.TOTAL_LAPS);
			if (mLapsFinished) {
				mFinished.setVisibility(View.VISIBLE);

				// if the user has beaten their previous best time notify them
				// or if the user doesn't have a best score yet then ask them to
				// record their score
				if (mBestTime > totalLapTime || mBestTime == 0) {

					mAlert.setTitle("High Score");
					mAlert.setMessage("Congratulations you have a new best time of "
							+ mTotalLapTimeFormatted
							+ ". Please eneter your name:");

					// Set an EditText view to get user input
					mAlert.setView(mHighScoreInput);
					mHighScoreInput.setText(mUserName);

					mAlert.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String value = mHighScoreInput.getText()
											.toString();

									mUserHighScoreEditor.putString(NAME_PREF,
											value);
									mUserHighScoreEditor.putInt(
											HIGH_SCORE_PREF, totalLapTime);
									mUserHighScoreEditor.commit();

									// Take the user back to the main menu
									mGameEndHandler.postDelayed(mEndOfGameHandler, 3000);
								}
							});

					mAlert.show();
				} else if (totalLapTime > mBestTime) {
					// The user didn't beat their best time so just take them back to the main menu
					mGameEndHandler.postDelayed(mEndOfGameHandler, 3000);
				}
			}
		}
	};

	Runnable mLapTimerHandler = new Runnable() {
		public void run() {
			// if we haven't finished the course then record how long the user is taking
			if (mCurrentLap < Map.TOTAL_LAPS) {
				// update the time passed
				totalLapTime++;
	
				int minutes = (int) Math.floor(totalLapTime / 60);
				int seconds = totalLapTime % 60;
				// format 0-9 seconds to 0:09 etc
				String padding = "";
				if (seconds < 10) {
					padding = "0";
				}
	
				mTotalLapTimeFormatted = minutes + ":" + padding + seconds;
	
				mTimer.setText(mTotalLapTimeFormatted);
	
				// update the timer every second
				mTimerHandler.postDelayed(mLapTimerHandler, 1000);
			}
		}
	};

	Runnable mEndOfGameHandler = new Runnable() {
		public void run() {
			Log.d("Racer", "game over");
			
			finish();
		}
	};
}
