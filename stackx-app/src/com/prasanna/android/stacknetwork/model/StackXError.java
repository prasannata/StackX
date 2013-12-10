package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.utils.LogWrapper;

public class StackXError implements Serializable {
    private static final String TAG = StackXError.class.getSimpleName();

    private static final long serialVersionUID = -8292786331139417559L;

    public int statusCode;

    public int id;

    public String name;

    public String msg;

    public static StackXError deserialize(String jsonText) {
        try {
            StackXError error = new StackXError();
            JSONObject response = new JSONObject(jsonText);
            error.id = response.getInt(StringConstants.HttpError.ERROR_ID);
            error.name = response.getString(StringConstants.HttpError.ERROR_NAME);
            error.msg = response.getString(StringConstants.HttpError.ERROR_MESSAGE);
            return error;
        }
        catch (JSONException e) {
            LogWrapper.e(TAG, "Json parsing failed: " + e.getMessage());
        }

        return null;
    }
}
