package com.autotest.sonicclient.handler;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.enums.AndroidKey;
import com.autotest.sonicclient.enums.ConditionEnum;
import com.autotest.sonicclient.enums.ErrorType;
import com.autotest.sonicclient.enums.SonicEnum;
import com.autotest.sonicclient.enums.StepType;
import com.autotest.sonicclient.interfaces.Assemble;
import com.autotest.sonicclient.interfaces.HandlerService;
import com.autotest.sonicclient.interfaces.IStepHandler;
import com.autotest.sonicclient.model.By;
import com.autotest.sonicclient.model.ResultInfo;
import com.autotest.sonicclient.nodes.AccessibilityNodeInfoImpl;
import com.autotest.sonicclient.services.DeviceService;
import com.autotest.sonicclient.services.TServiceWrapper;
import com.autotest.sonicclient.utils.Assert;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.DeviceUtil;
import com.autotest.sonicclient.utils.DownloadTool;
import com.autotest.sonicclient.utils.GConfigParams;
import com.autotest.sonicclient.utils.GroovyHelper;
import com.autotest.sonicclient.utils.JsonParser;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.MinioUtil;
import com.autotest.sonicclient.utils.PermissionHelper;
import com.autotest.sonicclient.exceptions.SonicRespException;
import com.autotest.sonicclient.utils.ToastUtil;

import org.opencv.utils.ImageMatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@HandlerService
public class StepHandler extends StepHandlerBase implements IStepHandler {
    private static final String TAG = "StepHandler";
    @Assemble
    StepHandlerWrapper stepHandlerWrapper;
    @Assemble
    DeviceService deviceService;

    private Context context;
    private final JSONObject globalParams = new JSONObject();
    private ResultInfo resultInfo = new ResultInfo();

