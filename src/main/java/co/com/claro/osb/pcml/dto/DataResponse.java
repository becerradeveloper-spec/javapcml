package co.com.claro.osb.pcml.dto;

public class DataResponse {

    private String parameter;
    private Object value;

    public DataResponse() {
    }

    public DataResponse(String parameter, Object value) {
        this.parameter = parameter;
        this.value = value;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DataResponse{" +
                "parameter='" + parameter + '\'' +
                ", value=" + value +
                '}';
    }
}
