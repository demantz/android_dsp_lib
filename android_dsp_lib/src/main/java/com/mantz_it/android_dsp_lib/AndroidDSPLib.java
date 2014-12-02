package com.mantz_it.android_dsp_lib;

import android.content.Context;
import android.content.res.Resources;
import android.renderscript.RenderScript;
import android.util.Log;

/**
 * Created by dennis on 20/11/14.
 */
public class AndroidDSPLib {
	private static Resources resources = null;
	private static RenderScript renderScript = null;
	private static final String LOGTAG = "ResSingleton";

	public static void init(Resources res, RenderScript rs) {
		resources = res;
		renderScript = rs;
	}

	public static void init(Context context) {
		resources = context.getResources();
		renderScript = RenderScript.create(context);
	}

	public static Resources getResources() {
		if(resources == null)
			Log.e(LOGTAG, "getResources: resources is null!");
		return resources;
	}

	public static RenderScript getRenderScript() {
		if(renderScript == null)
			Log.e(LOGTAG, "getResources: renderScript is null!");
		return renderScript;
	}
}
