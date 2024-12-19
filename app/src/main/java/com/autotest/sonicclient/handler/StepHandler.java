package com.autotest.sonicclient.handler;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.enums.ConditionEnum;
import com.autotest.sonicclient.enums.ErrorType;
import com.autotest.sonicclient.enums.SonicEnum;
import com.autotest.sonicclient.enums.StepType;
import com.autotest.sonicclient.model.ResultInfo;
import com.autotest.sonicclient.services.TService;
import com.autotest.sonicclient.utils.GConfigParams;
import com.autotest.sonicclient.utils.InstrumentImpl;
import com.autotest.sonicclient.utils.JsonParser;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.ShellUtil;
import com.autotest.sonicclient.utils.ToastUtil;

import java.util.List;

public class StepHandler {
    private static final String TAG = "StepHandler";
    Context handleContext;
    private JSONObject globalParams = new JSONObject();

    private ResultInfo resultInfo = new ResultInfo();


    public StepHandler(Context handleContext) {
        this.handleContext = handleContext;
    }

    public ResultInfo getResultInfo() {
        return resultInfo;
    }

    public ResultInfo runStep(JSONObject stepJSON) throws Throwable {
        resultInfo.clearStep();
        String stepType = stepJSON.getJSONObject("step").getString("stepType");
        try {
            handleStep(stepJSON);
        } catch (Exception e) {
            LogUtil.e(TAG, String.format("runStep: [%s] 执行失败", stepType), e);
            resultInfo.setE(e);
            resultInfo.setStepDes(stepType + "handle error!!");
        }
        return resultInfo;
    }

    private void handleResult() {

    }

