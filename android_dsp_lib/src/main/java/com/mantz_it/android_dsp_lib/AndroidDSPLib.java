package com.mantz_it.android_dsp_lib;

import android.content.Context;
import android.content.res.Resources;
import android.support.v8.renderscript.RenderScript;
import android.util.Log;

/**
 * Android DSP library - Android DSP Library Singleton
 *
 * Module:      AndroidDSPLib.java
 * Description: This class is a Singleton holding references to the RenderScript and Resources
 *              Whenever this library is used. The init() function of this class has to be called first!
 *
 * @author Dennis Mantz
 *
 * Copyright (C) 2014 Dennis Mantz
 * License: http://www.gnu.org/licenses/gpl.html GPL version 2 or higher
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
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
