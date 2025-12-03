package co.com.claro.osb.pcml.dto;

import java.util.List;

public class ConsumePCMLResponse {

    private String code;
    private String description;
    private Object data;

    public ConsumePCMLResponse() {
    }

    public ConsumePCMLResponse(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public ConsumePCMLResponse(String code, String description, List<DataResponse> data) {
        this.code = code;
        this.description = description;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ConsumePCMLResponse{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", data=" + data +
                '}';
    }
}