    private void handleStep(JSONObject stepJSON) throws Throwable {
        LogUtil.d(TAG, "handleStep: step: " + stepJSON);
        JSONObject step = stepJSON.getJSONObject("step");
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

        switch (step.getString("stepType")) {
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
                clickByImg(eleName
                        , eleValue);
                break;
            case "click":
                click(eleName, eleType
                        , eleValue);
                break;
            case "getTitle":
                getTitle(step.getString("content"));
                break;
            case "getUrl":
                getUrl(step.getString("content"));
                break;
            case "getActivity":
                getActivity(step.getString("content"));
                break;
            case "getElementAttr":
                getElementAttr(eleName, eleType
                        , eleValue, step.getString("text"), step.getString("content"));
                break;
            case "obtainElementAttr":
                obtainElementAttr(eleName,
                        eleType, eleValue,
                        step.getString("text"), step.getString("content"));
                break;
            case "logElementAttr":
                logElementAttr(eleName, eleType
                        , eleValue, step.getString("text"));
                break;
            case "sendKeys":
                sendKeys(eleName, eleType
                        , eleValue, step.getString("content"));
                break;
            case "sendKeysByActions":
                sendKeysByActions(eleName, eleType
                        , eleValue, step.getString("content"));
                break;
            case "isExistEle":
                isExistEle(eleName, eleType
                        , eleValue, step.getBoolean("content"));
                break;
            case "scrollToEle":
                scrollToEle(eleName,
                        eleType,
                        eleValue,
                        step.getInteger("content"),
                        step.getString("text"));
                break;
            case "isExistEleNum":
                isExistEleNum(eleName,
                        eleType,
                        eleValue,
                        step.getString("content"),
                        step.getInteger("text"), -1);
                break;
            case "clear":
                clear(eleName, eleType
                        , eleValue);
                break;
            case "longPress":
                longPress(eleName, eleType
                        , eleValue, step.getInteger("content"));
                break;
            case "swipe":
                swipePoint(eleName, eleValue
                        , eleName1, eleValue1);
                break;
            case "swipe2":
                swipe(eleName, eleType, eleValue
                        , eleName1, eleType1, eleValue1);
                break;
            case "drag":
                dragByPoint(eleName, eleValue
                        , eleName1, eleValue1);
                break;
            case "drag2":
                dragByEle(eleName, eleType, eleValue
                        , eleName1, eleType1, eleValue1);
                break;
            case "motionEvent":
                motionEventByEle(eleName,
                        eleType, eleValue, step.getString("text"));
                break;
            case "motionEventByPoint":
                motionEventByPoint(eleName,
                        eleValue,
                        step.getString("text"));
                break;
            case "tap":
                tap(eleName, eleValue);
                break;
            case "longPressPoint":
                longPressPoint(eleName, eleValue
                        , step.getInteger("content"));
                break;
            case "pause":
                pause(step.getInteger("content"));
                break;
            case "swipeByDefinedDirection":
                swipeByDefinedDirection(step.getString("text"), step.getInteger("content"));
                break;
            case "checkImage":
                checkImage(eleName, eleValue
                        , step.getDouble("content"));
                break;
            case "stepScreen":
                stepScreen(handleContext);
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
                rotateDevice(step.getString("stepType"));
                break;
            case "lock":
                lock(handleContext);
                break;
            case "unLock":
                unLock(handleContext);
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
                asserts(actual, expect, step.getString("stepType"));
                break;
            case "getTextValue":
                globalParams.put(step.getString("content"), getText(eleName
                        , eleType, eleValue));
                break;
            case "sendKeyForce":
                sendKeyForce(step.getString("content"));
                break;
            case "monkey":
                runMonkey(step.getJSONObject("content"), step.getJSONArray("text").toJavaList(JSONObject.class));
                break;
            case "publicStep":
                publicStep(step.getString("content"), step.getJSONArray("pubSteps"));
                break;
            case "setDefaultFindWebViewElementInterval":
                setDefaultFindWebViewElementInterval(step.getInteger("content"), step.getInteger("text"));
                break;
            case "webElementScrollToView":
                webElementScrollToView(eleName,
                        eleType,
                        eleValue);
                break;
            case "isExistWebViewEle":
                isExistWebViewEle(eleName, eleType
                        , eleValue, step.getBoolean("content"));
                break;
            case "isExistWebViewEleNum":
                isExistEleNum(
                        eleName, eleType
                        , eleValue, step.getString("content"),
                        step.getInteger("text"), -1);
                break;
            case "webViewClear":
                webViewClear(eleName, eleType
                        , eleValue);
                break;
            case "webViewSendKeys":
                webViewSendKeys(eleName, eleType
                        , eleValue, step.getString("content"));
                break;
            case "webViewSendKeysByActions":
                webViewSendKeysByActions(eleName, eleType
                        , eleValue, step.getString("content"));
                break;
            case "webViewClick":
                webViewClick(eleName, eleType, eleValue);
                break;
            case "webViewRefresh":
                webViewRefresh(handleContext);
                break;
            case "webViewBack":
                webViewBack(handleContext);
                break;
            case "getWebViewTextValue":
                globalParams.put(step.getString("content"), getWebViewText(eleName
                        , eleType, eleValue));
                break;
            case "findElementInterval":
                setFindElementInterval(step.getInteger("content"), step.getInteger("text"));
                break;
            case "runScript":
                runScript(step.getString("content"), step.getString("text"));
                break;
            case "setDefaultFindPocoElementInterval":
                setDefaultFindPocoElementInterval(step.getInteger("content"), step.getInteger("text"));
                break;
            case "startPocoDriver":
                startPocoDriver(step.getString("content"), step.getInteger("text"));
                break;
            case "isExistPocoEle":
                isExistPocoEle(eleName, eleType
                        , eleValue, step.getBoolean("content"));
                break;
            case "isExistPocoEleNum":
                isExistEleNum(
                        eleName, eleType
                        , eleValue, step.getString("content"),
                        step.getInteger("text"), -1);
                break;
            case "pocoClick":
                pocoClick(eleName, eleType
                        , eleValue);
                break;
            case "pocoLongPress":
                pocoLongPress(eleName, eleType
                        , eleValue
                        , step.getInteger("content"));
                break;
            case "pocoSwipe":
                pocoSwipe(eleName, eleType, eleValue
                        , eleName1, eleType1, eleValue1);
                break;
            case "setTheRealPositionOfTheWindow":
                setTheRealPositionOfTheWindow(step.getString("content"));
                break;
            case "getPocoElementAttr":
                getPocoElementAttr(eleName, eleType
                        , eleValue, step.getString("text"), step.getString("content"));
                break;
            case "obtainPocoElementAttr":
                obtainPocoElementAttr(eleName,
                        eleType, eleValue,
                        step.getString("text"), step.getString("content"));
                break;
            case "logPocoElementAttr":
                logPocoElementAttr(eleName, eleType
                        , eleValue, step.getString("text"));
                break;
            case "getPocoTextValue":
                globalParams.put(step.getString("content"), getPocoText(eleName
                        , eleType, eleValue));
                break;
            case "freezeSource":
                freezeSource(handleContext);
                break;
            case "thawSource":
                thawSource(handleContext);
                break;
            case "closePocoDriver":
                closePocoDriver(handleContext);
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
                closeKeyboard(handleContext);
                break;
            case "iteratorPocoElement":
                iteratorPocoElement(eleName, eleType
                        , eleValue);
                break;
            case "iteratorAndroidElement":
                iteratorAndroidElement(eleName, eleType
                        , eleValue);
                break;
            case "getClipperByKeyboard":
                globalParams.put(step.getString("content"), getClipperByKeyboard(handleContext));
                break;
            case "setClipperByKeyboard":
                setClipperByKeyboard(step.getString("content"));
                break;
            // <= 2.5版本的文本断言语法(包括原生，webView，Poco三类)，保留做兼容，老版本升级上来的存量用例继续可用
            case "getText":
                getTextAndAssert(eleName, eleType
                        , eleValue, step.getString("content"));
                break;
            case "getWebViewText":
                getWebViewTextAndAssert(eleName, eleType
                        , eleValue, step.getString("content"));
                break;
            case "getPocoText":
                getPocoTextAndAssert(eleName, eleType
                        , eleValue, step.getString("content"));
                break;
            // > 2.5版本的文本断言语法，支持指定断言的方式
            case "assertText":
                getElementTextAndAssertWithOperation(
                        eleName,
                        eleType,
                        eleValue,
                        step.getString("content"), step.getString("text"),
                        -1);
                break;
            case "assertWebViewText":
                getElementTextAndAssertWithOperation(
                        eleName,
                        eleType,
                        eleValue,
                        step.getString("content"), step.getString("text"),
                        -1);
                break;
            case "assertPocoText":
                getElementTextAndAssertWithOperation(
                        eleName,
                        eleType,
                        eleValue,
                        step.getString("content"), step.getString("text"),
                        -1);
                break;
        }
        switchType(step);
    }

