package com.xposed.miuiime;

import android.os.Build;
import android.view.inputmethod.InputMethodManager;

import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

public class MainHook implements IXposedHookLoadPackage {
    final static List<String> miuiImeList = Arrays.asList("com.iflytek.inputmethod.miui", "com.sohu.inputmethod.sogou.xiaomi", "com.baidu.input_mi", "com.miui.catcherpatch");
    boolean isA10;
    boolean isA11;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookMethod("android.inputmethodservice.InputMethodService", lpparam.classLoader, "initViews", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                checkVersion();
                if (isA10 || isA11) {
                    final boolean isNonCustomize = !miuiImeList.contains(lpparam.packageName);
                    if (isNonCustomize) {
                        XposedBridge.log("Hook ServiceInjector: " + lpparam.packageName);
                        Class<?> clazz = findClass("android.inputmethodservice.InputMethodServiceInjector", lpparam.classLoader);
                        setsIsImeSupport(clazz);
                        if (isA10)
                            findAndHookMethod(clazz, "isXiaoAiEnable", XC_MethodReplacement.returnConstant(false));
//                            findAndHookMethod(clazz, "isImeSupport", Context.class, setsIsImeSupport(clazz));
//                        else
//                            findAndHookMethod(clazz, "isCanLoadPlugin", Context.class, setsIsImeSupport(clazz));

                        findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader, "setNavigationBarColor", int.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            0xFF141414,0xFFA1A1A1,0x66A1A1A1
//                            0xFFE7E8EB,0x66000000,0x80000000
                                callStaticMethod(clazz, "customizeBottomViewColor", true, param.args[0], 0xff747474, 0x66747474);
                            }
                        });
                    }
                    if (isA10) {
                        findAndHookMethod("android.inputmethodservice.InputMethodServiceInjector$MiuiSwitchInputMethodListener", lpparam.classLoader, "deleteNotSupportIme", XC_MethodReplacement.returnConstant(null));
                    } else {
                        InputMethodManager mImm = (InputMethodManager) getObjectField(param.thisObject, "mImm");
                        findAndHookMethod("android.inputmethodservice.InputMethodModuleManager", lpparam.classLoader, "loadDex", ClassLoader.class, String.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                XposedBridge.log("Hook MiuiBottomView: " + lpparam.packageName);
                                final Class<?> clazz = findClass("com.miui.inputmethod.InputMethodBottomManager", (ClassLoader) param.args[0]);
                                if (isNonCustomize) {
                                    setsIsImeSupport(clazz);
                                    findAndHookMethod(clazz, "isXiaoAiEnable", XC_MethodReplacement.returnConstant(false));
//                                    findAndHookMethod(clazz, "checkMiuiBottomSupport", setsIsImeSupport(clazz));
                                }
                                if (mImm != null) {
                                    XposedBridge.log("Hook getSupportIme Method: " + lpparam.packageName);
                                    findAndHookMethod(clazz, "getSupportIme", XC_MethodReplacement.returnConstant(mImm.getEnabledInputMethodList()));
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void setsIsImeSupport(Class<?> clazz) {
        XposedHelpers.setStaticIntField(clazz, "sIsImeSupport", 1);
    }

    public void checkVersion() {
        switch (Build.VERSION.SDK_INT) {
            case 30:
                isA10 = false;
                isA11 = true;
                break;
            case 29:
                isA10 = true;
                isA11 = false;
                break;
            default:
                isA10 = false;
                isA11 = false;
        }
    }
}