    public StepHandler(Context context) {
        super(context);
        this.context = context;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public void runStep(JSONObject stepJSON) throws Throwable {
        runStep(stepJSON, resultInfo);
    }

    public ResultInfo runStep(JSONObject stepJSON, ResultInfo resultInfo) throws Throwable {
        if (!waitServiceReady()) {
            return null;
        }
        LogUtil.d(TAG, String.format("runStep: stepJSON : %s", stepJSON));
        this.resultInfo = resultInfo;
        resultInfo.clearStep();
        String stepType = stepJSON.getJSONObject(Constant.KEY_STEP_INFO_STEP).getString(Constant.KEY_STEP_INFO_TYPE);
        handleStep(stepJSON);
        return resultInfo;
    }

    private boolean waitServiceReady() {
        for (int i = 0; i < 20 && !deviceService.isReady(); i++) {
            SystemClock.sleep(500);
        }
        if (!deviceService.isReady()) {
            ToastUtil.showToast(context, "服务连接失败", true);
            return false;
        }
        return true;
    }

    @Override
    public ConditionEnum getCondition() {
        return ConditionEnum.NONE;
    }


    private void handleStep(JSONObject stepJSON) throws Throwable {
        LogUtil.d(TAG, "handleStep: step: " + stepJSON);
        JSONObject step = stepJSON.getJSONObject(Constant.KEY_STEP_INFO_STEP);
        // 兼容childSteps
        if (JsonParser.isJSONObjectEmpty(step)) {
            step = stepJSON;
        }
        SystemClock.sleep(GConfigParams.holdTime);
        String eleName = null;
        String eleType = null;
        String eleValue = null;
        String eleName1 = null;
        String eleType1 = null;
        String eleValue1 = null;
        JSONArray eleList = step.getJSONArray("elements");
        if (eleList.size() > 0) {
            eleName = (String) eleList.getJSONObject(0).getOrDefault("eleName", null);
            eleType = (String) eleList.getJSONObject(0).getOrDefault("eleType", null);
            eleValue = (String) eleList.getJSONObject(0).getOrDefault("eleValue", null);
            if (eleList.size() > 1) {
                eleName1 = (String) eleList.getJSONObject(1).getOrDefault("eleName", null);
                eleType1 = (String) eleList.getJSONObject(1).getOrDefault("eleType", null);
                eleValue1 = (String) eleList.getJSONObject(1).getOrDefault("eleValue", null);
            }
        }

        String stepType = step.getString(Constant.KEY_STEP_INFO_TYPE);
        resultInfo.setStepType(stepType);
        switch (stepType) {
            case "switchTouchMode":
                switchTouchMode(step.getString("content"));
                break;
            case "appReset":
                appReset(step.getString("text"));
                break;
            case "appAutoGrantPermissions":
                appAutoGrantPermissions(step.getString("text"));
                break;
            case "stepHold":
                stepHold(step.getInteger("content"));
                break;
            case "toWebView":
                toWebView(step.getString("content"), step.getString("text"));
                break;
            case "toHandle":
                toHandle(step.getString("content"));
                break;
            case "readText":
                readText(step.getString("content"), step.getString("text"));
                break;
            case "clickByImg":
                clickByImg(eleName, eleValue);  // 未实现
                break;
            case "click":
                click(eleName, eleType, eleValue);
                break;
            case "zoom":
                zoom(eleList);
                break;
            case "getTitle":
                getTitle(step.getString("content"));  // 未实现
                break;
            case "getUrl":
                getUrl(step.getString("content"));
                break;
            case "getActivity":
                getActivity(step.getString("content"));
                break;
            case "getElementAttr":
                getElementAttr(eleName, eleType, eleValue, step.getString("text"), step.getString("content"));
                break;
            case "obtainElementAttr":
                obtainElementAttr(eleName, eleType, eleValue, step.getString("text"), step.getString("content"));
                break;
            case "logElementAttr":
                logElementAttr(eleName, eleType, eleValue, step.getString("text"));
                break;
            case "sendKeys":
                sendKeys(eleName, eleType, eleValue, step.getString("content"));
                break;
            case "sendKeysByActions":
                sendKeysByActions(eleName, eleType, eleValue, step.getString("content"));
                break;
            case "isExistEle":
                isExistEle(eleName, eleType, eleValue, step.getBoolean("content"));
                break;
            case "scrollToEle":
                scrollToEle(eleName, eleType, eleValue, step.getInteger("content"), step.getString("text"));
                break;
            case "isExistEleNum":
                isExistEleNum(eleName, eleType, eleValue, step.getString("content"), step.getInteger("text"), -1);
                break;
            case "clear":
                clear(eleName, eleType, eleValue);
                break;
            case "longPress":
                longPress(eleName, eleType, eleValue, step.getInteger("content"));
                break;
            case "swipe":
                // json值与平台有差异
                if (null == eleValue) {
                    String[] pts = step.getString("text").split("|");
                    eleValue = pts[0];
                    eleValue1 = pts[1];
                    eleName = "坐标1";
                    eleName1 = "坐标2";
                }
                swipePoint(eleName, eleValue, eleName1, eleValue1);
                break;
            case "swipe2":
                swipe(eleName, eleType, eleValue, eleName1, eleType1, eleValue1);
                break;
            case "drag":
                dragByPoint(eleName, eleValue, eleName1, eleValue1);
                break;
            case "drag2":
                dragByEle(eleName, eleType, eleValue, eleName1, eleType1, eleValue1);
                break;
            case "motionEvent":
                motionEventByEle(eleName, eleType, eleValue, step.getString("text"));
                break;
            case "motionEventByPoint":
                motionEventByPoint(eleName, eleValue, step.getString("text"));
                break;
            case "tap":
                // 临时兼容
                tap(eleName, eleValue == null ? step.getString("text") : eleValue);
                break;
            case "longPressPoint":
                longPressPoint(eleName, eleValue, step.getInteger("content"));
                break;
            case "pause":
                pause(step.getInteger("content"));
                break;
            case "swipeByDefinedDirection":
                swipeByDefinedDirection(step.getString("text"), step.getInteger("content"));
                break;
            case "checkImage":
                checkImage(eleName, eleValue, step.getDouble("content"));
                break;
            case "stepScreen":
                stepScreen();
                break;
            case "openApp":
                openApp(step.getString("text"));
                break;
            case "terminate":
                terminate(step.getString("text"));
                break;
            case "install":
                install(step.getString("text"));
                break;
            case "uninstall":
                uninstall(step.getString("text"));
                break;
            case "screenSub":
            case "screenAdd":
            case "screenAbort":
                rotateDevice(stepType);
                break;
            case "lock":
                lock();
                break;
            case "unLock":
                unLock();
                break;
            case "airPlaneMode":
                airPlaneMode(step.getBoolean("content"));
                break;
            case "wifiMode":
                wifiMode(step.getBoolean("content"));
                break;
            case "locationMode":
                locationMode(step.getBoolean("content"));
                break;
            case "keyCode":
                keyCode(step.getString("content"));
                break;
            case "keyCodeSelf":
                keyCode(step.getInteger("content"));
                break;
            case "assertEquals":
            case "assertNotEquals":
            case "assertTrue":
            case "assertNotTrue":
                String actual = TextHandler.replaceTrans(step.getString("text"), globalParams);
                String expect = TextHandler.replaceTrans(step.getString("content"), globalParams);
                asserts(actual, expect, stepType);
                break;
            case "getTextValue":
                globalParams.put(step.getString("content"), getText(eleName, eleType, eleValue));
                break;
            case "sendKeyForce":
                sendInputText(step.getString("content"));
                break;
            case "monkey":
                runMonkey(step.getJSONObject("content"), step.getJSONArray("text").toJavaList(JSONObject.class));
                break;
            case "publicStep":
                publicStep(step.getString("content"), stepJSON.getJSONArray("pubSteps"), step.getInteger("error"));
                break;
            case "setDefaultFindWebViewElementInterval":
                setDefaultFindWebViewElementInterval(step.getInteger("content"), step.getInteger("text"));
                break;
            case "webElementScrollToView":
                webElementScrollToView(eleName, eleType, eleValue);
                break;
            case "isExistWebViewEle":
                isExistWebViewEle(eleName, eleType, eleValue, step.getBoolean("content"));
                break;
            case "isExistWebViewEleNum":
                isExistEleNum(eleName, eleType, eleValue, step.getString("content"), step.getInteger("text"), -1);
                break;
            case "webViewClear":
                webViewClear(eleName, eleType, eleValue);
                break;
            case "webViewSendKeys":
                webViewSendKeys(eleName, eleType, eleValue, step.getString("content"));
                break;
            case "webViewSendKeysByActions":
                webViewSendKeysByActions(eleName, eleType, eleValue, step.getString("content"));
                break;
            case "webViewClick":
                webViewClick(eleName, eleType, eleValue);
                break;
            case "webViewRefresh":
                webViewRefresh();
                break;
            case "webViewBack":
                webViewBack();
                break;
            case "getWebViewTextValue":
                globalParams.put(step.getString("content"), getWebViewText(eleName, eleType, eleValue));
                break;
            case "findElementInterval":
                setFindElementInterval(step.getInteger("content"), step.getInteger("text"));
                break;
            case "runScript":
                runScript(step.getString("content"), step.getString("text"), step.getString("stepName"));
                break;
            case "setDefaultFindPocoElementInterval":
                setDefaultFindPocoElementInterval(step.getInteger("content"), step.getInteger("text"));
                break;
            case "startPocoDriver":
                startPocoDriver(step.getString("content"), step.getInteger("text"));
                break;
            case "isExistPocoEle":
                isExistPocoEle(eleName, eleType, eleValue, step.getBoolean("content"));
                break;
            case "isExistPocoEleNum":
                isExistEleNum(eleName, eleType, eleValue, step.getString("content"), step.getInteger("text"), -1);
                break;
            case "pocoClick":
                pocoClick(eleName, eleType, eleValue);
                break;
            case "pocoLongPress":
                pocoLongPress(eleName, eleType, eleValue, step.getInteger("content"));
                break;
            case "pocoSwipe":
                pocoSwipe(eleName, eleType, eleValue, eleName1, eleType1, eleValue1);
                break;
            case "setTheRealPositionOfTheWindow":
                setTheRealPositionOfTheWindow(step.getString("content"));
                break;
            case "getPocoElementAttr":
                getPocoElementAttr(eleName, eleType, eleValue, step.getString("text"), step.getString("content"));
                break;
            case "obtainPocoElementAttr":
                obtainPocoElementAttr(eleName, eleType, eleValue, step.getString("text"), step.getString("content"));
                break;
            case "logPocoElementAttr":
                logPocoElementAttr(eleName, eleType, eleValue, step.getString("text"));
                break;
            case "getPocoTextValue":
                globalParams.put(step.getString("content"), getPocoText(eleName, eleType, eleValue));
                break;
            case "freezeSource":
                freezeSource();
                break;
            case "thawSource":
                thawSource();
                break;
            case "closePocoDriver":
                closePocoDriver();
                break;
            case "switchWindowMode":
                switchWindowMode(step.getBoolean("content"));
                break;
            case "switchIgnoreMode":
                switchIgnoreMode(step.getBoolean("content"));
                break;
            case "switchVisibleMode":
                switchVisibleMode(step.getBoolean("content"));
                break;
            case "closeKeyboard":
                closeKeyboard();
                break;
            case "iteratorPocoElement":
                iteratorPocoElement(eleName, eleType, eleValue);
                break;
            case "iteratorAndroidElement":
                iteratorAndroidElement(eleName, eleType, eleValue);
                break;
            case "getClipperByKeyboard":
                globalParams.put(step.getString("content"), getClipperByKeyboard());
                break;
            case "setClipperByKeyboard":
                setClipperByKeyboard(step.getString("content"));
                break;
            // <= 2.5版本的文本断言语法(包括原生，webView，Poco三类)，保留做兼容，老版本升级上来的存量用例继续可用
            case "getText":
                getTextAndAssert(eleName, eleType, eleValue, step.getString("content"));
                break;
            case "getWebViewText":
                getWebViewTextAndAssert(eleName, eleType, eleValue, step.getString("content"));
                break;
            case "getPocoText":
                getPocoTextAndAssert(eleName, eleType, eleValue, step.getString("content"));
                break;
            // > 2.5版本的文本断言语法，支持指定断言的方式
            case "assertText":
                getElementTextAndAssertWithOperation(eleName, eleType, eleValue, step.getString("content"), step.getString("text"), -1);
                break;
            case "assertWebViewText":
                getElementTextAndAssertWithOperation(eleName, eleType, eleValue, step.getString("content"), step.getString("text"), -1);
                break;
            case "assertPocoText":
                getElementTextAndAssertWithOperation(eleName, eleType, eleValue, step.getString("content"), step.getString("text"), -1);
                break;
        }
        switchType(step);
    }

    private void zoom(JSONArray elements) {
        resultInfo.setStepDes("双指缩放");

        ArrayList<Point> points = new ArrayList<>();
        try {
            // 要保证有4个坐标点
            if (elements != null && elements.size() == 4) {
                List<JSONObject> jsonObjects = elements.toJavaList(JSONObject.class);
                for (JSONObject jsonObject : jsonObjects) {
                    String eleValue = jsonObject.getString("eleValue");
                    int[] coords = Arrays.stream(eleValue.split(","))
                            .mapToInt(Integer::parseInt)
                            .toArray();
                    points.add(new Point(coords[0], coords[1]));
                }
                deviceService.zoomIn(points);

            } else {
                throw new Exception("没有足够的坐标点");
            }
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void notImplement() {
        ToastUtil.showToast("not implement");
        try {
            throw new Exception("not implement");
        } catch (Exception e) {
            e.printStackTrace();
            resultInfo.setE(e);
        }
    }

    private Object getPocoText(String eleName, String eleType, String eleValue) {
        notImplement();
        return null;
    }

    private void rotateDevice(String stepType) {
        try {
            Context context = contextWeakReference.get();
            resultInfo.setDetail("");
            switch (stepType) {
                case "screenSub":
                    resultInfo.setStepDes("左转屏幕");
                    deviceService.getRotation().setLeft(context);
                    break;
                case "screenAdd":
                    resultInfo.setStepDes("右转屏幕");
                    deviceService.getRotation().setRight(context);
                    break;
                case "screenAbort":
                    resultInfo.setStepDes("关闭自动旋转");
                    deviceService.getRotation().disable(context);
                    break;
            }
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void getElementTextAndAssertWithOperation(String eleName, String eleType, String eleValue, String content, String text, int i) {
        notImplement();
    }

    private void switchType(JSONObject stepJson) throws Throwable {
        Integer error = stepJson.getInteger("error");
        String stepDes = resultInfo.getStepDes();
        String detail = resultInfo.getDetail();
        Throwable e = resultInfo.getE();
        if (e != null && !"exit while".equals(e.getMessage()) && (e.getMessage() == null || !e.getMessage().startsWith("IGNORE:"))) {
            switch (ErrorType.fromValue(error)) {
                case IGNORE:
                    if (stepJson.getInteger("conditionType").equals(ConditionEnum.NONE.getValue())) {
                        resultInfo.packIgnore();
                    } else {
                        ConditionEnum conditionType =
                                SonicEnum.valueToEnum(ConditionEnum.class, stepJson.getInteger("conditionType"));
                        String des = String.format("「%s」步骤「%s」异常", conditionType.getName(), stepDes);
                        resultInfo.pack(StepType.ERROR, des);
                    }
                    break;
                case WARNING:
                    resultInfo.packWarning();
                    break;
                case SHUTDOWN:
                    resultInfo.packError();
                    throw e;
            }
        } else if (!"IGNORE".equals(stepDes)) {
            resultInfo.packPass();
        }
        LogUtil.i(TAG, String.format("switchType: pack info : %s", resultInfo.getPackInfo()));
        resultInfo.collect();
    }

    private void getPocoTextAndAssert(String eleName, String eleType, String eleValue, String content) {
        notImplement();
    }

    private void getWebViewTextAndAssert(String eleName, String eleType, String eleValue, String content) {
        notImplement();
    }

    public void getTextAndAssert(String des, String selector, String pathValue, String expect) {
        try {
            String s = getText(des, selector, pathValue);
            if (resultInfo.getE() != null) {
                return;
            }
            resultInfo.setStepDes("验证 [" + des + "] 文本");
            resultInfo.setDetail("验证 [" + selector + ":" + pathValue + "] 文本");
            try {
                expect = TextHandler.replaceTrans(expect, globalParams);
                Assert.assertEquals(s, expect);
                LogUtil.i(TAG, "验证文本: 真实值： " + s + " 期望值： " + expect);
            } catch (AssertionError e) {
                resultInfo.setE(e);
            }
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void setClipperByKeyboard(String content) {
        notImplement();
    }

    private Object getClipperByKeyboard() {
        notImplement();
        return null;
    }

    private void iteratorAndroidElement(String eleName, String eleType, String eleValue) {
        notImplement();
    }

    private void iteratorPocoElement(String eleName, String eleType, String eleValue) {
        notImplement();
    }

    private void closeKeyboard() {
        notImplement();
    }

    private void switchVisibleMode(Boolean content) {
        notImplement();
    }

    private void switchIgnoreMode(Boolean content) {
        notImplement();
    }

    private void switchWindowMode(Boolean content) {
        notImplement();
    }

    private void closePocoDriver() {
        notImplement();
    }

    private void thawSource() {
        notImplement();
    }

    private void freezeSource() {
        notImplement();
    }

    private void logPocoElementAttr(String eleName, String eleType, String eleValue, String text) {
        notImplement();
    }

    private void obtainPocoElementAttr(String eleName, String eleType, String eleValue, String text, String content) {
        notImplement();
    }

    private void getPocoElementAttr(String eleName, String eleType, String eleValue, String text, String content) {
        notImplement();
    }

    private void setTheRealPositionOfTheWindow(String content) {
        notImplement();
    }

    private void pocoSwipe(String eleName, String eleType, String eleValue, String eleName1, String eleType1, String eleValue1) {
        notImplement();
    }

    private void pocoLongPress(String eleName, String eleType, String eleValue, Integer content) {
        notImplement();
    }

    private void pocoClick(String eleName, String eleType, String eleValue) {
        notImplement();
    }

    private void isExistPocoEle(String eleName, String eleType, String eleValue, Boolean content) {
        notImplement();
    }

    private void startPocoDriver(String content, Integer text) {
        notImplement();
    }

    private void setDefaultFindPocoElementInterval(Integer content, Integer text) {
        notImplement();
    }

    private void runScript( String script, String type, String stepName) {
        resultInfo.setStepDes("Run Custom Scripts\n");
        switch (type) {
            case "Groovy":
//                String script = "a.print(\"测试\")";

//                GroovyHelper.bind("stepHandler", this);
//                GroovyHelper.bind("deviceService", deviceService);
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    GroovyHelper.executeCmd(script, cmd -> {
                        String s = deviceService.execCmd(cmd);
                        stringBuilder.append("run: ").append(cmd).append("\n");
                        stringBuilder.append("res: ").append(s).append("\n");
                    });
                    resultInfo.setDetail(stringBuilder.toString());
                } catch (Exception e) {
                    LogUtil.e(TAG, "runScript: ", e);
                    resultInfo.setE(e);
                }
                break;
            default:
                resultInfo.setE(new Exception("不识别脚本内容"));
        }
    }

    private void setFindElementInterval(Integer retry, Integer interval) {
        // appium 框架参数值，无对应参数可配置
        resultInfo.setStepDes("Set Global Find Android Element Interval");
        resultInfo.setDetail(String.format("Retry count: %d, retry interval: %d ms", retry, interval));
    }

    private Object getWebViewText(String eleName, String eleType, String eleValue) {
        notImplement();
        return null;
    }

    private String getText(String des, String selector, String pathValue) {
        CharSequence text = "";
        resultInfo.setStepDes("获取 [" + des + "] 文本");
        resultInfo.setDetail("获取 [" + selector + ":" + pathValue + "] 文本");
        try {
            return (String) findEle(selector, pathValue).getText();
        } catch (Exception e) {
            resultInfo.setE(e);
        }
        return null;
    }

    private void webViewBack() {
        notImplement();
    }

    private void webViewRefresh() {
        notImplement();
    }

    private void webViewClick(String eleName, String eleType, String eleValue) {
        notImplement();
    }

    private void webViewSendKeysByActions(String eleName, String eleType, String eleValue, String content) {
        notImplement();
    }

    private void webViewSendKeys(String eleName, String eleType, String eleValue, String content) {
        notImplement();
    }

    private void webViewClear(String eleName, String eleType, String eleValue) {
        notImplement();
    }

    private void isExistWebViewEle(String eleName, String eleType, String eleValue, Boolean content) {
        notImplement();
    }

    private void webElementScrollToView(String eleName, String eleType, String eleValue) {
        notImplement();
    }

    private void publicStep(String name, JSONArray publicSteps, Integer errorType) throws SonicRespException {
        LogUtil.i(TAG, "公共步骤「" + name + "」开始执行", "");
//        isLockStatus = true;
        List<JSONObject> stepArray = publicSteps.toJavaList(JSONObject.class);
        for (JSONObject stepDetail : stepArray) {
            try {
//                resultInfo.setStepDes("IGNORE");
                resultInfo.setPrefixStepDes("公共步骤 " + name);
                stepDetail.put("error", errorType);
                stepHandlerWrapper.runStep(stepDetail, resultInfo);
            } catch (Throwable e) {
                LogUtil.e(TAG, resultInfo.getStepDes());
                resultInfo.setE(e);
                resultInfo.packError();
                resultInfo.collect();
                break;
            }
        }
        resultInfo.setPrefixStepDes("");
        resultInfo.setStepDes("IGNORE");
        if (resultInfo.getE() != null && (resultInfo.getE().getMessage() != null) && (!resultInfo.getE().getMessage().startsWith("IGNORE:"))) {
            resultInfo.setStepDes("公共步骤 " + name);
            resultInfo.setE(new SonicRespException("Exception thrown during child step running."));
        }
        LogUtil.i(TAG, "公共步骤「" + name + "」执行完毕", "");
    }

    private void setDefaultFindWebViewElementInterval(Integer content, Integer text) {
        notImplement();
    }

    private void sendInputText(String keys) {
        deviceService.sendText(keys);
    }

    private void runMonkey(JSONObject content, List<JSONObject> text) {
        resultInfo.setStepDes("运行随机事件测试");
        resultInfo.setDetail("");
        resultInfo.setE(new Exception("App 端不支持 Monkey 测试"));
//        String packageName = content.getString("packageName");
//        int pctNum = content.getInteger("pctNum");
//        if (!deviceService.execCmd("pm list package").contains(packageName)) {
////            log.sendStepLog(StepType.ERROR, "应用未安装！", "设备未安装 " + packageName);
//            ToastUtil.showToast(contextWeakReference.get(), "设备未安装 " + packageName);
//            resultInfo.setE(new Exception("未安装应用"));
//            return;
//        }
//        JSONArray options = content.getJSONArray("options");
//        Point screenSize = deviceService.getScreenSize();
//
//        int width = screenSize.x;
//        int height = screenSize.y;
//        int sleepTime = 50;
//        int systemEvent = 0;
//        int tapEvent = 0;
//        int longPressEvent = 0;
//        int swipeEvent = 0;
//        int navEvent = 0;
//        boolean isOpenH5Listener = false;
//        boolean isOpenPackageListener = false;
//        boolean isOpenActivityListener = false;
//        boolean isOpenNetworkListener = false;
//        if (!options.isEmpty()) {
//            for (Object j : options) {
//                JSONObject jsonOption = JSON.parseObject(j.toString());
//                if (jsonOption.getString("name").equals("sleepTime")) {
//                    sleepTime = jsonOption.getInteger("value");
//                }
//                if (jsonOption.getString("name").equals("systemEvent")) {
//                    systemEvent = jsonOption.getInteger("value");
//                }
//                if (jsonOption.getString("name").equals("tapEvent")) {
//                    tapEvent = jsonOption.getInteger("value");
//                }
//                if (jsonOption.getString("name").equals("longPressEvent")) {
//                    longPressEvent = jsonOption.getInteger("value");
//                }
//                if (jsonOption.getString("name").equals("swipeEvent")) {
//                    swipeEvent = jsonOption.getInteger("value");
//                }
//                if (jsonOption.getString("name").equals("navEvent")) {
//                    navEvent = jsonOption.getInteger("value");
//                }
//                if (jsonOption.getString("name").equals("isOpenH5Listener")) {
//                    isOpenH5Listener = jsonOption.getBoolean("value");
//                }
//                if (jsonOption.getString("name").equals("isOpenPackageListener")) {
//                    isOpenPackageListener = jsonOption.getBoolean("value");
//                }
//                if (jsonOption.getString("name").equals("isOpenActivityListener")) {
//                    isOpenActivityListener = jsonOption.getBoolean("value");
//                }
//                if (jsonOption.getString("name").equals("isOpenNetworkListener")) {
//                    isOpenNetworkListener = jsonOption.getBoolean("value");
//                }
//            }
//        }
//        int finalSleepTime = sleepTime;
//        int finalTapEvent = tapEvent;
//        int finalLongPressEvent = longPressEvent;
//        int finalSwipeEvent = swipeEvent;
//        int finalSystemEvent = systemEvent;
//        int finalNavEvent = navEvent;
//        Future<?> randomThread = AndroidDeviceThreadPool.cachedThreadPool.submit(() -> {
//                    log.sendStepLog(StepType.INFO, "", "随机事件数：" + pctNum +
//                            "<br>目标应用：" + packageName
//                            + "<br>用户操作时延：" + finalSleepTime + " ms"
//                            + "<br>轻触事件权重：" + finalTapEvent
//                            + "<br>长按事件权重：" + finalLongPressEvent
//                            + "<br>滑动事件权重：" + finalSwipeEvent
//                            + "<br>物理按键事件权重：" + finalSystemEvent
//                            + "<br>系统事件权重：" + finalNavEvent
//                    );
//                    openApp(resultInfo, packageName);
//                    int totalCount = finalSystemEvent + finalTapEvent + finalLongPressEvent + finalSwipeEvent + finalNavEvent;
//                    for (int i = 0; i < pctNum; i++) {
//                        try {
//                            int random = new Random().nextInt(totalCount);
//                            if (random < finalSystemEvent) {
//                                int key = new Random().nextInt(4);
//                                String keyType = switch (key) {
//                                    case 0 -> "HOME";
//                                    case 1 -> "BACK";
//                                    case 2 -> "MENU";
//                                    case 3 -> "APP_SWITCH";
//                                    default -> "";
//                                };
//                                deviceService.pressKeyCode(keyCode);
//                            }
//                            if (random >= finalSystemEvent && random < (finalSystemEvent + finalTapEvent)) {
//                                int x = new Random().nextInt(width - 60) + 60;
//                                int y = new Random().nextInt(height - 60) + 60;
//                                deviceService.click(x, y);
//                            }
//                            if (random >= (finalSystemEvent + finalTapEvent) && random < (finalSystemEvent + finalTapEvent + finalLongPressEvent)) {
//                                int x = new Random().nextInt(width - 60) + 60;
//                                int y = new Random().nextInt(height - 60) + 60;
//                                AndroidTouchHandler.longPress(iDevice, x, y, (new Random().nextInt(3) + 1) * 1000);
//                            }
//                            if (random >= (finalSystemEvent + finalTapEvent + finalLongPressEvent) && random < (finalSystemEvent + finalTapEvent + finalLongPressEvent + finalSwipeEvent)) {
//                                int x1 = new Random().nextInt(width - 60) + 60;
//                                int y1 = new Random().nextInt(height - 80) + 80;
//                                int x2 = new Random().nextInt(width - 60) + 60;
//                                int y2 = new Random().nextInt(height - 80) + 80;
//                                AndroidTouchHandler.swipe(iDevice, x1, y1, x2, y2);
//                            }
//                            if (random >= (finalSystemEvent + finalTapEvent + finalLongPressEvent + finalSwipeEvent) && random < (finalSystemEvent + finalTapEvent + finalLongPressEvent + finalSwipeEvent + finalNavEvent)) {
//                                int a = new Random().nextInt(2);
//                                if (a == 1) {
//                                    AndroidDeviceBridgeTool.executeCommand(iDevice, "svc wifi enable");
//                                } else {
//                                    AndroidDeviceBridgeTool.executeCommand(iDevice, "svc wifi disable");
//                                }
//                            }
//                            Thread.sleep(finalSleepTime);
//                        } catch (Throwable e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//        );
//        boolean finalIsOpenH5Listener = isOpenH5Listener;
//        Future<?> H5Listener = AndroidDeviceThreadPool.cachedThreadPool.submit(() -> {
//                    if (finalIsOpenH5Listener) {
//                        int h5Time = 0;
//                        while (!randomThread.isDone()) {
//                            try {
//                                Thread.sleep(8000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            try {
//                                if (androidDriver.findElementList(AndroidSelector.CLASS_NAME, "android.webkit.WebView").size() > 0) {
//                                    h5Time++;
//                                    AndroidDeviceBridgeTool.executeCommand(iDevice, "input keyevent 4");
//                                } else {
//                                    h5Time = 0;
//                                }
//                                if (h5Time >= 12) {
//                                    AndroidDeviceBridgeTool.forceStop(iDevice, packageName);
//                                    h5Time = 0;
//                                }
//                            } catch (Throwable ignored) {
//                            }
//                        }
//                    }
//                }
//        );
//        boolean finalIsOpenPackageListener = isOpenPackageListener;
//        Future<?> packageListener = AndroidDeviceThreadPool.cachedThreadPool.submit(() -> {
//                    if (finalIsOpenPackageListener) {
//                        while (!randomThread.isDone()) {
//                            int waitTime = 0;
//                            while (waitTime <= 10 && (!randomThread.isDone())) {
//                                try {
//                                    Thread.sleep(5000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                                if (!AndroidDeviceBridgeTool.getCurrentActivity(iDevice).contains(packageName)) {
//                                    AndroidDeviceBridgeTool.activateApp(iDevice, packageName);
//                                }
//                                waitTime++;
//                            }
//                            AndroidDeviceBridgeTool.activateApp(iDevice, packageName);
//                        }
//                    }
//                }
//        );
//        boolean finalIsOpenActivityListener = isOpenActivityListener;
//        Future<?> activityListener = AndroidDeviceThreadPool.cachedThreadPool.submit(() -> {
//                    if (finalIsOpenActivityListener) {
//                        if (text.isEmpty()) {
//                            return;
//                        }
//                        Set<String> blackList = new HashSet<>();
//                        for (JSONObject activities : text) {
//                            blackList.add(activities.getString("name"));
//                        }
//                        while (!randomThread.isDone()) {
//                            try {
//                                Thread.sleep(8000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            if (blackList.contains(AndroidDeviceBridgeTool.getCurrentActivity(iDevice))) {
//                                AndroidDeviceBridgeTool.executeCommand(iDevice, "input keyevent 4");
//                            } else continue;
//                            try {
//                                Thread.sleep(8000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            if (blackList.contains(AndroidDeviceBridgeTool.getCurrentActivity(iDevice))) {
//                                AndroidDeviceBridgeTool.forceStop(iDevice, packageName);
//                            }
//                        }
//                    }
//                }
//        );
//        boolean finalIsOpenNetworkListener = isOpenNetworkListener;
//        Future<?> networkListener = AndroidDeviceThreadPool.cachedThreadPool.submit(() -> {
//                    if (finalIsOpenNetworkListener) {
//                        while (!randomThread.isDone()) {
//                            try {
//                                Thread.sleep(8000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            AndroidDeviceBridgeTool.executeCommand(iDevice, "settings put global airplane_mode_on 0");
//                            try {
//                                Thread.sleep(8000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            AndroidDeviceBridgeTool.executeCommand(iDevice, "svc wifi enable");
//                        }
//                    }
//                }
//        );
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        log.sendStepLog(StepType.INFO, "", "测试目标包：" + packageName +
//                (isOpenPackageListener ? "<br>应用包名监听器已开启..." : "") +
//                (isOpenH5Listener ? "<br>H5页面监听器已开启..." : "") +
//                (isOpenActivityListener ? "<br>黑名单Activity监听器..." : "") +
//                (isOpenNetworkListener ? "<br>网络状态监听器已开启..." : ""));
//        while (!randomThread.isDone() || (!packageListener.isDone()) || (!activityListener.isDone()) || (!networkListener.isDone()) || (!H5Listener.isDone())) {
//        }
    }

    private void asserts( String actual, String expect, String type) {
        resultInfo.setDetail("真实值： " + actual + " 期望值： " + expect);
        resultInfo.setStepDes("");
        try {
            switch (type) {
                case "assertEquals":
                    resultInfo.setStepDes("断言验证(相等)");
                    Assert.assertEquals(actual, expect);
                    break;
                case "assertTrue":
                    resultInfo.setStepDes("断言验证(包含)");
                    Assert.assertTrue(actual.contains(expect));
                    break;
                case "assertNotTrue":
                    resultInfo.setStepDes("断言验证(不包含)");
                    Assert.assertFalse(actual.contains(expect));
                    break;
            }
        } catch (AssertionError e) {
            resultInfo.setE(e);
        }
    }

    private void keyCode(String keyCode) {
        keyCode(AndroidKey.valueOf(keyCode).getCode());
    }

    private void keyCode(Integer keyCode) {
        resultInfo.setStepDes("按系统按键 [" + keyCode + "] 键");
        resultInfo.setDetail("");
        LogUtil.i(TAG, String.format("keyCode: press keyCode: [%s]", keyCode));
        try {
            deviceService.pressKeyCode(keyCode);
        } catch (Exception e) {
            resultInfo.setE(e);
            e.printStackTrace();
        }
    }

    private void locationMode(Boolean enable) {
        resultInfo.setStepDes("切换位置服务");
        resultInfo.setDetail("");
        try {
            deviceService.setLocationState(enable);
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void wifiMode(Boolean enable) {
        resultInfo.setStepDes("开关WIFI");
        resultInfo.setDetail(enable ? "打开" : "关闭");
        try {
            deviceService.setWifiState(enable);
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void airPlaneMode(Boolean enable) {
        resultInfo.setStepDes("切换飞行模式");
        resultInfo.setDetail(enable ? "打开" : "关闭");
        try {
            deviceService.setAirPlaneMode(enable);
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void unLock() {
        resultInfo.setStepDes("点击Power键");
        resultInfo.setDetail("");
        try {
            deviceService.pressKeyCode(KeyEvent.KEYCODE_POWER);
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void lock() {
        resultInfo.setStepDes("锁定屏幕");
        resultInfo.setDetail("");
        try {
            deviceService.pressKeyCode(KeyEvent.KEYCODE_POWER);
        } catch (Exception e) {
            resultInfo.setE(e);
            e.printStackTrace();
        }
    }

    private void uninstall(String pkg) {
        resultInfo.setStepDes("卸载应用");
        pkg = TextHandler.replaceTrans(pkg, globalParams);
        resultInfo.setDetail("App包名： " + pkg);
        try {
            deviceService.uninstallPkg(pkg);
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void install(String path) {
        resultInfo.setStepDes("安装应用");
        path = TextHandler.replaceTrans(path, globalParams);
        resultInfo.setDetail("App安装路径： " + path);
        File localFile = new File(path);
        try {
            if (path.contains("http")) {
                localFile = DownloadTool.download(path);
            }
            Context context = contextWeakReference.get();
            ToastUtil.showToast(context, "开始安装App，请稍后...");
            deviceService.installApk(localFile.getAbsolutePath());
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void terminate(String packageName) {
        resultInfo.setStepDes("终止应用");
        packageName = TextHandler.replaceTrans(packageName, globalParams);
        resultInfo.setDetail("应用包名： " + packageName);
        try {
            deviceService.forceStop(packageName);
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void openApp(String appPackage) {
        resultInfo.setStepDes("打开应用");
        appPackage = TextHandler.replaceTrans(appPackage, globalParams);
        resultInfo.setDetail("App包名： " + appPackage);
        try {
            boolean b = deviceService.startApp(appPackage);
            if (!b) {
                LogUtil.w(TAG, "openApp: 未找到应用 " + appPackage);
                Exception e = new Exception("未找到应用 " + appPackage);
                resultInfo.setE(e);
            }
//            targetPackage = appPackage;
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private String stepScreen() {
        resultInfo.setStepDes("获取截图");
        resultInfo.setDetail("");
        String url = "";
        String filePath = deviceService.takeShot("test-output");
        try {
            url = MinioUtil.builder().build().upload(filePath, new File(filePath).getName());
            resultInfo.setDetail(url);
        } catch (Exception e) {
            resultInfo.setE(e);
        }
        return url;
    }

    private void checkImage(String des, String pathValue, double matchThreshold) {
        resultInfo.setStepDes("检测与当前设备截图相似度，期望相似度为" + matchThreshold + "%");
        try {
            File file = null;
            if (pathValue.startsWith("http")) {
                file = DownloadTool.download(pathValue);
            }
            String tempPic = deviceService.takeShot("temp");
            double score = ImageMatcher.getSimilarMSSIMScore(file.getAbsolutePath(), tempPic, true);
            resultInfo.setStepDes("检测" + des + "图片相似度");
            resultInfo.setDetail("相似度为" + score * 100 + "%");
            if (score == 0) {
                resultInfo.setE(new Exception("图片相似度检测不通过！比对图片分辨率不一致！"));
            } else if (score < (matchThreshold / 100)) {
                resultInfo.setE(new Exception("图片相似度检测不通过！expect " + matchThreshold + " but " + score * 100));
            }
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void swipeByDefinedDirection(String text, Integer content) {
        notImplement();
    }

    private void pause(Integer time) {
        resultInfo.setStepDes("强制等待");
        resultInfo.setDetail("等待" + time + " ms");
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            resultInfo.setE(e);
        }
    }

    private void longPressPoint(String des, String xy, int time) {
        double x = Double.parseDouble(xy.substring(0, xy.indexOf(",")));
        double y = Double.parseDouble(xy.substring(xy.indexOf(",") + 1));
        Point point = deviceService.computedPoint(x, y);
        resultInfo.setStepDes("长按 " + des);
        resultInfo.setDetail("长按坐标 " + time + " 毫秒 (" + point.x + "," + point.y + ")");
        try {
            deviceService.longPress(point, time);
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void tap(String eleName, String eleValue) {
        resultInfo.setStepDes("点击 " + (eleName == null ? "临时坐标" : eleName));
        resultInfo.setDetail(String.format("点击坐标(%s)", eleValue));
        eleValue = TextHandler.replaceTrans(eleValue, globalParams);
        LogUtil.i(TAG, String.format("tap: eleName: [%s], pos: [%s]", eleName, eleValue));
        String[] split = eleValue.split(",");
        try {
            deviceService.click(split[0], split[1]);
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void motionEventByPoint(String eleName, String eleValue, String text) {
        notImplement();
    }

    private void motionEventByEle(String eleName, String eleType, String eleValue, String text) {
        notImplement();
    }

    private void dragByEle(String eleName, String eleType, String eleValue, String eleName1, String eleType1, String eleValue1) {
        notImplement();
    }

    private void dragByPoint(String eleName, String eleValue, String eleName1, String eleValue1) {
        notImplement();
    }

    private void swipe(String eleName, String eleType, String eleValue, String eleName1, String eleType1, String eleValue1) {
        notImplement();
    }

    private void swipePoint(String des1, String xy1, String des2, String xy2) {
        // Athena 让坐标系也支持变量替换
        xy1 = TextHandler.replaceTrans(xy1, globalParams);
        xy2 = TextHandler.replaceTrans(xy2, globalParams);
        double x1 = Double.parseDouble(xy1.substring(0, xy1.indexOf(",")));
        double y1 = Double.parseDouble(xy1.substring(xy1.indexOf(",") + 1));
        Point point1 = deviceService.computedPoint(x1, y1);
        double x2 = Double.parseDouble(xy2.substring(0, xy2.indexOf(",")));
        double y2 = Double.parseDouble(xy2.substring(xy2.indexOf(",") + 1));
        Point point2 = deviceService.computedPoint(x2, y2);
        resultInfo.setStepDes("拖动坐标(" + point1 + ")到(" + point2 + ")");
        try {
            deviceService.swipe(point1, point2);
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void longPress(String des, String selector, String pathValue, int time) {
        resultInfo.setStepDes("长按 " + des);
        resultInfo.setDetail("长按控件元素 " + time + " 毫秒 ");
        try {
            AccessibilityNodeInfoImpl ele = (AccessibilityNodeInfoImpl) findEle(selector, pathValue);
            Point point = ele.getVisibleCenter();
            deviceService.longPress(point, time);
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void isExistEleNum(String eleName, String eleType, String eleValue, String content, Integer text, int i) {
        notImplement();
    }

    private void clear(String des, String selector, String pathValue) {
        resultInfo.setStepDes("清空 " + des);
        resultInfo.setDetail("清空 " + selector + ": " + pathValue);
        try {
            new AccessibilityNodeInfoImpl(findEle(selector, pathValue)).inputText("");
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void scrollToEle(String eleName, String eleType, String eleValue, Integer content, String text) {
        notImplement();
    }

    private void isExistEle(String eleName, String eleType, String eleValue, Boolean content) {
        resultInfo.setStepDes("判断控件 " + eleName + " 是否存在");
        resultInfo.setDetail("期望值：" + (content ? "存在" : "不存在"));
        boolean existEle = false;
        try {
            existEle = hasEle(eleType, eleValue);
        } catch (Exception ignored) {
            resultInfo.setE(new Exception(String.format("控件[%s]获取失败", eleName)));
            return ;
        }
        try {
            Assert.assertEquals(existEle, content);
        } catch (AssertionError e) {
            resultInfo.setE(e);
        }
    }

    public AccessibilityNodeInfo findEle(String eleType, String eleValue) throws Exception {
        eleValue = TextHandler.replaceTrans(eleValue, globalParams);
        switch (eleType) {
            case "androidIterator" : notImplement(); break;
            case "id" : return deviceService.findNode(By.res(eleValue));
            case "accessibilityId" : return deviceService.findNode(By.desc(eleValue));
            case "xpath" : return deviceService.findNode(By.xpath(eleValue));
            case "className" : return deviceService.findNode(By.className(eleValue));
            case "androidUIAutomator" : notImplement(); break;
            default :
                String message = "findEle: " + "查找控件元素失败" + "这个控件元素类型: " + eleType + " 不存在!!!";
                LogUtil.e(TAG, message);
                throw new Exception(message);
        }
        return null;
    }

    private boolean hasEle(String eleType, String eleValue) throws Exception {
        eleValue = TextHandler.replaceTrans(eleValue, globalParams);
        switch (eleType) {
            case "androidIterator" : notImplement(); break;
            case "id" : return deviceService.getXml().contains(String.format("id=\"%s\"", eleValue));
            case "accessibilityId" : return deviceService.getXml().contains(String.format("desc=\"%s\"", eleValue));
            case "xpath" : return deviceService.findNode(By.xpath(eleValue)) != null;
            case "className" : return deviceService.getXml().contains(String.format("class=\"%s\"", eleValue));
            case "androidUIAutomator" : notImplement();break;
            default :
                notImplement();
                LogUtil.e(TAG, "findEle: " + "查找控件元素失败" + "这个控件元素类型: " + eleType + " 不存在!!!");
        }
        return false;
    }

    private void sendKeysByActions(String eleName, String eleType, String eleValue, String content) {
        notImplement();
    }

    private void sendKeys(String des, String selector, String pathValue, String keys) {
        keys = TextHandler.replaceTrans(keys, globalParams);
        resultInfo.setStepDes("对 " + des + " 输入内容");
        resultInfo.setDetail("对 " + selector + ": " + pathValue + " 输入: " + keys);
        try {
            new AccessibilityNodeInfoImpl(findEle(selector, pathValue)).inputText(keys);
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void logElementAttr(String eleName, String eleType, String eleValue, String text) {
        notImplement();
    }

    private void obtainElementAttr(String eleName, String eleType, String eleValue, String text, String content) {
        notImplement();
    }

    private void getElementAttr(String des, String selector, String pathValue, String attr, String expect) {
        resultInfo.setStepDes("验证控件 " + des + " 属性");
        resultInfo.setDetail("属性：" + attr + "，期望值：" + expect);
        try {
            AccessibilityNodeInfo ele = findEle(selector, pathValue);
            JSONObject jsonObject = ((AccessibilityNodeInfoImpl) ele).toJson(false);
            String attrValue = jsonObject.getString(attr);
            LogUtil.i(TAG,  attr + " 属性获取结果: " + attrValue);
            try {
                Assert.assertEquals(attrValue, expect);
            } catch (AssertionError e) {
                resultInfo.setE(e);
            }
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void getActivity(String expect) {
        expect = TextHandler.replaceTrans(expect, globalParams);
        String currentActivity = deviceService.getCurrentActivity();
        resultInfo.setStepDes("验证当前Activity");
        resultInfo.setDetail("activity：" + currentActivity + "，期望值：" + expect);
        try {
            Assert.assertEquals(expect, currentActivity);
        } catch (AssertionError e) {
            resultInfo.setE(e);
        }
    }

    private void getUrl(String content) {
        notImplement();
    }

    private void getTitle(String content) {
//        String title = chromeDriver.getTitle();
//        resultInfo.setStepDes("验证网页标题");
//        resultInfo.setDetail("标题：" + title + "，期望值：" + expect);
        notImplement();
    }

    private void click(String eleName, String eleType, String eleValue) throws Exception {
        resultInfo.setStepDes("点击 " + eleName);
        resultInfo.setDetail("点击 " + eleType + ": " + eleValue);

        LogUtil.i(TAG, String.format("click: eleName: [%s], eleType: [%s], eleValue: [%s]", eleName, eleType, eleValue));
        eleValue = TextHandler.replaceTrans(eleValue, globalParams);
        switch (eleType) {
            case "androidIterator":
                notImplement();
                break;
            case "id":
                clickId(eleValue);
                break;
            case "accessibilityId":
                clickDesc(eleValue);
                break;
            case "xpath":
                clickXpath(eleValue);
                break;
            case "className":
                clickClassName(eleValue);
                break;
            case "androidUIAutomator":
                notImplement();
                break;
            default:
                String msg = "click: 查找控件元素失败, 这个控件元素类型: " + eleType + " 不存在!!!";
                LogUtil.e(TAG, msg);
                resultInfo.setE(new Exception(msg));
        }
    }

    private void clickXpath(String eleValue) throws Exception {
        try {
            deviceService.findNode(By.xpath(eleValue)).click();
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void clickClassName(String eleValue) throws Exception {
        try {
            deviceService.findNode(By.className(eleValue)).click();
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void clickDesc(String eleValue) throws Exception {
        try {
            deviceService.findNode(By.desc(eleValue)).click();
        } catch (Exception e) {
            LogUtil.e(TAG, "clickDesc: ", e);
            resultInfo.setE(e);
        }
    }

    private void clickId(String eleValue) throws Exception {
        try {
            deviceService.findNode(By.res(eleValue)).click();
        } catch (Exception e) {
            resultInfo.setE(e);
        }
    }

    private void clickByImg(String des, String pathValue) {
        resultInfo.setStepDes("点击图片" + des);
        resultInfo.setDetail(pathValue);
        File file = null;
        if (pathValue.startsWith("http")) {
            try {
                file = DownloadTool.download(pathValue);
            } catch (Exception e) {
                resultInfo.setE(e);
                return;
            }
        } else {
            resultInfo.setE(new Exception(String.format("无法下载图片：%s", pathValue)));
        }
        String tempPic = deviceService.takeShot("temp");
        org.opencv.core.Point imageLocation = ImageMatcher.findImageLocation(file.getAbsolutePath(), tempPic);
        deviceService.click(imageLocation.x, imageLocation.y);
    }

    private void readText(String content, String text) {
        resultInfo.setStepDes("图像文字识别");
        resultInfo.setDetail("（该功能暂时关闭）期望包含文本：" + text);
    }

    private void toHandle(String params) {
        params = TextHandler.replaceTrans(params, globalParams);
        resultInfo.setStepDes("切换Handle - Android Client 内不做处理");
        resultInfo.setDetail("");
    }

    private void toWebView(String content, String text) {
        notImplement();
    }

    private void stepHold(Integer time) {
        resultInfo.setStepDes("设置全局步骤间隔");
        resultInfo.setDetail("间隔 " + time + " ms");
        LogUtil.i(TAG, String.format("stepHold: set as [%s]", time));
        GConfigParams.holdTime = time;
    }

    private void appAutoGrantPermissions(String pkg) {
        resultInfo.setStepDes("自动授权应用权限");
        String targetPackageName = TextHandler.replaceTrans(pkg, globalParams);
        resultInfo.setDetail("授权 " + targetPackageName);
        try {
            PermissionHelper.grantAllPermissions(contextWeakReference.get(), pkg);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.e(TAG, "appAutoGrantPermissions: " + pkg, e);
            resultInfo.setE(e);
        }
    }

    private void appReset(String pkg) {
        resultInfo.setStepDes("清空App内存缓存");
        pkg = TextHandler.replaceTrans(pkg, globalParams);
        resultInfo.setDetail("清空 " + pkg);
        try {
            deviceService.clearPkg(pkg);
        } catch (Exception e) {
            resultInfo.setE(e);
            LogUtil.e(TAG, String.format("清空%s内存缓存失败", pkg), e);
        }
    }

    private void switchTouchMode(String mode) {
        resultInfo.setStepDes("设置触控模式");
        resultInfo.setDetail("切换为 " + mode + " 模式");
        // 暂不处理
//        notImplement();
    }
}