    private void notImplement() {
        ToastUtil.showToast("not implement");
        try {
            throw new Exception("not implement");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object getPocoText(String eleName, String eleType, String eleValue) {
        notImplement();
        return null;
    }

    private void rotateDevice(String stepType) {
        notImplement();
    }

    private void getElementTextAndAssertWithOperation(String eleName, String eleType, String eleValue, String content, String text, int i) {
        notImplement();
    }

    private void switchType(JSONObject stepJson) throws Throwable {
        Integer error = stepJson.getInteger("error");
        String stepDes = resultInfo.getStepDes();
        String detail = resultInfo.getDetail();
        Throwable e = resultInfo.getE();
        if (e != null && !"exit while".equals(e.getMessage()) && !e.getMessage().startsWith("IGNORE:")) {
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
//                    setResultDetailStatus(ResultDetailStatus.WARN);
//                    errorScreen();
//                    exceptionLog(e);
                    break;
                case SHUTDOWN:
                    resultInfo.packError();
//                    log.sendStepLog(StepType.ERROR, stepDes + "异常！", detail);
//                    setResultDetailStatus(ResultDetailStatus.FAIL);
//                    errorScreen();
//                    exceptionLog(e);
                    throw e;
            }
        } else if (!"IGNORE".equals(stepDes)) {
//            log.sendStepLog(StepType.PASS, stepDes, detail);
            resultInfo.packPass();
        }
        Log.i(TAG, String.format("switchType: pack info : %s", resultInfo.getPackInfo()));
    }

    private void getPocoTextAndAssert(String eleName, String eleType, String eleValue, String content) {
        notImplement();
    }

    private void getWebViewTextAndAssert(String eleName, String eleType, String eleValue, String content) {
        notImplement();
    }

    private void getTextAndAssert(String eleName, String eleType, String eleValue, String content) {
        notImplement();
    }

    private void setClipperByKeyboard(String content) {
        notImplement();
    }

    private Object getClipperByKeyboard(Context handleContext) {
        notImplement();
        return null;
    }

    private void iteratorAndroidElement(String eleName, String eleType, String eleValue) {
        notImplement();
    }

    private void iteratorPocoElement(String eleName, String eleType, String eleValue) {
        notImplement();
    }

    private void closeKeyboard(Context handleContext) {
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

    private void closePocoDriver(Context handleContext) {
        notImplement();
    }

    private void thawSource(Context handleContext) {
        notImplement();
    }

    private void freezeSource(Context handleContext) {
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

    private void runScript(String content, String text) {
        notImplement();
    }

    private void setFindElementInterval(Integer content, Integer text) {
        notImplement();
    }

    private Object getWebViewText(String eleName, String eleType, String eleValue) {
        notImplement();
        return null;
    }

    private Object getText(String eleName, String eleType, String eleValue) {
        notImplement();
        return null;
    }

    private void webViewBack(Context handleContext) {
        notImplement();
    }

    private void webViewRefresh(Context handleContext) {
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

    private void publicStep(String content, JSONArray pubSteps) {
        notImplement();
    }

    private void setDefaultFindWebViewElementInterval(Integer content, Integer text) {
        notImplement();
    }

    private void sendKeyForce(String content) {
        notImplement();
    }

    private void runMonkey(JSONObject content, List<JSONObject> text) {
        notImplement();
    }

    private void asserts(String actual, String expect, String stepType) {
        notImplement();
    }

    private void keyCode(String keyCode) {
        Log.i(TAG, String.format("keyCode: press keyCode: [%s]", keyCode));
        InstrumentImpl instrumentImpl = new InstrumentImpl();
        switch (keyCode) {
            case "HOME":
                instrumentImpl.pressKeyCode(KeyEvent.KEYCODE_HOME);
                break;
            default:
                notImplement();
        }
    }

    private void keyCode(Integer content) {
        notImplement();
    }

    private void locationMode(Boolean content) {
        notImplement();
    }

    private void wifiMode(Boolean content) {
        notImplement();
    }

    private void airPlaneMode(Boolean content) {
        notImplement();
    }

    private void unLock(Context handleContext) {
        notImplement();
    }

    private void lock(Context handleContext) {
        notImplement();
    }

    private void uninstall(String text) {
        notImplement();
    }

    private void install(String text) {
        notImplement();
    }

    private void terminate(String text) {
        notImplement();
    }

    private void openApp(String text) {
        notImplement();
    }

    private void stepScreen(Context handleContext) {
        notImplement();
    }

    private void checkImage(String eleName, String eleValue, Double content) {
        notImplement();
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

    private void longPressPoint(String eleName, String eleValue, Integer content) {
        notImplement();
    }

    private void tap(String eleName, String eleValue) {
        resultInfo.setStepDes("点击" + eleName);
        resultInfo.setDetail(String.format("点击坐标(%s)", eleValue));
        eleValue = TextHandler.replaceTrans(eleValue, globalParams);
        LogUtil.i(TAG, String.format("tap: eleName: [%s], pos: [%s]", eleName, eleValue));
        String[] split = eleValue.split(",");
        try {
            TService.getInstance().clickPos(Float.parseFloat(split[0]), Float.parseFloat(split[1]));
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

    private void swipePoint(String eleName, String eleValue, String eleName1, String eleValue1) {
        notImplement();
    }

    private void longPress(String eleName, String eleType, String eleValue, Integer content) {
        notImplement();
    }

    private void isExistEleNum(String eleName, String eleType, String eleValue, String content, Integer text, int i) {
        notImplement();
    }

    private void clear(String eleName, String eleType, String eleValue) {
        notImplement();
    }

    private void scrollToEle(String eleName, String eleType, String eleValue, Integer content, String text) {
        notImplement();
    }

    private void isExistEle(String eleName, String eleType, String eleValue, Boolean content) {
        resultInfo.setStepDes("判断控件 " + eleName + " 是否存在");
        resultInfo.setDetail("期望值：" + (content ? "存在" : "不存在"));
        boolean hasEle = false;
        try {
            hasEle = findEle(eleType, eleValue);
        } catch (Exception ignored) {
        }
        try {
            assert hasEle == content;
        } catch (AssertionError e) {
            resultInfo.setE(e);
        }
    }

    private boolean findEle(String eleType, String eleValue) {
        eleValue = TextHandler.replaceTrans(eleValue, globalParams);
        switch (eleType) {
            case "androidIterator" : notImplement(); break;
            case "id" : return TService.getInstance().getXml().contains(String.format("id=\"%s\"", eleValue));
            case "accessibilityId" : return TService.getInstance().getXml().contains(String.format("desc=\"%s\"", eleValue));
            case "xpath" : notImplement();break;
            case "className" : return TService.getInstance().getXml().contains(String.format("class=\"%s\"", eleValue));
            case "androidUIAutomator" : notImplement();break;
            default :
                notImplement();
                LogUtil.e(TAG, "findEle: " + "查找控件元素失败" + "这个控件元素类型: " + eleType + " 不存在!!!");
//                    log.sendStepLog(StepType.ERROR, "查找控件元素失败", "这个控件元素类型: " + selector + " 不存在!!!");
        }
        return false;
    }

    private void sendKeysByActions(String eleName, String eleType, String eleValue, String content) {
        notImplement();
    }

    private void sendKeys(String eleName, String eleType, String eleValue, String content) {
        notImplement();
    }

    private void logElementAttr(String eleName, String eleType, String eleValue, String text) {
        notImplement();
    }

    private void obtainElementAttr(String eleName, String eleType, String eleValue, String text, String content) {
        notImplement();
    }

    private void getElementAttr(String eleName, String eleType, String eleValue, String text, String content) {
        notImplement();
    }

    private void getActivity(String content) {
        notImplement();
    }

    private void getUrl(String content) {
        notImplement();
    }

    private void getTitle(String content) {
        notImplement();
    }

    private void click(String eleName, String eleType, String eleValue) throws Exception {
        resultInfo.setStepDes("点击" + eleName);
        resultInfo.setDetail("点击" + eleType + ": " + eleValue);

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
                notImplement();
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

    private void clickClassName(String eleValue) throws Exception {
        try {
            TService.getInstance().clickClassName(eleValue);
        } catch (Exception e) {
            LogUtil.e(TAG, "clickClassName: ", e);
            resultInfo.setE(e);
        }
    }

    private void clickDesc(String eleValue) throws Exception {
        try {
            TService.getInstance().clickDesc(eleValue);
        } catch (Exception e) {
            LogUtil.e(TAG, "clickDesc: ", e);
            resultInfo.setE(e);
        }
    }

    private void clickId(String eleValue) throws Exception {
        try {
            TService.getInstance().clickID(eleValue);
        } catch (Exception e) {
            LogUtil.e(TAG, "clickId: ", e);
            resultInfo.setE(e);
        }
    }

    private void clickByImg(String eleName, String eleValue) {
        notImplement();
    }

    private void readText(String content, String text) {
        notImplement();
    }

    private void toHandle(String content) {
        notImplement();
    }

    private void toWebView(String content, String text) {
        notImplement();
    }

    private void stepHold(Integer time) {
        resultInfo.setStepDes("设置全局步骤间隔");
        resultInfo.setDetail("间隔" + time + " ms");
        Log.i(TAG, String.format("stepHold: set as [%s]", time));
        GConfigParams.holdTime = time;
    }

    private void appAutoGrantPermissions(String text) {
        // adb shell dumpsys package com.ape.soundrecorder | grep permission | grep granted=false
        // adb shell pm grant <packageName> <permissionName>
        notImplement();
    }

    private void appReset(String pkg) {
        resultInfo.setStepDes("清空App内存缓存");
        pkg = TextHandler.replaceTrans(pkg, globalParams);
        resultInfo.setDetail("清空 " + pkg);

        StringBuilder stringBuilder = new StringBuilder();
        ShellUtil.execCmd("pm clear " + pkg, 3000, new ShellUtil.OnStreamChangedListener() {
            @Override
            public void onStreamChanged(String line) {

            }

            @Override
            public void onErrorStreamChanged(String line) {
                stringBuilder.append(line);
            }
        });
        resultInfo.setE(new Exception(stringBuilder.toString()));
    }

    private void switchTouchMode(String content) {
        notImplement();
    }
}
