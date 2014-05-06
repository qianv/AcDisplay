/*
 * Copyright (C) 2014 AChep@xda <artemchep@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package com.achep.activedisplay.xposed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class OverrideHomeButton implements IXposedHookZygoteInit {

    static boolean active = false;
    public static final String INTENT_EAT_HOME_PRESS_START = "com.achep.acdisplay.EAT_HOME_PRESS_START";
    public static final String INTENT_EAT_HOME_PRESS_STOP = "com.achep.acdisplay.EAT_HOME_PRESS_STOP";

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

        /**
        * Register BroadcastReceiver in PhoneWindowManager.init(…) so that we
        * can enable and disable the home button on demand.
        */
        findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "init",
                Context.class, "android.view.IWindowManager", "android.view.WindowManagerPolicy.WindowManagerFuncs",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        BroadcastReceiver mAcDisplayReceiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                if (intent.getAction().equals(INTENT_EAT_HOME_PRESS_START))
                                    active = true;
                                else if (intent.getAction().equals(EAT_HOME_PRESS_STOP))
                                    active = false;
                            }
                        };
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(EAT_HOME_PRESS_START);
                        filter.addAction(EAT_HOME_PRESS_STOP);
                        Context context = (Context) getObjectField(param.thisObject, "mContext");
                        context.registerReceiver(mAcDisplayReceiver, filter);
                    }
                }

        );

        /**
         * If active, have PhoneWindowManager.launchHomeFromHotKey() do nothing.
         */
        findAndHookMethod("com.android.internal.policy.impl.PhoneWindowManager", null, "launchHomeFromHotKey",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (active)
                            param.setResult(null);
                    }
                }

        );
    }
}
