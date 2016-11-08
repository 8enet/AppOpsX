package com.zzzmode.appopsx.common;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by zl on 2016/11/6.
 */

public class OpsCommands {

    public static final String ACTION_GET="get";
    public static final String ACTION_SET="set";

//    public static class GetBuilder{
//        String packageName;
//        String opStr;
//
//        public GetBuilder(String packageName){
//            this.packageName=packageName;
//        }
//
//        public GetBuilder(String packageName,String opStr){
//            this.packageName=packageName;
//            this.opStr=opStr;
//        }
//
//        public GetBuilder opStr(String opStr){
//            this.opStr=opStr;
//            return this;
//        }
//
//        public String getPackageName() {
//            return packageName;
//        }
//
//        public String getOpStr() {
//            return opStr;
//        }
//
//        public String build(){
//            JSONObject jsonObject=new JSONObject();
//            try {
//                jsonObject.put("action",ACTION_GET);
//                JSONObject args=new JSONObject();
//                args.put("packageName",packageName);
//                args.put("opStr",opStr);
//                jsonObject.put("args",args);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return jsonObject.toString();
//        }
//
//        public static GetBuilder parseObject(String json){
//            try {
//                JSONObject jsonObject=new JSONObject(json);
//                jsonObject=jsonObject.getJSONObject("args");
//                return new GetBuilder(jsonObject.getString("packageName"),jsonObject.optString("opStr"));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }
//
//
//    public static class SetBuilder{
//        String packageName;
//        String opStr;
//        String mode;
//
//        public SetBuilder(String packageName,String opStr,String mode){
//            this.packageName=packageName;
//            this.opStr=opStr;
//            this.mode=mode;
//        }
//
//        public String getPackageName() {
//            return packageName;
//        }
//
//        public String getOpStr() {
//            return opStr;
//        }
//
//        public String build(){
//            JSONObject jsonObject=new JSONObject();
//            try {
//                jsonObject.put("action",ACTION_SET);
//                JSONObject args=new JSONObject();
//                args.put("packageName",packageName);
//                args.put("opStr",opStr);
//                args.put("mode",mode);
//                jsonObject.put("args",args);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return jsonObject.toString();
//        }
//
//
//
//        public static final SetBuilder parseObject(String json){
//            try {
//                JSONObject jsonObject=new JSONObject(json);
//                jsonObject=jsonObject.getJSONObject("args");
//                return new SetBuilder(jsonObject.getString("packageName"),jsonObject.getString("opStr"),jsonObject.getString("mode"));
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }
//
//
//
//    public static String parseAction(String json){
//        try {
//            JSONObject jsonObject=new JSONObject(json);
//            if(jsonObject.has("action")){
//                return jsonObject.optString("action");
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static String toGetRestlt(List<PackageOps> list){
//        JSONObject jsonObject=new JSONObject();
//        try {
//            jsonObject.put("code",0);
//            JSONObject data=new JSONObject();
//            data.put("list",toJSONArray(list));
//            jsonObject.put("data",data);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return jsonObject.toString();
//    }
//
//    public static JSONArray toJSONArray(List<PackageOps> list){
//        if(list == null){
//            return new JSONArray();
//        }
//
//        JSONArray jsonArray=new JSONArray();
//        for (PackageOps packageOps : list) {
//            jsonArray.put(toJSONObject(packageOps));
//        }
//        return jsonArray;
//    }
//
//
//    public static JSONObject toJSONObject(PackageOps packageOps){
//        if(packageOps == null){
//            return new JSONObject();
//        }
//        JSONObject jsonObject=new JSONObject();
//        try {
//            jsonObject.put("mPackageName",packageOps.getPackageName());
//            jsonObject.put("mUid",packageOps.getUid());
//            List<OpEntry> ops = packageOps.getOps();
//            if(ops != null){
//                JSONArray jsonArray=new JSONArray();
//                for (OpEntry op : ops) {
//                    jsonArray.put(toJSONObject(op));
//                }
//                jsonObject.put("mEntries",jsonArray);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        return jsonObject;
//    }
//
//    public static JSONObject toJSONObject(OpEntry opEntry){
//        if(opEntry == null){
//            return new JSONObject();
//        }
//        JSONObject jsonObject=new JSONObject();
//        try {
//            jsonObject.put("mOp",opEntry.getOp());
//            jsonObject.put("mMode",opEntry.getMode());
//            jsonObject.put("mTime",opEntry.getTime());
//            jsonObject.put("mRejectTime",opEntry.getRejectTime());
//            jsonObject.put("mDuration",opEntry.getDuration());
//            jsonObject.put("mProxyUid",opEntry.getProxyUid());
//            jsonObject.put("mProxyPackageName",opEntry.getProxyPackageName());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        return jsonObject;
//    }


    public static class Builder implements Parcelable {
        private String action=ACTION_GET;
        private String packageName;
        private String opStr;
        private String mode;



        public String getAction() {
            return action;
        }

        public Builder setAction(String action) {
            this.action = action;
            return this;
        }

        public String getPackageName() {
            return packageName;
        }

        public Builder setPackageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public String getOpStr() {
            return opStr;
        }

        public Builder setOpStr(String opStr) {
            this.opStr = opStr;
            return this;
        }

        public String getMode() {
            return mode;
        }

        public Builder setMode(String mode) {
            this.mode = mode;
            return this;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    "action='" + action + '\'' +
                    ", packageName='" + packageName + '\'' +
                    ", opStr='" + opStr + '\'' +
                    ", mode='" + mode + '\'' +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.action);
            dest.writeString(this.packageName);
            dest.writeString(this.opStr);
            dest.writeString(this.mode);
        }

        public Builder() {
        }

        protected Builder(Parcel in) {
            this.action = in.readString();
            this.packageName = in.readString();
            this.opStr = in.readString();
            this.mode = in.readString();
        }

        public static final Parcelable.Creator<Builder> CREATOR = new Parcelable.Creator<Builder>() {
            @Override
            public Builder createFromParcel(Parcel source) {
                return new Builder(source);
            }

            @Override
            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };
    }
}
